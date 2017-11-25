package Imaging;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;
public class Worker implements Callable<BufferedImage>{

	//position and offset + dimenson
	int w = 0;
	int h = 0;
	int x = 0;
	int y = 0;
	BufferedImage bi;
	public Worker(int w, int h, int x, int y){
		this.w = w;
		this.h = h;
		this.x = x;
		this.y = y;
		bi = new BufferedImage(w,h,BufferedImage.TYPE_3BYTE_BGR);
	}

	public BufferedImage call() throws Exception {
		//return image
		Graphics g = bi.getGraphics();
		for(int i = 0; i < w;i++){
			for(int j = 0; j < h;j++){
				int X = i + x;
				int Y = j + y;
				g.setColor(new Color(FastImaging.Fast[1 - FastImaging.current].getRGB(X, Y)));
				g.fillRect(i, j, 1, 1);
			}
		}
		return bi;
	}

}
