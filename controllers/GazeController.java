package controllers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import com.theeyetribe.client.GazeManager;
import com.theeyetribe.client.IGazeListener;
import com.theeyetribe.client.GazeManager.ApiVersion;
import com.theeyetribe.client.GazeManager.ClientMode;
import com.theeyetribe.client.data.GazeData;

public class GazeController {

	private FileWriter outputFileWriter;
	private boolean recording;
	private GazeData lastGaze;

	public GazeController() {
		final GazeManager gm = GazeManager.getInstance();
		gm.activate(ApiVersion.VERSION_1_0, ClientMode.PUSH);

		final GazeListener gazeListener = new GazeListener();
		gm.addGazeListener(gazeListener);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				gm.removeGazeListener(gazeListener);
				gm.deactivate();
			}
		});
	}

	private class GazeListener implements IGazeListener {

		@Override
		public void onGazeUpdate(GazeData gazeData) {
			if (recording) {
				saveData(gazeData);
				System.out.println(gazeData.smoothedCoordinates.x + " "
						+ gazeData.smoothedCoordinates.y);
			}
		}
	}

	public void addCurrentEyePosition(BufferedImage img) {
		double x = lastGaze.smoothedCoordinates.x ;
		double y = lastGaze.smoothedCoordinates.y; 
		if (lastGaze != null && x != 0 && y != 0) {
			Graphics2D g2d = img.createGraphics();
			g2d.setColor(Color.GREEN);
			System.out.println("drawing "
					+ (int) x + " "
					+ (int) y);
			g2d.fillOval((int) x,
					(int) y, 30, 30);
			g2d.dispose();
		}
	}

	public void addCursor(BufferedImage img) {
		Image cursor;
		int x = MouseInfo.getPointerInfo().getLocation().x;
		int y = MouseInfo.getPointerInfo().getLocation().y;
		try {
			cursor = ImageIO.read(new File("cursor.png"));
//					"C:\\Users\\Arkadiusz\\Desktop\\EYETT\\cursor.png"));
			Graphics2D g2d = img.createGraphics();
			img.createGraphics().drawImage(cursor, x, y, 16, 16, null);
			g2d.dispose();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void startRecording(String filename) {
		try {
			outputFileWriter = new FileWriter(filename + ".csv");

			outputFileWriter.append("Time Stamp");
			outputFileWriter.append(';');
			outputFileWriter.append("x");
			outputFileWriter.append(';');
			outputFileWriter.append("y");
			outputFileWriter.append(';');
			outputFileWriter.append('\n');

			recording = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveData(GazeData gazeData) {
		lastGaze = gazeData;
		try {
			outputFileWriter.append(gazeData.timeStampString);
			outputFileWriter.append(';');
			outputFileWriter.append(Double
					.toString(gazeData.smoothedCoordinates.x));
			outputFileWriter.append(';');
			outputFileWriter.append(Double
					.toString(gazeData.smoothedCoordinates.y));
			outputFileWriter.append(';');
			outputFileWriter.append('\n');
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void endRecording() {

		recording = false;

		try {
			outputFileWriter.flush();
			outputFileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
