package controllers;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class ImagePanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	BufferedImage displayedImage;
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		//Draw the loaded picture
		if (displayedImage != null){
			g.drawImage(displayedImage, 0, 0, null);
			revalidate();
		}
	}
	
	public void setImage(BufferedImage img) {
		this.displayedImage = img;
		setPreferredSize(new Dimension(displayedImage.getWidth(), displayedImage.getHeight()));
		repaint();
	}
	
	
}
