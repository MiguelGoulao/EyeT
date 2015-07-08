package views;

import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;

import controllers.ScreenCaptureController;

public class ActionToolBar extends JToolBar implements ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ScreenCaptureController controller;
	
	private JButton recordButton;
	private JButton stopButton;

	public ActionToolBar(ScreenCaptureController controller) {
		this.controller = controller;
		
		addButtons();
	}
	
	private void addButtons() {
		
		recordButton = new JButton();
		stopButton = new JButton();
		
		try {
		    Image img = ImageIO.read(getClass().getResource("/resources/Record-32.png"));
		    recordButton.setIcon(new ImageIcon(img));
		    
		    img = ImageIO.read(getClass().getResource("/resources/Stop-32.png"));
		    stopButton.setIcon(new ImageIcon(img));

		} 
		catch (IOException ex) {
			ex.printStackTrace();
		}
		
		setButtonParams(recordButton);
		setButtonParams(stopButton);
		
		add(recordButton);
		add(stopButton);
	}
	
	private void setButtonParams(JButton button) {
		if (!System.getProperty("java.version").startsWith("1.3")) {
            button.setOpaque(false);
            button.setBackground(new java.awt.Color(0, 0, 0, 0));
        }
        button.setBorderPainted(false);
        button.setMargin(new Insets(2, 2, 2, 2));
        button.addActionListener(this);
	}
	
	private void changeRecordingButtonView() {
		try{
			if(controller.isCapturing()) {
				Image img = ImageIO.read(getClass().getResource("/resources/Pause-32.png"));
			    recordButton.setIcon(new ImageIcon(img));
			}
			else {
				Image img = ImageIO.read(getClass().getResource("/resources/Record-32.png"));
			    recordButton.setIcon(new ImageIcon(img));
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if(event.getSource() == recordButton) {
			if(controller.isCapturing()) {
				controller.pauseRecording();
			}
			else {
				controller.startRecording();
			}
		}
		else if(event.getSource() == stopButton) {
			controller.endRecording();
		}
		changeRecordingButtonView();
	}
}
