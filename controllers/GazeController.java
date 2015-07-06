package controllers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.theeyetribe.client.GazeManager;
import com.theeyetribe.client.IGazeListener;
import com.theeyetribe.client.GazeManager.ApiVersion;
import com.theeyetribe.client.GazeManager.ClientMode;
import com.theeyetribe.client.data.GazeData;

public class GazeController {
	
	private FileWriter outputFileWriter;
	private boolean recording;
	private GazeData lastGaze;
	
	public GazeController() {
		final GazeManager gm = GazeManager.getInstance();
       	gm.activate(ApiVersion.VERSION_1_0, ClientMode.PUSH);
       	
       	startRecording();
        
        final GazeListener gazeListener = new GazeListener();
        gm.addGazeListener(gazeListener);

        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                gm.removeGazeListener(gazeListener);
                gm.deactivate();
            }
        });
	}
	
    private class GazeListener implements IGazeListener {

        @Override
        public void onGazeUpdate(GazeData gazeData)
        {
        	if(recording) {
        		saveData(gazeData);
        		System.out.println(gazeData.smoothedCoordinates.x + " " + gazeData.smoothedCoordinates.y);
        	}
        }
    }
    
    public void addCurrentEyePosition(BufferedImage img) {
		if(lastGaze != null) {
			Graphics2D g2d = img.createGraphics();
			g2d.setColor(Color.GREEN);
			g2d.fillOval((int) lastGaze.smoothedCoordinates.x, (int) lastGaze.smoothedCoordinates.x, 10, 10);
			g2d.dispose();
		}
    }
    
    public void startRecording() {
    	try
    	{
    		DateFormat dateFormat = new SimpleDateFormat("yyyy MM dd HH;mm;ss");
    		Date date = new Date();
    		
    		String outputFileName = "recording " + dateFormat.format(date) + ".csv";
    	    outputFileWriter = new FileWriter(outputFileName);  	    
    	    
    	    outputFileWriter.append("Time Stamp");
    	    outputFileWriter.append(';');
    	    outputFileWriter.append("x");
    	    outputFileWriter.append(';');
    	    outputFileWriter.append("y");
    	    outputFileWriter.append(';');
    	    outputFileWriter.append('\n');

    	    recording = true;
    	}
    	catch(IOException e) {
    	     e.printStackTrace();
    	} 
    }
    
    private void saveData(GazeData gazeData) {
        lastGaze = gazeData;
	    try {
			outputFileWriter.append(gazeData.timeStampString);
			outputFileWriter.append(';');
			outputFileWriter.append(Double.toString(gazeData.smoothedCoordinates.x));
    	    outputFileWriter.append(';');
		    outputFileWriter.append(Double.toString(gazeData.smoothedCoordinates.y));
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
