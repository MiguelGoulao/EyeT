package controllers;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import views.ActionToolBar;
import views.MainFrame;
import views.TopMenu;

public class ScreenCaptureController {

	private static final int FRAME_RATE = 60;

	private MainFrame mainFrame;
	private boolean recording;
	private GazeController gazeController;
	private MovieController movieController;

	private CaptureLoop captureLoop;
	private boolean paused;
	private long pauseStart;
	private long pausedTime;

	private String workingDirectory = "E:\\";

	public ScreenCaptureController(GazeController gazeController,
			MovieController movieController) {

		this.gazeController = gazeController;
		this.movieController = movieController;
		this.paused = false;

		this.mainFrame = new MainFrame();
		TopMenu topMenu = new TopMenu(this);
		mainFrame.add(topMenu, BorderLayout.NORTH);
		JPanel middlePanel = new JPanel();
		middlePanel.setLayout(new BorderLayout());
		mainFrame.add(middlePanel, BorderLayout.CENTER);
		ActionToolBar toolbar = new ActionToolBar(this);
		middlePanel.add(toolbar, BorderLayout.NORTH);
		mainFrame.setVisible(true);
		mainFrame.pack();

		recording = false;
		loadSettings();
	}

	public BufferedImage captureScreen() {
		Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit()
				.getScreenSize());
		BufferedImage capture = null;
		try {
			capture = new Robot().createScreenCapture(screenRect);
		} catch (AWTException e) {
			e.printStackTrace();
		}
		return capture;
	}

	public void startRecording() {
		if (!recording) {
			if (!paused) {
				pausedTime = 0;
				String currentTime = getCurrentTime();
				movieController.startRecording(workingDirectory + "\\"
						+ currentTime);
				gazeController.startRecording(workingDirectory + "\\"
						+ currentTime);
			} else {
				pausedTime += System.nanoTime() - pauseStart;
				paused = false;
			}

			recording = true;

			captureLoop = new CaptureLoop();
			captureLoop.start();

		}
	}

	public void pauseRecording() {
		if (recording) {
			recording = false;
			paused = true;
			pauseStart = System.nanoTime();

			gazeController.pauseRecording();
		}
	}

	public void endRecording() {
		if (recording || paused) {
			recording = false;

			try {
				captureLoop.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			movieController.endRecording();
			gazeController.endRecording();
		}
	}

	private class CaptureLoop extends Thread {

		@Override
		public void run() {
			while (recording) {

				BufferedImage screenshot = captureScreen();
				gazeController.addCursor(screenshot);
				gazeController.addCurrentEyePosition(screenshot);

				movieController.encodeImage(screenshot, pausedTime);
				
				try {
					Thread.sleep(FRAME_RATE);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
		}
	}

	private String getCurrentTime() {
		DateFormat dateFormat = new SimpleDateFormat(
				"yyyy MM dd HH'h'mm'm'ss's'");
		Date date = new Date();

		return dateFormat.format(date);
	}

	public boolean isCapturing() {
		return recording;
	}

	public void setCapturing(boolean recording) {
		this.recording = recording;
	}

	public String getWorkingDirectory() {
		return workingDirectory;
	}

	public void setWorkingDirectory(String workingDirectory) {
		this.workingDirectory = workingDirectory;
		saveSettings();
	}
	
	public void loadSettings() {
		File file = new File("settings.txt");
		
		if(file.exists() && ! file.isDirectory()) {
			try {
				Scanner scanner = new Scanner(file);
				while(scanner.hasNext()) {
					if(scanner.next().equals("workingDirectory")) {
						scanner.next();
						workingDirectory = scanner.next();
					}
				}
				scanner.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
	}
	
	public void saveSettings() {
		File file = new File("settings.txt");
		try{
			FileWriter fw = new FileWriter(file, false);
			fw.write("workingDirectory = " + workingDirectory);
			fw.close();
		}
		catch( IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		GazeController gc = new GazeController();
		MovieController mc = new MovieController();
		ScreenCaptureController scc = new ScreenCaptureController(gc, mc);
	}

}
