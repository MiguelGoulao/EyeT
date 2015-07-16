package controllers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.theeyetribe.client.data.GazeData;

public class ImageEditor {
	
	private DataController gc;
	
	private int circleStrokeWidth;
	private int pathStrokeWidth;
	private int baseDiameter;
	private int maxDiameter;
	
	private BufferedImage cursor;
	private BufferedImage cursorPressed;
	
	private Graphics2D g2d;
	
	public ImageEditor(DataController gazeController) {
		this.gc = gazeController;
		loadResources();
		initializeVariables();
	}
	
	private void loadResources() {
		try {
			cursor = ImageIO.read(getClass().getResource("/resources/cursor.png"));
			cursorPressed = ImageIO.read(getClass().getResource("/resources/cursor2.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void initializeVariables() { 
		//TODO: make it customizable in gui
		circleStrokeWidth = 3;
		pathStrokeWidth = 2;
		baseDiameter = 10;
		maxDiameter = 100;
	}
	
	public void addCursor(BufferedImage img) {

		int x = MouseInfo.getPointerInfo().getLocation().x;
		int y = MouseInfo.getPointerInfo().getLocation().y;

		if(gc.isMousePressed()) {
			g2d.drawImage(cursorPressed, x, y, 32, 32, null);
		}
		else{
			g2d.drawImage(cursor, x, y, 16, 16, null);
		}
		
	}
	
	public void addCurrentEyePosition(BufferedImage img) {
		g2d = img.createGraphics();

		markLatestGazes(img);

		if (gc.atLeastTwoGazes())
			markSaccadesPaths();
		
		addCursor(img);
		
		g2d.dispose();
	}
	
	private void markLatestGazes(BufferedImage img) {
		g2d.setStroke(new BasicStroke(circleStrokeWidth));

		for (GazeData gaze : gc.getGazeHistory()) {

			if (gc.isLooking(gaze)) {
				drawGaze(gaze, g2d);
			} 
			else {
				setBorderOn(img);
			}
		}
	}
	
	private void drawGaze(GazeData gaze, Graphics2D g2d) {
		double x = gaze.smoothedCoordinates.x;
		double y = gaze.smoothedCoordinates.y;
		int size = 0;
		int shift = baseDiameter / 2;
		
		if (gc.isLast(gaze)) {
			g2d.setColor(Color.GREEN);
			shift = getFixationCircleGrowth() / 2;
			size = getFixationCircleGrowth();
		} else {
			size = baseDiameter;
			g2d.setColor(Color.RED);
		}
		
		x -= shift;
		y -= shift;

		if (size <= maxDiameter)
			g2d.drawOval((int) x, (int) y, size, size);
		else
			g2d.fillOval((int) x, (int) y, maxDiameter, maxDiameter);
	}
	
	private void markSaccadesPaths() {
		g2d.setColor(Color.BLUE);
		g2d.setStroke(new BasicStroke(pathStrokeWidth));

		for (int i = 0; i < gc.getGazeHistory().size() - 1; i++) {
			
			GazeData startGaze = gc.getGazeHistory().get(i);
			GazeData endGaze = gc.getGazeHistory().get(i + 1);

			if (gc.isLooking(startGaze) && gc.isLooking(endGaze)) {
				
				g2d.drawLine((int) startGaze.smoothedCoordinates.x ,
						(int) startGaze.smoothedCoordinates.y,
						(int) endGaze.smoothedCoordinates.x,
						(int) endGaze.smoothedCoordinates.y);
			}
		}
	}
	
	private int getFixationCircleGrowth() {
		return Math.min(maxDiameter, baseDiameter + (int) (gc.lastFixationLength() / 10));
	}
	
	private void setBorderOn(BufferedImage img) {
		g2d.setColor(Color.RED);
		g2d.drawRect(0, 0, img.getWidth() - 3, img.getHeight() - 3);
	}
}
