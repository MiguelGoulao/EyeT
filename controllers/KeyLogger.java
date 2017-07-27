package controllers;

import java.util.LinkedList;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;

public class KeyLogger implements NativeKeyListener, NativeMouseInputListener{
	
	private LinkedList<String> lastOperations;
	private int numOfButtonsPressed;
	
	public KeyLogger() {
		numOfButtonsPressed = 0;
		
		lastOperations = new LinkedList<>();
	//	registerNativeHook();
		
	}
	
	private void registerNativeHook() {
		try {
            /* Register jNativeHook */
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            /* Its error */
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());
            System.exit(1);
        }
	}
	
	public void startRecording() {
		GlobalScreen.addNativeKeyListener(this);
        GlobalScreen.addNativeMouseListener(this);
	}
	
	public void endRecording() {
		GlobalScreen.removeNativeKeyListener(this);
		GlobalScreen.removeNativeMouseListener(this);
	}
	
	public LinkedList<String> getLastOperations() {
			
		LinkedList<String> clone = (LinkedList<String>) lastOperations.clone();
		lastOperations.clear();
		return clone;
	}
	
	public boolean isMousePressed() {
		return numOfButtonsPressed > 0;
	}

	@Override
	public void nativeKeyPressed(NativeKeyEvent e) {
		lastOperations.add("Key Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode())); 
		
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent e) {
		lastOperations.add("Key Released: " + NativeKeyEvent.getKeyText(e.getKeyCode())); 
		
	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nativeMouseClicked(NativeMouseEvent e) {
		lastOperations.add("Button " + e.getButton() + " clicked " + e.getClickCount() + " times");
	}

	@Override
	public void nativeMousePressed(NativeMouseEvent e) {
		lastOperations.add("Button " + e.getButton() + " pressed");
		numOfButtonsPressed++;
	}

	@Override
	public void nativeMouseReleased(NativeMouseEvent e) {
		lastOperations.add("Button " + e.getButton() + " released");
		numOfButtonsPressed--;
	}

	@Override
	public void nativeMouseDragged(NativeMouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nativeMouseMoved(NativeMouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
