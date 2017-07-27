package controllers;

import java.awt.AWTException;
import java.awt.BorderLayout;
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

import javax.sound.sampled.LineUnavailableException;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import views.ActionToolBar;
import views.MainFrame;
import views.TopMenu;

public class ScreenCaptureController {

	private MainFrame mainFrame;
	private boolean recording;
	private DataController gazeController;
	private MovieController movieController;
	private AudioController audioController;
	private ImageEditor imageEditor;

	private CaptureLoop captureLoop;
	private boolean paused;
	private long pauseStart;
	private long pausedTime;

	private String workingDirectory;

	// fields used in capturing screen
	private Robot robot;
	private Rectangle screenRect;

	public ScreenCaptureController(DataController gazeController, MovieController movieController,
			AudioController audioController) {

		this.gazeController = gazeController;
		this.movieController = movieController;
		this.audioController = audioController;
		this.imageEditor = new ImageEditor(gazeController);
		this.paused = false;

		this.screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
		try {
			this.robot = new Robot();
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		recording = false;
		loadSettings();

		buildWindows();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				endRecording();
			}
		});
	}

	private void buildWindows() {
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
	}

	public BufferedImage captureScreen() {
		return robot.createScreenCapture(screenRect);
	}

	public void startRecording() {
		if (!recording) {
			if (!paused) {
				pausedTime = 0;
				String currentTime = getCurrentTime();

				if (workingDirectory == null) {
					movieController.startRecording(currentTime);
					audioController.setFilename(currentTime);
					gazeController.startRecording(currentTime);
				} else {
					movieController.startRecording(workingDirectory + "\\" + currentTime);
					audioController.setFilename(workingDirectory + "\\" + currentTime);
					gazeController.startRecording(workingDirectory + "\\" + currentTime);
				}

			} else {
				pausedTime += System.nanoTime() - pauseStart;
				gazeController.pauseRecording();
				paused = false;

			}

			recording = true;

			captureLoop = new CaptureLoop();
		
			captureLoop.start();

			try {
				recordThread.start();
			} catch (Exception e) {
				// TODO Auto-generated catch block
			//	e.printStackTrace();
				
				System.out.println("Problem with sound recording");
			}

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

			audioController.stop();
			audioController.save();
			
			movieController.endRecording();			

			gazeController.endRecording();
			
		}
	}

	private class CaptureLoop extends Thread {

		@Override
		public void run() {
			while (recording) {

				BufferedImage screenshot = captureScreen();
				imageEditor.addCurrentEyePosition(screenshot);

				movieController.encodeImage(screenshot, pausedTime);
			}
		}
	}
	
	Thread recordThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                audioController.start();
            } catch (LineUnavailableException ex) {
            	System.out.println("cant record audio");
            }              
        }
    });

	
	private String getCurrentTime() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy MM dd HH'h'mm'm'ss's'");
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

	public void badDirectory() {
		JOptionPane.showMessageDialog(mainFrame, "You have no authorization to write in this location");
	}

	public void loadSettings() {
		File file = new File("settings.txt");

		if (file.exists() && !file.isDirectory()) {
			try {
				Scanner scanner = new Scanner(file);
				while (scanner.hasNext()) {
					if (scanner.next().equals("workingDirectory")) {
						scanner.next();
						workingDirectory = scanner.nextLine();
						workingDirectory = workingDirectory.trim();
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
		try {
			FileWriter fw = new FileWriter(file, false);
			fw.write("workingDirectory = " + workingDirectory);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		DataController gc = new DataController();
		MovieController mc = new MovieController();
		AudioController ac = new AudioController();
		ScreenCaptureController scc = new ScreenCaptureController(gc, mc, ac);
	}

}
