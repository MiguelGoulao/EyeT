package controllers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileController {
	
	public static boolean canWrite(File directory) {
		try{
			File testFile = new File(directory + "test.txt");
			FileWriter fw = new FileWriter(testFile);
			fw.write("test");
			fw.close();
			testFile.delete();
			return true;
		}
		catch(IOException e) {
			return false;
		}
		
	}
}
