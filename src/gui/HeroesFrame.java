package gui;

import javax.swing.JFrame;

public class HeroesFrame extends JFrame {
	
	private static final long serialVersionUID = 1L;
	private int w = 0, h = 0;

	public HeroesFrame(int width, int height) {
		w = width;
		h = height;
        this.setUndecorated(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public HeroesFrame() {
        this.setUndecorated(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public void display(int w, int h) {
		this.pack();
		this.setSize(w, h);
        this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	public void display() {
		display(w, h);
	}

	public int getFrameWidth() {
		return w;
	}

	public void setFrameWidth(int width) {
		w = width;
	}
	
	public int getFrameHeight() {
		return h;
	}

	public void setFrameHeight(int height) {
		h = height;
	}
}
