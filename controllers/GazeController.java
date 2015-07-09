package controllers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
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
	private static final int MAX_DIAMETER = 50;

	public static final int SACCADE_RANGE = 80;
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

	private void add(GazeData gaze) {
		boolean newFixated = lastIsSaccade() && gaze.isFixated;
		if (isSaccade(gaze) || newFixated || lastNGazes.isEmpty()) {

			if (lastNGazes.size() == GAZES_NUMBER)
				lastNGazes.remove(0);

			lastNGazes.add(gaze);
			System.out.println("Added: " + gaze.timeStampString);
		}
	}

	private boolean isSaccade(GazeData gaze) {
		return !gaze.isFixated && !inRange(gaze);
	}

	private boolean lastIsSaccade() {
		GazeData last = getLatest();
		return last != null && !last.isFixated;
	}

	private boolean inRange(GazeData gaze) {
		GazeData last = getLatest();
		if (last == null)
			return true;

		return Point.distance(last.smoothedCoordinates.x,
				last.smoothedCoordinates.y, gaze.smoothedCoordinates.x,
				gaze.smoothedCoordinates.y) <= SACCADE_RANGE;

	}

	private GazeData getLatest() {
		int index = lastNGazes.size() - 1;
		return index >= 0 ? lastNGazes.get(index) : null;
	}

	private boolean isLooking() {
		GazeData gaze = getLatest();
		return gaze != null && gaze.smoothedCoordinates.x != 0
				&& gaze.smoothedCoordinates.y != 0;
	}

	private boolean isFixed() {
		GazeData gaze = getLatest();
		return gaze != null && gaze.isFixated;
	}

	public void addCurrentEyePosition(BufferedImage img) {
		if (isLooking()) {
			marker = img.createGraphics();
			marker.setStroke(new BasicStroke(3));
			if (isFixed())
				fixationsCounter++;
			else
				fixationsCounter = 0;

			markLatestGazes();

			if (atLeastTwoGaze())
				markSaccadesPaths(img);

			marker.dispose();
		}
	}

	private boolean atLeastTwoGaze() {
		return lastNGazes.size() >= 2;
	}

	public void markLatestGazes() {
		for (GazeData gaze : lastNGazes) {
			double x = gaze.smoothedCoordinates.x;
			double y = gaze.smoothedCoordinates.y;

			int size = 0;
			if (isLast(gaze)) {
				marker.setColor(Color.GREEN);
				if (gaze.isFixated) {
					size = BASE_DIAMETER + fixationsCounter;
				}
			} else {
				size = BASE_DIAMETER;
				marker.setColor(Color.RED);
			}
			if (size <= MAX_DIAMETER)
				marker.drawOval((int) x, (int) y, size, size);
			else
				marker.fillOval((int) x, (int) y, MAX_DIAMETER, MAX_DIAMETER);
		}
	}

	public boolean isLast(GazeData gaze) {
		return lastNGazes.indexOf(gaze) == lastNGazes.size() - 1;
	}

	public void markSaccadesPaths(BufferedImage img) {
		Graphics2D g2d = img.createGraphics();
		g2d.setColor(Color.BLUE);

		for (int i = 0; i < lastNGazes.size() - 1; i++) {
			GazeData gA = lastNGazes.get(i);
			GazeData gB = lastNGazes.get(i + 1);

			g2d.drawLine((int) gA.rawCoordinates.x, (int) gA.rawCoordinates.y,
					(int) gB.rawCoordinates.x, (int) gB.rawCoordinates.y);
		}
		g2d.dispose();
	}

	public void addCursor(BufferedImage img) {
		Image cursor;
		int x = MouseInfo.getPointerInfo().getLocation().x;
		int y = MouseInfo.getPointerInfo().getLocation().y;
		try {
			cursor = ImageIO.read(getClass().getResource(
					"/resources/cursor.png"));

			Graphics2D g2d = img.createGraphics();
			img.createGraphics().drawImage(cursor, x, y, 16, 16, null);
			g2d.dispose();
		} catch (IOException e) {
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
		add(gazeData);
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

	public void pauseRecording() {
		recording = !recording;
	}
}
