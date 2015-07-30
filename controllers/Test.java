package controllers;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Tesseract1;
import net.sourceforge.tess4j.TesseractException;

import org.opencv.core.Core;

import views.MainFrame;

public class Test {

	public static void main(String[] args) {

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		BufferedImage img = null;
		try {
			img = ImageIO.read(new File(
					"E:\\kod2.png"));
		} catch (IOException e) {
		}

		BufferedImage customImg = TextBlocksMarker.toBufferedImageOfType(img,
				BufferedImage.TYPE_3BYTE_BGR);

		TextBlocksMarker detection = TextBlocksMarker.sentenceSize(customImg);
		
		Tesseract instance = new Tesseract(); // JNA Interface Mapping
//        Tesseract1 instance = new Tesseract1(); // JNA Direct Mapping

	     try {
	         String result = instance.doOCR(img);
	         System.out.println(result);
	     } catch (TesseractException e) {
	         System.err.println(e.getMessage());
	     }

		MainFrame mf = new MainFrame();
		mf.setVisible(true);
		ImagePanel i = new ImagePanel();
		i.setImage(detection.getMarkedImage());
		mf.add(i);
		mf.pack();
	}
}