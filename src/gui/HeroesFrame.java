package gui;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class HeroesFrame extends JFrame {
	
	private int width, height;
	
	private static final long serialVersionUID = 1L;

	public HeroesFrame(int width, int height) {
		this.width = width;
		this.height = height;
        this.setUndecorated(true);
        this.setBounds(0, 0, width, height);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public HeroesFrame() {
        this(1024, 712);
	}
	
	public void display(JComponent comp, int width, int height) {
		this.add(comp);
		this.pack();
        this.setSize(width, height);
		this.setVisible(true);
	}
	
	public void display(JComponent comp) {
		display(comp, width, height);
	}
}
