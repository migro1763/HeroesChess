package gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.util.List;
import java.util.ArrayList;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JDialog;
import javax.swing.JPanel;

public class HeroesDialog extends JDialog implements MouseListener {
		
	private static final long serialVersionUID = 1L;
	
	public static final int[][] BUTTON_COORDS = {	{0, 0, 120, 50}, 	// main window
													{33, 35, 120, 50}, 	// button 0
													{33, 107, 120, 50}, // button 1
													{33, 371, 120, 50} 	// button 2
												};
	
	public static final int[][] HANDLE_COORDS = {	{98, 193, 94, 24}, 	// button 0
													{33, 124, 225, 15}, // input field
													{19, 62, 0, 0} 		// hosting at game text image
												};
	
	private static final Font GAME_FONT = new Font("DejaVu Sans Bold", Font.PLAIN, 14);
	
	private int w, h, imgX, imgY, choice = -1, choiceOut = choice;
	static Point mouseDownCompCoords;
	private Point mouseDownCoords;
	private List<Image> images = new ArrayList<Image>();
	private PaintPanel panel = new PaintPanel();
	private boolean isMenu = true;
	private String text = "";

	public HeroesDialog(Image image) {
		images.clear();
		images.add(image);
		w = image.getWidth(null);
		h = image.getHeight(null);
        this.setUndecorated(true);
        this.setModal(true);
        this.setBackground(new Color(0,0,0,0));
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        // panel and image settings
		this.setContentPane(panel);
		imgX = w/2 - panel.getWidth();
		imgY = h/2 - panel.getHeight();
        panel.setLocation(imgX, imgY);
        panel.setSize(w, h);
        panel.setBackground(new Color(0,0,0,0));
        panel.setFocusable(true);
        panel.requestFocusInWindow();
        this.addMouseListener(this);
	}
	
	public void display() {
		this.pack();
		this.setSize(w, h);
        this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	public void setImage(Image image) {
		images.set(0, image);
		w = image.getWidth(null);
		h = image.getHeight(null);
		panel.setBackground(new Color(0,0,0,0));
		repaint();
	}
	
	public void addImage(Image image) {
		images.add(image);
	}
	
	public void flushImages() {
		for (int i = 1; i < images.size(); i++)
			images.remove(i);
	}
	
	public JPanel getPanel() {
		return panel;
	}

	public void panelRepaint() {
		panel.repaint();
	}
	
	public void setButtonCoord(int index, int x, int y, int width, int height) {
		BUTTON_COORDS[index][0] = x;
		BUTTON_COORDS[index][1] = y;
		BUTTON_COORDS[index][2] = width;
		BUTTON_COORDS[index][3] = height;
	}
	
	public void setButtonCoord(int index, int x, int y) {
		setButtonCoord(index, x, y, BUTTON_COORDS[index][2], BUTTON_COORDS[index][3]);
	}
	
	public int getDialogWidth() {
		return w;
	}
	
	public int getDialogHeight() {
		return h;
	}	
	
	public int[][] getCurrentCoords() {
		return isMenu ? BUTTON_COORDS : HANDLE_COORDS;
	}
	
	public void setIsMenu(boolean isMenu) {
		this.isMenu = isMenu;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		mouseDownCompCoords = e.getPoint();
		mouseDownCoords = mouseDownCompCoords;
		Rectangle rect = null;
		int[][] currentCoords = getCurrentCoords();
		
		for (int i = isMenu?1:0; i < currentCoords.length-(isMenu?0:2); i++) {
			rect = new Rectangle(currentCoords[i][0], currentCoords[i][1],
								currentCoords[i][2], currentCoords[i][3]);
			if(rect.contains(mouseDownCompCoords)) {
				choice = i;
				break;
			}
		}		
	}

	public Point getMouseDownCoords() {
		return mouseDownCoords;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		mouseDownCompCoords = null;
		choiceOut = choice;
		choice = -1;
		try {
			Thread.sleep(40);
		} catch (InterruptedException e1) {}
		if(choiceOut >= 0) {
			flushImages();
			dispose();
		}
	}
	
	public int getChoice() {
		int choice = choiceOut;
		choiceOut = -1;
		return choice;
	}
	
	public String getText() {
		return text;
	}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}
	
	class PaintPanel extends JPanel implements KeyListener {

		private static final long serialVersionUID = 1L;
		
		int textX = HANDLE_COORDS[1][0], textY = HANDLE_COORDS[1][1];
		
		public PaintPanel() {
			addKeyListener(this);
		}

		@Override
	    public void paintComponent(Graphics g) {
			super.paintComponent(g);
    		for (int i = 0; i < images.size(); i++)
    			g.drawImage(images.get(i), BUTTON_COORDS[i][0], BUTTON_COORDS[i][1], null);
    			g.setFont(GAME_FONT);
    			g.setColor(Color.WHITE);
    			g.drawString(text, textX, textY);
	    }
		
		@Override
		public void keyTyped( KeyEvent e ) {
			if(!isMenu) {
				char c = e.getKeyChar(); 
				if ( c != KeyEvent.CHAR_UNDEFINED ) {
					if(c == KeyEvent.VK_BACK_SPACE && text.length() > 0)
						text = text.substring(0, text.length()-1);
					else if(getText().length() < 24)
						text += c;
					repaint(); 
					e.consume();
				}
			}
		}
		
		@Override
		public void keyPressed(KeyEvent e) {}

		@Override
		public void keyReleased(KeyEvent e) {}
	}
}
