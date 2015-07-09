package controllers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import com.theeyetribe.client.GazeManager;
import com.theeyetribe.client.GazeManager.ApiVersion;
import com.theeyetribe.client.GazeManager.ClientMode;
import com.theeyetribe.client.IGazeListener;
import com.theeyetribe.client.data.GazeData;

public class GazeController {

	private static final int BASE_DIAMETER = 30;
	public static final int FIXATION_RANGE = 15;
	private static final int GAZES_NUMBER = 3;

	private List<GazeData> lastNGazes;
	private int fixationsCounter = 0;
	private Graphics2D marker;
	private FileWriter outputFileWriter;
	private boolean recording;

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

		lastNGazes = new ArrayList<>(GAZES_NUMBER);
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

	public void addIfSaccadeOrFirstLook(GazeData gaze) {
		if (!gaze.isFixated || currentLastIsSaccade() || lastNGazes.isEmpty()) {

			if (lastNGazes.size() == GAZES_NUMBER)
				lastNGazes.remove(0);

			lastNGazes.add(gaze);
			System.out.println("Added: " + gaze.timeStampString);
		}
	}

	private boolean currentLastIsSaccade() {
		GazeData last = getLatest();
		return last != null && !last.isFixated;
	}

	public GazeData getLatest() {
		return lastNGazes.get(lastNGazes.size() - 1);
	}

	public boolean isLooking() {
		GazeData gaze = getLatest();
		return gaze != null && gaze.smoothedCoordinates.x != 0
				&& gaze.smoothedCoordinates.y != 0;
	}

	public boolean isFixed() {
		GazeData gaze = getLatest();
		return gaze != null && gaze.isFixated;
	}

	public void addCurrentEyePosition(BufferedImage img) {
		if (isLooking()) {
			marker = img.createGraphics();

			if (isFixed())
				fixationsCounter++;
			else
				fixationsCounter = 0;

			markLatestGazes();

			// markSaccadesPaths(img);
			marker.dispose();
		}
	}

	public void markLatestGazes() {
		for (GazeData gaze : lastNGazes) {
			double x = gaze.smoothedCoordinates.x;
			double y = gaze.smoothedCoordinates.y;

			int size = 0;
			if (isLast(gaze) && gaze.isFixated) {
				System.out.println("======fixed======");
				size = BASE_DIAMETER + fixationsCounter;
				marker.setColor(Color.GREEN);
			} else {
				size = BASE_DIAMETER;
				marker.setColor(Color.YELLOW);
			}
			marker.fillOval((int) x, (int) y, size, size);
		}
	}

	public boolean isLast(GazeData gaze) {
		System.out.println("index: " + lastNGazes.indexOf(gaze) + "is fixed: "
				+ gaze.isFixated);
		return lastNGazes.indexOf(gaze) == (GAZES_NUMBER - 1);
	}

	// public void markSaccadesPaths(BufferedImage img) {
	// EyeMarker m1 = last3Gazes.get(0);
	// EyeMarker m2 = null;
	// EyeMarker m3 = null;
	// if (last3Gazes.size() >= 2)
	// m2 = last3Gazes.get(1);
	// if (last3Gazes.size() >= 3)
	// m3 = last3Gazes.get(2);
	//
	// Graphics2D g2d = img.createGraphics();
	// g2d.setColor(Color.BLUE);
	//
	// if (m2 != null) {
	// g2d.drawLine((int) m1.getX(), (int) m1.getY(), (int) m2.getX(),
	// (int) m2.getY());
	// if (m3 != null)
	// g2d.drawLine((int) m2.getX(), (int) m2.getY(), (int) m3.getX(),
	// (int) m3.getY());
	// }
	//
	// g2d.dispose();
	// }

	public void addCursor(BufferedImage img) {
		Image cursor;
		int x = MouseInfo.getPointerInfo().getLocation().x;
		int y = MouseInfo.getPointerInfo().getLocation().y;
		try {
			cursor = ImageIO.read(new File("cursor.png"));
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
		addIfSaccadeOrFirstLook(gazeData);
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