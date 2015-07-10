package controllers;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;


public class MovieController {

    private static Dimension screenBounds;
    
    private IMediaWriter writer;
    
    private long recordingStartTime;
    
    public MovieController() {
    	
    	screenBounds = Toolkit.getDefaultToolkit().getScreenSize();
    }
    
    public void startRecording(String filename) {
    	
    	writer = ToolFactory.makeWriter(filename + ".mp4");
    	
    	// We tell it we're going to add one video stream, with id 0,
		// at position 0, and that it will have a fixed frame rate of FRAME_RATE.
    	writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, screenBounds.width, screenBounds.height);
    	
    	recordingStartTime = System.nanoTime();
    }

	public void encodeImage(BufferedImage img, long pausedTime) {
		
		// convert to the right image type
		BufferedImage bgrImage = convertToType(img, BufferedImage.TYPE_3BYTE_BGR);
					
		// encode the image to stream #0
		writer.encodeVideo(0, bgrImage, System.nanoTime() - recordingStartTime - pausedTime, TimeUnit.NANOSECONDS);
	}
	
	public void endRecording() {
		writer.close();
	}
	
	public static BufferedImage convertToType(BufferedImage sourceImage, int targetType) {

    	BufferedImage image;
	
    	// if the source image is already the target type, return the source image
    	if (sourceImage.getType() == targetType) {
    		image = sourceImage;
    	}
    	
    	// otherwise create a new image of the target type and draw the new image
    	
    	else {
    		image = new BufferedImage(sourceImage.getWidth(), 
    		sourceImage.getHeight(), targetType);
    		image.getGraphics().drawImage(sourceImage, 0, 0, null);
    	}
    	return image;
    }
}

