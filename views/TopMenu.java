package views;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import controllers.FileController;
import controllers.ScreenCaptureController;

public class TopMenu extends JMenuBar implements ActionListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ScreenCaptureController controller;
	private JMenu fileMenu;
	private JMenuItem loadMenuItem;
	
	private JFileChooser fc;
	
	public TopMenu(ScreenCaptureController controller) {
		
		this.controller = controller;
		
		buildMenus();
		
		createFileChooser(controller.getWorkingDirectory());
	}
	
	private void createFileChooser(String currentDirectory) {
		
		File dir;
		if(currentDirectory == null) {
			dir = new File(System.getProperty("user.dir"));
		}
		else {
			dir = new File(currentDirectory);
		}
		fc = new JFileChooser(dir);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setAcceptAllFileFilterUsed(false);
	}
	
	private void buildMenus(){
		
		fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_1);
		
		//Add load menu item
		loadMenuItem = new JMenuItem("Change work directory", KeyEvent.VK_D);
		loadMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
		loadMenuItem.getAccessibleContext().setAccessibleDescription("Open new image");
		loadMenuItem.addActionListener(this);
		fileMenu.add(loadMenuItem);
		
		add(fileMenu);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		Object src = e.getSource();	
		
		if(src == loadMenuItem) {
			chooseDirectory();
		}
	}
	
	private void chooseDirectory(){
		
		int returnVal = fc.showOpenDialog(this);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
            File folder = fc.getSelectedFile();
            if(FileController.canWrite(folder)) {
            	controller.setWorkingDirectory(folder.toString());
            	fc.setCurrentDirectory(folder);
            }
            else {
            	controller.badDirectory();
            }
            
            
		}
	}

}
