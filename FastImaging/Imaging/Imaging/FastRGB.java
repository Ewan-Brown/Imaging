package Imaging;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;


public class FastRGB {

	private final int width;
	private final int height;
	private final boolean hasAlphaChannel;
	private int pixelLength;
	private final byte[] pixels;

	public FastRGB(BufferedImage image) {
		pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		width = image.getWidth();
		height = image.getHeight();
		hasAlphaChannel = image.getAlphaRaster() != null;
		pixelLength = 3;
		if (hasAlphaChannel) {
			pixelLength = 4;
		}
	}

	public int getRGB(int x, int y) {
		int pos = (y * pixelLength * width) + (x * pixelLength);
		int argb = -16777216;
		if (hasAlphaChannel) {
			argb = (((int) pixels[pos++] & 0xff) << 24);
		}
		argb += ((int) pixels[pos++] & 0xff);
		argb += (((int) pixels[pos++] & 0xff) << 8);
		argb += (((int) pixels[pos++] & 0xff) << 16);
		return argb;
	}
	public void setRGB(int x, int y,int rgb,int s){
		int pos = (y * pixelLength * width) + (x * pixelLength);
		byte r = (byte)(rgb & 0XFF);
		byte g = (byte)(rgb >> 8 & 0XFF);
		byte b = (byte)(rgb >> 16 & 0XFF);
		switch(s){
		case 0:
			pixels[pos++] = r;
			pixels[pos++] = g;
			pixels[pos++] = b;
			break;
		case 1:
			pixels[pos++] = r;
			break;
		case 2:
			pixels[pos++] = g;
			break;
		case 3:
			pixels[pos++] = b;
			break;
		}
	}
}