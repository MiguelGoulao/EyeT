package controllers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.theeyetribe.client.data.GazeData;

public class ImageEditor {

	private DataController dc;
	private int circleStrokeWidth;
	private int pathStrokeWidth;
	private int baseDiameter;
	private int maxDiameter;

	private BufferedImage cursor;
	private BufferedImage cursorPressed;

	private Graphics2D g2d;

	public ImageEditor(DataController gazeController) {
		this.dc = gazeController;
		loadResources();
		initializeVariables();
	}

	private void loadResources() {
		try {
			cursor = ImageIO.read(getClass().getResource(
					"/resources/cursor.png"));
			cursorPressed = ImageIO.read(getClass().getResource(
					"/resources/cursor2.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initializeVariables() {
		// TODO: make it customizable in gui
		circleStrokeWidth = 3;
		pathStrokeWidth = 2;
		baseDiameter = 10;
		maxDiameter = 80;
	}

	public void addCursor(BufferedImage img) {

		int x = MouseInfo.getPointerInfo().getLocation().x;
		int y = MouseInfo.getPointerInfo().getLocation().y;

		if (dc.isMousePressed()) {
			g2d.drawImage(cursorPressed, x, y, 32, 32, null);
		} else {
			g2d.drawImage(cursor, x, y, 16, 16, null);
		}

	}

	public void addCurrentEyePosition(BufferedImage img) {
		g2d = img.createGraphics();

		addCursor(img);

		if (dc.hasAnyGazesInRange()) {
			System.out.println(dc.getGazeHistory().size());
			markLatestGazes(img);

			if (dc.atLeastTwoGazes())
				markSaccadesPaths();

		} else {
			setBorderOn(img);
		}

		g2d.dispose();
	}

	private void markLatestGazes(BufferedImage img) {
		g2d.setStroke(new BasicStroke(circleStrokeWidth));

		for (GazeData gaze : dc.getGazeHistory())
			drawGaze(gaze, g2d);
	}

	private void drawGaze(GazeData gaze, Graphics2D g2d) {
		double x = gaze.smoothedCoordinates.x;
		double y = gaze.smoothedCoordinates.y;
		int size = 0;
		if (dc.isLast(gaze)) {
			g2d.setColor(Color.GREEN);
			size = getFixationCircleGrowth();
		} else {
			size = baseDiameter;
			g2d.setColor(Color.RED);
		}
		int shift = size / 2;
		x = calcualteX(x) - shift;
		y = calcualteY(y) - shift;

		if (size <= maxDiameter)
			g2d.drawOval((int) x, (int) y, size, size);
		else
			g2d.fillOval((int) x, (int) y, maxDiameter, maxDiameter);

	}

	private void markSaccadesPaths() {
		g2d.setColor(Color.BLUE);
		g2d.setStroke(new BasicStroke(pathStrokeWidth));

		for (int i = 0; i < dc.getGazeHistory().size() - 1; i++) {
			GazeData startGaze = dc.getGazeHistory().get(i);
			GazeData endGaze = dc.getGazeHistory().get(i + 1);

			int startX = (int) calcualteX(startGaze.smoothedCoordinates.x);
			int startY = (int) calcualteY(startGaze.smoothedCoordinates.y);
			int endX = (int) calcualteX(endGaze.smoothedCoordinates.x);
			int endY = (int) calcualteY(endGaze.smoothedCoordinates.y);

			g2d.drawLine(startX, startY, endX, endY);

		}
	}

	private double calcualteX(double x) {
		if (dc.isInWidthRange(x))
			return x;

		return x < 0 ? 0 : dc.getScreenSize().getWidth();
	}

	private double calcualteY(double y) {
		if (dc.isInHeightRange(y))
			return y;

		return y < 0 ? 0 : dc.getScreenSize().getHeight();
	}

	private int getFixationCircleGrowth() {
		return Math.min(maxDiameter,
				baseDiameter + (int) (dc.lastFixationLength() / 100));
	}

	private void setBorderOn(BufferedImage img) {
		g2d.setColor(Color.RED);
		g2d.drawRect(0, 0, img.getWidth() - 3, img.getHeight() - 3);
	}
}
