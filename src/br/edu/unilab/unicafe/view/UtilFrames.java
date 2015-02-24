package br.edu.unilab.unicafe.view;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
*
* @author Erivando Sena
*/

public class UtilFrames {
	
	protected static final String BASE_PATH_IMAGES = "/br/edu/unilab/unicafe/images/";
	
	/**
	 * 
	 * Redimensionar imagem proporcionalmente conforme altura e altura sem
	 * distorcer a mesma
	 * 
	 * @param srcImg
	 * @param w
	 * @param h
	 * @return Image
	 */
	public static Image getScaledImage(Image srcImg, int w, int h) {
		BufferedImage resizedImg = new BufferedImage(w, h,BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = resizedImg.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(srcImg, 0, 0, w, h, null);
		g2.dispose();
		
		return resizedImg;
	}

}
