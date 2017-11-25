package Imaging;

import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class FastImaging extends JPanel implements Runnable,KeyListener{
	static BufferedImage i;
	static BufferedImage[] i2 = new BufferedImage[4];
	static ArrayList<Integer> a = new ArrayList<Integer>();
	static int cooldown = 0;
	static int maxCooldown = 20;
	static boolean reverse = false;
	static int w = 0;
	static int h = 0;
	static int ticks = 0;
	static Random rand = new Random();
	static boolean paused = false;
	static int[][][] RGBs; //TODO Have a single array of bit shifted RGB, then to swap r/g/bs just bit shift by 2^s and swap? (binary logic swapping???)
	static int[][][] originalRGBs;
	static boolean[] keySet = new boolean[256];
	static ExecutorService e = Executors.newFixedThreadPool(4);
	static FastRGB Fast[] = new FastRGB[2];
	static int current = 0;
	public static BufferedImage scale( BufferedImage sbi,  int imageType,  int dWidth,  int dHeight,  double fWidth,  double fHeight) {
		BufferedImage dbi = null;
		if(sbi != null) {
			dbi = new BufferedImage(dWidth, dHeight, imageType);
			Graphics2D g = dbi.createGraphics();
			AffineTransform at = AffineTransform.getScaleInstance(fWidth, fHeight);
			g.drawRenderedImage(sbi, at);
		}
		return dbi;
	}
	public static void main(String[] args) throws InterruptedException{
		System.setProperty("sun.java2d.opengl","True");
		FileDialog fd = new FileDialog((java.awt.Frame) null);
		fd.setTitle("Choose an image");
		fd.setVisible(true);
		File f = new File(fd.getDirectory() + fd.getFile());
		if(fd.getDirectory() == null || fd.getFile() == null)
			System.exit(0);
		BufferedImage img = null;
		try {
			img = ImageIO.read(f);
			img.getType();
		} catch (IOException | NullPointerException e) {}
		i = img;
		w = i.getWidth();
		h = i.getHeight();
		int mW = 350;
		if(w > mW){
			JOptionPane infoPane = new JOptionPane();
			infoPane.setMessage("Image is too big, do you want it resized?");
			infoPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
			infoPane.setOptionType(JOptionPane.PLAIN_MESSAGE);
			JDialog infoDialog = infoPane.createDialog(new JFrame(), "Uh Oh");
			infoDialog.setResizable(true);
			infoDialog.setModal(true);
			Object[] options = { "OK!", "NO!","EXIT?" };
			infoPane.setOptions(options);
			infoDialog.setSize(320,200);
			infoDialog.setVisible(true);
			if(infoPane.getValue() == "EXIT?" || infoPane.getValue() == null){
				System.exit(0);
			}
			if(infoPane.getValue() == "OK!"){
				double w1 = i.getWidth();
				double h1 = i.getHeight();
				double w2 = mW;
				double ratio = (double) w1 / (double) w2;
				double h2 = h1 / ratio;
				i = scale(img, img.getType(), mW,(int)h2, 1D / ratio, 1D / ratio);
			}
			w = i.getWidth();
			h = i.getHeight();

		}
		Fast[0] = new FastRGB(i);
		Fast[1] = new FastRGB(i);
		JFrame fr = new JFrame();
		FastImaging god = new FastImaging();
		god.addKeyListener(god);
		god.setFocusable(true);
		god.setSize(1920,1080);
		fr.add(god);
		fr.pack();
		fr.setSize(1000, 1000);
		fr.setVisible(true);
		fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Thread t1 = new Thread(god);
		t1.start();
		for(int i = 0; i < 4;i++){
			i2[i] = new BufferedImage(w / 2,h / 2,BufferedImage.TYPE_3BYTE_BGR);
		}
		RGBs = new int[w][h][3];
		originalRGBs = new int[w][h][3];
		for(int x = 0; x < w;x++){
			for(int y = 0; y < h;y++){	
				int rgb = i.getRGB(x, y);
				int r = rgb >> 16 & 0XFF;
			int g = rgb >> 8 & 0XFF;
		int b = rgb & 0XFF;
		RGBs[x][y][0] = r;
		RGBs[x][y][1] = g;
		RGBs[x][y][2] = b;
		originalRGBs[x][y][0] = r;
		originalRGBs[x][y][1] = g;
		originalRGBs[x][y][2] = b;

			}
		}
		while(true){
			update();
			doImages();
		}
	}
	//	int rgb = r << 16 | g << 8 | b ;
	//	int r = rgb >> 16 & 0XFF;
	//	int g = rgb >> 8 & 0XFF;
	//	int b = rgb & 0XFF;
	//	static BufferedImage deepCopy(BufferedImage bi){
	//		ColorModel cm = bi.getColorModel();
	//		boolean iap = cm.isAlphaPremultiplied();
	//		WritableRaster raster = bi.copyData(null);
	//		return new BufferedImage(cm,raster,iap,null);
	//	}
	public static void update(){
		ticks++;
		cooldown--;
		//Multithread this too?
		int g = 1 - current;
		for(int x = 0; x < w;x++){
			for(int y = 0; y < h;y++){	
				if(!paused){
//					int s = rand.nextInt(3);
//					s = 1;
//					int X = rand.nextInt(5) - 2;
//					int Y = rand.nextInt(5) - 2;
////					X = 0;
////					Y = 0;
//					if(keySet[KeyEvent.VK_UP]){
//						Y =  1;
//					}
//					if(keySet[KeyEvent.VK_DOWN]){
//						Y = -1;
//					}
//					if(keySet[KeyEvent.VK_LEFT]){
//						X = 1;
//					}
//					if(keySet[KeyEvent.VK_RIGHT]){
//						X = -1;
//					}
//					if(keySet[KeyEvent.VK_CONTROL]){
//						X *= 3;
//						Y *= 3;
//					}
//					if(keySet[KeyEvent.VK_SHIFT]){
//						X *= 2;
//						Y *= 2;
//					}
//					int nX = x + X;
//					int nY = y + Y;
//					int c = 10;
//					if(keySet[KeyEvent.VK_B]){
//						c += 80;
//					}
//					if(keySet[KeyEvent.VK_V]){
//						c += 100;
//					}
//					if((rand.nextInt(100) < c)){
//						if(nX < 0){
//							nX = w + nX;
//						}
//						if(nY < 0){
//							nY = h + nY;
//						}
//						if(nX >= w){
//							nX = nX % w;
//						}
//						if(nY >= h){
//							nY = nY % h;
//						} 
//						int RGB = Fast[g].getRGB(nX, nY);
//						Fast[current].setRGB(nX,nY,Fast[g].getRGB(x, y),2);
//						Fast[current].setRGB(x, y, RGB,1);
//					}
				}
			}
		}
		current = g;

	}
	public static void doImages(){
		//		long t0 = System.nanoTime();
		//		for(int i = 0; i < 10;i++){
		Future<BufferedImage> f1 = e.submit(new Worker(w/2, h/2, 0,   0));
		Future<BufferedImage> f2 = e.submit(new Worker(w/2, h/2, w/2, 0));
		Future<BufferedImage> f3 = e.submit(new Worker(w/2, h/2, 0,   h/2));
		Future<BufferedImage> f4 = e.submit(new Worker(w/2, h/2, w/2, h/2));
		while(!(f1.isDone() && f2.isDone() && f3.isDone() && f4.isDone())){

		}
		try {
			i2[0] = f1.get();
			i2[1] = f2.get();
			i2[2] = f3.get();
			i2[3] = f4.get();
		} catch (InterruptedException | ExecutionException e1) {
			e1.printStackTrace();
		}
		//		}
		//		long t1 = System.nanoTime();
		//		System.out.println(t1 - t0);
	}
	public void paint(Graphics gr){
		super.paint(gr);

		gr.drawImage(i2[0], 0, 0, null);
		gr.drawImage(i2[1], w/2, 0, null);
		gr.drawImage(i2[2], 0, h/2, null);
		gr.drawImage(i2[3], w/2, h/2, null);


	}
	@Override
	public void run() {
		while(true) {
			repaint();
			if(keySet[KeyEvent.VK_SPACE] && cooldown < 0){
				cooldown = maxCooldown;
				paused = !paused;
			}
		}
	}
	public void keyTyped(KeyEvent e) {
	}
	public void keyPressed(KeyEvent e) {keySet[e.getKeyCode()] = true;}
	public void keyReleased(KeyEvent e) {keySet[e.getKeyCode()] = false;}
}
