package controllers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.theeyetribe.client.GazeManager;
import com.theeyetribe.client.GazeManager.ApiVersion;
import com.theeyetribe.client.GazeManager.ClientMode;
import com.theeyetribe.client.IGazeListener;
import com.theeyetribe.client.data.GazeData;

public class DataController {

	private static final int GAZES_NUMBER = 4;
	private static final int MIN_DISTANCE = 40;
	private static final int MARGIN = 15;

	private List<GazeData> gazeHistory;
	private FileWriter outputFileWriter;
	private boolean recording;

	private long fixationStart;

	private KeyLogger keyLogger;

	public DataController() {
		final GazeManager gm = GazeManager.getInstance();
		gm.activate(ApiVersion.VERSION_1_0, ClientMode.PUSH);

		final GazeListener gazeListener = new GazeListener();
		gm.addGazeListener(gazeListener);

		keyLogger = new KeyLogger();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				gm.removeGazeListener(gazeListener);
				gm.deactivate();
			}
		});

		gazeHistory = new CopyOnWriteArrayList<GazeData>();
	}

	private class GazeListener implements IGazeListener {
		@Override
		public void onGazeUpdate(GazeData gazeData) {
			if (recording) {
				saveData(gazeData);
			}
		}
	}

	private void addGazeToHistory(GazeData gaze) {
		if (gazeHistory.size() == GAZES_NUMBER) {
			if (!gaze.isFixated || nearTheLastFixated(gaze)) {
				gazeHistory.remove(gazeHistory.size() - 1);
			} else {
				gazeHistory.remove(0);
				fixationStart = gaze.timeStamp;
			}
		}
		gazeHistory.add(gaze);
		System.out.println("Added: " + gaze.smoothedCoordinates.x + " "
				+ gaze.smoothedCoordinates.y);
	}

	boolean isLastFixated() {
		GazeData last = getLatest();
		return last != null && last.isFixated;
	}

	private GazeData getLatest() {
		int index = gazeHistory.size() - 1;
		return index >= 0 ? gazeHistory.get(index) : null;
	}

	private boolean nearTheLastFixated(GazeData gaze) {
		if (gazeHistory.size() > 2) {
			GazeData last = gazeHistory.get(gazeHistory.size() - 2);
			return Point.distance(last.smoothedCoordinates.x,
					last.smoothedCoordinates.y, gaze.smoothedCoordinates.x,
					gaze.smoothedCoordinates.y) <= MIN_DISTANCE;
		} else
			return false;
	}

	private boolean isLooking(GazeData gaze) {
		return gaze != null
				&& isInWidthRange(gaze.smoothedCoordinates.x, MARGIN)
				& isInHeightRange(gaze.smoothedCoordinates.y, MARGIN)
				&& gaze.smoothedCoordinates.x != 0
				&& gaze.smoothedCoordinates.y != 0;
	}

	boolean isInWidthRange(double x) {
		return isInWidthRange(x, 0);
	}

	private boolean isInWidthRange(double x, int margin) {
		return x + margin > 0 && x < getScreenSize().getWidth() + margin;
	}

	boolean isInHeightRange(double y) {
		return isInHeightRange(y, 0);
	}

	private boolean isInHeightRange(double y, int margin) {
		return y + margin > 0 && y < getScreenSize().getHeight() + margin;
	}

	boolean isLast(GazeData gaze) {
		return gazeHistory.indexOf(gaze) == gazeHistory.size() - 1;
	}

	boolean atLeastTwoGazes() {
		return gazeHistory.size() >= 2;
	}

	long lastFixationLength() {
		GazeData last = getLatest();
		if (last.isFixated) {
			return System.currentTimeMillis() - fixationStart;
		} else
			return 0;
	}

	boolean hasAnyGazesInRange() {
		return !getGazeHistory().isEmpty();
	}

	List<GazeData> getGazeHistory() {
		return gazeHistory;
	}

	Dimension getScreenSize() {
		return Toolkit.getDefaultToolkit().getScreenSize();
	}

	public boolean isMousePressed() {
		return keyLogger.isMousePressed();
	}

	public void startRecording(String filename) {
		keyLogger.startRecording();
		try {
			outputFileWriter = new FileWriter(filename + ".csv");

			outputFileWriter.append("Time Stamp");
			outputFileWriter.append(';');
			outputFileWriter.append("x");
			outputFileWriter.append(';');
			outputFileWriter.append("y");
			outputFileWriter.append(';');
			outputFileWriter.append("Mouse x");
			outputFileWriter.append(';');
			outputFileWriter.append("Mouse y");
			outputFileWriter.append(';');
			outputFileWriter.append("Keyboard and mouse events");
			outputFileWriter.append(';');
			outputFileWriter.append('\n');

			recording = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveData(GazeData gazeData) {
		if (isLooking(gazeData))
			addGazeToHistory(gazeData);
		else
			gazeHistory.clear();

		try {
			outputFileWriter.append(gazeData.timeStampString);
			outputFileWriter.append(';');
			outputFileWriter.append(Double
					.toString(gazeData.smoothedCoordinates.x));
			outputFileWriter.append(';');
			outputFileWriter.append(Double
					.toString(gazeData.smoothedCoordinates.y));
			outputFileWriter.append(';');
			outputFileWriter.append(Integer.toString(MouseInfo.getPointerInfo()
					.getLocation().x));
			outputFileWriter.append(';');
			outputFileWriter.append(Integer.toString(MouseInfo.getPointerInfo()
					.getLocation().y));
			outputFileWriter.append(';');
			saveKeyLoggerData();
			outputFileWriter.append('\n');
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveKeyLoggerData() {
		LinkedList<String> keyLoggerData = keyLogger.getLastOperations();
		Iterator<String> it = keyLoggerData.iterator();

		try {
			while (it.hasNext()) {
				outputFileWriter.append(it.next());
				if (it.hasNext()) {
					outputFileWriter.append(", ");
				}
			}
			outputFileWriter.append(';');
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void endRecording() {
		gazeHistory.clear();
		recording = false;
		keyLogger.endRecording();

		try {
			outputFileWriter.flush();
			outputFileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void pauseRecording() {
		recording = !recording;
		if (recording) {
			keyLogger.startRecording();
		} else {
			keyLogger.endRecording();
		}
	}
}
