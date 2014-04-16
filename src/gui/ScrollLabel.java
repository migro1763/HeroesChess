package gui;

import interfaces.Vals;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class ScrollLabel extends JLabel {

	private static final long serialVersionUID = 1L;
	
	public ScrollLabel(String text, int x, int y, int width, int height) {
		super(text, SwingConstants.LEADING);
		this.setBounds(x, y, width, height);
		this.setPreferredSize(new Dimension(width, height));
		this.setForeground(Color.YELLOW);
		this.setFont(Vals.GAME_FONT);
		this.setVerticalAlignment(JLabel.BOTTOM);
		this.setVerticalTextPosition(JLabel.BOTTOM);
		this.setAlignmentY(BOTTOM_ALIGNMENT);
		this.setOpaque(false);
		this.setLocation(x, y);
	}
	
	public void setTextColour(Color colour) {
		this.setForeground(colour);
	}
}
