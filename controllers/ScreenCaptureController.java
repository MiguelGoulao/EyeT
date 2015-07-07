package controllers;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import views.ImagePanel;
import views.MainFrame;

public class ScreenCaptureController {
	
	private ImagePanel imagePanel;
	private MainFrame mainFrame;
	private boolean capturing;
	private GazeController gazeController;
	
	
	public ScreenCaptureController(GazeController gazeController) {
		
		this.gazeController = gazeController;
		this.mainFrame = new MainFrame();
		this.imagePanel = new ImagePanel();
		mainFrame.add(imagePanel, BorderLayout.CENTER);
		mainFrame.setVisible(true);
		
		CaptureLoop captureLoop = new CaptureLoop();
		captureLoop.start();
		
		capturing = true;
	}
	
	public BufferedImage captureScreen() {
		Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
		BufferedImage capture = null;
		try {
			capture = new Robot().createScreenCapture(screenRect);
			gazeController.addCurrentEyePosition(capture);
			//ImageIO.write(capture, "bmp", new File("screenshot.png"));
		} 
		catch ( AWTException e) {
			e.printStackTrace();
		}
		return capture;
	}
	
	public void setImage(BufferedImage img) {
		
		imagePanel.setImage(img);
		mainFrame.pack();
		//mainFrame.setSize(new Dimension(img.getWidth(), img.getHeight() + topMenu.getHeight()));
	}
	
	private class CaptureLoop extends Thread {

		private long startTime;
		private long endTime;
		private long duration;
		
		@Override
		public void run() {
			
			while(capturing) {
				startTime = System.nanoTime();
				imagePanel.setImage(captureScreen());
				endTime = System.nanoTime();
				duration = endTime - startTime;
				System.out.println(duration / 1000000);
				/*
				try {
					Thread.sleep(30);
				} 
				catch (InterruptedException e) {
					e.printStackTrace();
				}*/
			}
		}
	}
	
	public static void main(String args[]) {
		
		//MovieController mc = new MovieController();
		GazeController gc = new GazeController();
		ScreenCaptureController scc = new ScreenCaptureController(gc);
	}
}
