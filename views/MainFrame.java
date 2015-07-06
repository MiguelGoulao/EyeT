package views;

import java.awt.BorderLayout;

import javax.swing.JFrame;

public class MainFrame extends JFrame{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MainFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setTitle("Eye Tracking");
        setSize(800, 600);
        setLocation(0, 0);
	}

}
