package controllers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.imageio.ImageIO;

import com.theeyetribe.client.GazeManager;
import com.theeyetribe.client.GazeManager.ApiVersion;
import com.theeyetribe.client.GazeManager.ClientMode;
import com.theeyetribe.client.IGazeListener;
import com.theeyetribe.client.data.GazeData;

public class GazeController {

	private static final int BASE_DIAMETER = 10;
	private static final int MAX_DIAMETER = 25;
	private static final int GAZES_NUMBER = 4;
	private static final int MIN_DISTANCE = 30;

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

		lastNGazes = new CopyOnWriteArrayList<GazeData>();
	}

	private class GazeListener implements IGazeListener {
		@Override
		public void onGazeUpdate(GazeData gazeData) {
			if (recording) {
				saveData(gazeData);
			}
		}
	}

	private void add(GazeData gaze) {
		if (isLastFixated() || lastNGazes.size() == GAZES_NUMBER) {
			if (gaze.isFixated || tooClose(gaze))
				lastNGazes.remove(lastNGazes.size() - 1);
			else
				lastNGazes.remove(0);
		}
		lastNGazes.add(gaze);
		System.out.println("Added: " + gaze.smoothedCoordinates.x + " "
				+ gaze.smoothedCoordinates.y);
	}

	private boolean isLastFixated() {
		GazeData last = getLatest();
		return last != null && last.isFixated;
	}

	private GazeData getLatest() {
		int index = lastNGazes.size() - 1;
		return index >= 0 ? lastNGazes.get(index) : null;
	}

	private boolean tooClose(GazeData gaze) {
		GazeData last = getLatest();
		return Point.distance(last.smoothedCoordinates.x,
						last.smoothedCoordinates.y, gaze.smoothedCoordinates.x,
						gaze.smoothedCoordinates.y) <= MIN_DISTANCE;
	}

	public void addCurrentEyePosition(BufferedImage img) {
		marker = img.createGraphics();

		if (isFixed())
			fixationsCounter++;
		else
			fixationsCounter = 0;

		markLatestGazes(img);

		if (atLeastTwoGazes())
			markSaccadesPaths();
		marker.dispose();
	}

	private boolean isFixed() {
		GazeData gaze = getLatest();
		return gaze != null && gaze.isFixated;
	}

	private void markLatestGazes(BufferedImage img) {
		marker.setStroke(new BasicStroke(3));

		for (GazeData gaze : lastNGazes) {
			double x = gaze.smoothedCoordinates.x;
			double y = gaze.smoothedCoordinates.y;

			if (isLooking(gaze)) {
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
					marker.fillOval((int) x, (int) y, MAX_DIAMETER,
							MAX_DIAMETER);
			} else {
				setBorderOn(img);
			}
		}
	}

	private boolean isLooking(GazeData gaze) {
		return gaze != null
				&& (gaze.smoothedCoordinates.x > 0 || gaze.smoothedCoordinates.y > 0);
	}

	private boolean isLast(GazeData gaze) {
		return lastNGazes.indexOf(gaze) == lastNGazes.size() - 1;
	}

	private void setBorderOn(BufferedImage img) {
		marker.setColor(Color.RED);
		marker.drawRect(0, 0, img.getWidth() - 3, img.getHeight() - 3);
	}

	private boolean atLeastTwoGazes() {
		return lastNGazes.size() >= 2;
	}

	private void markSaccadesPaths() {
		marker.setColor(Color.BLUE);
		marker.setStroke(new BasicStroke(2));

		for (int i = 0; i < lastNGazes.size() - 1; i++) {
			GazeData gA = lastNGazes.get(i);
			GazeData gB = lastNGazes.get(i + 1);

			if (isLooking(gA) && isLooking(gB)) {
				marker.drawLine((int) gA.smoothedCoordinates.x + BASE_DIAMETER / 2,
						(int) gA.smoothedCoordinates.y + BASE_DIAMETER / 2,
						(int) gB.smoothedCoordinates.x + BASE_DIAMETER / 2,
						(int) gB.smoothedCoordinates.y + BASE_DIAMETER / 2);
			}
		}
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
		System.out.println("is fixed: " + gazeData.isFixated);
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
