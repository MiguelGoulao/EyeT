package controllers;

import java.awt.AWTException;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import views.ImagePanel;
import views.MainFrame;

public class ScreenCaptureController {

	private static final int FRAME_RATE = 30;

	private ImagePanel imagePanel;
	private MainFrame mainFrame;
	private boolean capturing;
	private GazeController gazeController;
	private MovieController movieController;

	public ScreenCaptureController(GazeController gazeController,
			MovieController movieController) {

		this.gazeController = gazeController;
		this.movieController = movieController;

		/*
		 * this.mainFrame = new MainFrame(); this.imagePanel = new ImagePanel();
		 * mainFrame.add(imagePanel, BorderLayout.CENTER);
		 * mainFrame.setVisible(true);
		 */
		CaptureLoop captureLoop = new CaptureLoop();
		captureLoop.start();

		capturing = true;
	}

	public BufferedImage captureScreen() {
		Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit()
				.getScreenSize());
		BufferedImage capture = null;
		try {
			capture = new Robot().createScreenCapture(screenRect);
			// ImageIO.write(capture, "bmp", new File("screenshot.png"));
		} catch (AWTException e) {
			e.printStackTrace();
		}
		return capture;
	}

	private void setImage(BufferedImage img) {

		imagePanel.setImage(img);
		mainFrame.pack();
		// mainFrame.setSize(new Dimension(img.getWidth(), img.getHeight() +
		// topMenu.getHeight()));
	}

	private class CaptureLoop extends Thread {

		private long startTime;
		private long endTime;
		private long duration;

		@Override
		public void run() {

			movieController.startRecording("recording " + getCurrentTime());
			gazeController.startRecording("recording " + getCurrentTime());

			while (capturing) {
				BufferedImage screenshot = captureScreen();
				gazeController.addCursor(screenshot);
				gazeController.addCurrentEyePosition(screenshot);

				movieController.encodeImage(screenshot);

				try {
					Thread.sleep(FRAME_RATE);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			movieController.endRecording();
			gazeController.endRecording();
		}
	}

	private String getCurrentTime() {
		DateFormat dateFormat = new SimpleDateFormat(
				"yyyy MM dd HH'h'mm'm'ss's'");
		Date date = new Date();

		return dateFormat.format(date);
	}

	public boolean isCapturing() {
		return capturing;
	}

	public void setCapturing(boolean capturing) {
		this.capturing = capturing;
	}

	public static void main(String args[]) {
		GazeController gc = new GazeController();
		MovieController mc = new MovieController();
		ScreenCaptureController scc = new ScreenCaptureController(gc, mc);

		try {
			Thread.sleep(22000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		scc.setCapturing(false);
	}

}
