package listeners;

import gui.HeroesDialog;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class WindowMoverListener implements MouseListener, MouseMotionListener {
	
	static Point mouseDownCompCoords;
	private Container window = null;
	private HeroesDialog dialog = null;
	private int offsetX = 0, offsetY = 0;
	
	public WindowMoverListener(Container window, int offsetX, int offsetY) {
		this.window = window;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}
	
	public WindowMoverListener(HeroesDialog dialog) {
		this.dialog = dialog;
	}
	
	public Container getWindow() {
		return window;
	}

	public void setWindow(Container window) {
		this.window = window;
	}
	
	public int getOffsetX() {
		return offsetX;
	}

	public void setOffsetX(int offsetX) {
		this.offsetX = offsetX;
	}
	
	public int getOffsetY() {
		return offsetY;
	}

	public void setOffsetY(int offsetY) {
		this.offsetY = offsetY;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		mouseDownCompCoords = e.getPoint();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		Point currCoords = e.getLocationOnScreen();
		
		// moves the entire window
		if(dialog != null) {
			mouseDownCompCoords = dialog.getMouseDownCoords();
			dialog.setLocation(currCoords.x - mouseDownCompCoords.x - offsetX, 
	        		currCoords.y - mouseDownCompCoords.y - offsetY);
		} else if(window != null)
			window.setLocation(currCoords.x - mouseDownCompCoords.x - offsetX, 
	        		currCoords.y - mouseDownCompCoords.y - offsetY);
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		mouseDownCompCoords = null;	
	}

	@Override
	public void mouseMoved(MouseEvent e) {}
	
	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

}
