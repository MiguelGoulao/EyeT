package controllers;

import org.opencv.core.Core;
import org.opencv.videoio.VideoCapture;

public class MovieController {
	
	public MovieController() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		VideoCapture capture = new VideoCapture("new.avi");
		//capture.open("new.avi");

		System.out.println(capture.isOpened());
	}
}
