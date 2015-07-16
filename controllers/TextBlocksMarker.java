package controllers;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class TextBlocksMarker {
	private int width, height;
	private BufferedImage image;

	private TextBlocksMarker(BufferedImage source, int w, int h) {
		this.image = source;
		width = w;
		height = h;
	}
	
	public static TextBlocksMarker avgWordSize(BufferedImage source){
		return new TextBlocksMarker(source, 5, 5);
	}
	
	public static TextBlocksMarker sentenceSize(BufferedImage source){
		return new TextBlocksMarker(source, 9, 2);
	}
	
	public static TextBlocksMarker smallWindowSize(BufferedImage source){
		return new TextBlocksMarker(source, 14, 13);
	}

	public BufferedImage getMarkedImage() {
		List<Rect> boxes = new ArrayList<>();
		Mat matImg = matify(image);
		boxes = detect(matImg);
		markImage(boxes);

		return image;
	}

	public void setArea(int w, int h) {
		width = w;
		height = h;
	}

	private Mat matify(BufferedImage img) {
		byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer())
				.getData();

		Mat image = new Mat(img.getHeight(), img.getWidth(), CvType.CV_8UC3);
		image.put(0, 0, pixels);

		return image;
	}

	public List<Rect> detect(Mat img) {
		List<Rect> boundRect = new ArrayList<>();
		Mat imgGray = new Mat();
		Mat imgSobel = new Mat();
		Mat imgThreshold = new Mat();
		Mat element = new Mat();
		Mat hierarchy = new Mat();

		Imgproc.cvtColor(img, imgGray, Imgproc.COLOR_RGB2GRAY);
		Imgproc.Sobel(imgGray, imgSobel, CvType.CV_8U, 1, 0, 3, 1, 0,
				Core.BORDER_DEFAULT);
		Imgproc.threshold(imgSobel, imgThreshold, 0, 255, Imgproc.THRESH_OTSU
				+ Imgproc.THRESH_BINARY);
		element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(
				width, height));
		Imgproc.morphologyEx(imgThreshold, imgThreshold, Imgproc.MORPH_CLOSE,
				element);
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(imgThreshold, contours, hierarchy, 0, 1);

		for (int i = 0; i < contours.size(); i++) {
//			 System.out.println((contours.get(i).elemSize()));
			if (contours.get(i).elemSize() > 5) { //?
				MatOfPoint2f curve = new MatOfPoint2f(contours.get(i).toArray());
				MatOfPoint2f approx = new MatOfPoint2f();
				Imgproc.approxPolyDP(curve, approx, 3, true);
				Rect appRect = Imgproc.boundingRect(new MatOfPoint(approx
						.toArray()));
				if (appRect.width > appRect.height)
					boundRect.add(appRect);
			}
		}

		return boundRect;
	}

	private void markImage(List<Rect> rect) {
		Graphics2D g = image.createGraphics();
		g.setColor(Color.RED);

		for (Rect r : rect)
			g.drawRect(r.x, r.y, r.width, r.height);

		g.dispose();
	}

	public static BufferedImage toBufferedImageOfType(BufferedImage original,
			int type) {
		if (original == null) {
			throw new IllegalArgumentException("original == null");
		}

		if (original.getType() == type) {
			return original;
		}

		BufferedImage image = new BufferedImage(original.getWidth(),
				original.getHeight(), type);

		Graphics2D g = image.createGraphics();
		try {
			g.setComposite(AlphaComposite.Src);
			g.drawImage(original, 0, 0, null);
		} finally {
			g.dispose();
		}

		return image;
	}

}
