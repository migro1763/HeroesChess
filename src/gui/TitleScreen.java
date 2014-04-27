package gui;

import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import listeners.WindowMoverListener;
import interfaces.GuiParams;
import interfaces.Vals;

public class TitleScreen extends JPanel implements Runnable, MouseListener, GuiParams, Vals {
	
	private static final long serialVersionUID = 1L;
	
	public static final int[][] BUTTON_COORDS = {	{490, 200, 64, 64}, // new game
													{200, 190, 71, 58}, // load game
													{415, 110, 72, 59}, // high scores
													{300, 140, 80, 32}, // credits
													{10, 416, 75, 54}	// quit
												};
	
	private static final String bgImagePath = "/img/backgrounds/";
	private static final String bgImage = "h2chess_title.png";
	private static final String BUTTONS_FOLDER = "buttons";
	private static final String[] BUTTON_PATHS = {	"newGame.png",
													"loadGame.png",
													"highScores.png",
													"credits.png",
													"quitGame.png"
												};	
	private static final String[] MENU_BUTTON_PATHS = {	"playerSelection.png",
														"colourSelection.png",
														"networkSelection.png"
													};
	private static final String[] MENU_IMAGE_PATHS = {	"menuFrame.png",
														"enterHandleWindow.png",
														"hostingGameAtText.png"
													};
	
	private Image imgBackground;
	private List<BufferedImage> buttonImages = new ArrayList<BufferedImage>();
	private List<BufferedImage> menuButtonImages = new ArrayList<BufferedImage>();
	private List<BufferedImage> menuImages = new ArrayList<BufferedImage>();
	private static Point mouseDownCompCoords;
//	private HeroesFrame frame;
	private HeroesDialog dialog;
	private WindowMoverListener winMoverListener, menuMoverListener;
	private int choice = -1, choiceOut = choice;
	private int playerSelect = -1, colourSelect = -1, networkSelect = -1;
	private String text = "";
    
    public TitleScreen(HeroesFrame frame) {
    	this.setLayout(null);
		// load graphics into buffers/memory
		loadGraphics();	
		// create application frame, menubar height = +23
		frame.setContentPane(this);
//		frame = new HeroesFrame(imgBackground.getWidth(null), imgBackground.getHeight(null));
//        frame.setContentPane(desktop);
		winMoverListener = new WindowMoverListener(frame, 0, 0);
		// configure title screen frame	
		frame.display(imgBackground.getWidth(null), imgBackground.getHeight(null));	
		
		// add mouse listeners to JPanel
		this.addMouseListener(this);
		this.addMouseListener(winMoverListener);
		this.addMouseMotionListener(winMoverListener);
	}
    
    @Override
    public void run() {		
		// keep going until interrupted
		try {
            while (!Thread.currentThread().isInterrupted()) {
            	Thread.sleep(1);
            }
        } catch (InterruptedException e) {
        	System.out.println("thread interrupted!");
        	Frame[] frames = Frame.getFrames();
        	for (Frame frame : frames)
        		frame.dispose();
            Thread.currentThread().interrupt();
            return;
        }
    }
    
    private void loadGraphics() {
    	// load background and menu image
    	BufferedImageLoader imgLoader = new BufferedImageLoader();
		this.imgBackground = imgLoader.loadImage(bgImagePath + bgImage);
		// load buttonImages
		for (String path : BUTTON_PATHS)
			buttonImages.add(imgLoader.loadImage(bgImagePath + BUTTONS_FOLDER + "/" + path));
		for (String path : MENU_BUTTON_PATHS)
			menuButtonImages.add(imgLoader.loadImage(bgImagePath + BUTTONS_FOLDER + "/" + path));
		for (String path : MENU_IMAGE_PATHS)
			menuImages.add(imgLoader.loadImage(bgImagePath + path));
    } 
	
	@Override
	protected void paintComponent(Graphics g) {
		// draw background
		g.drawImage(imgBackground, 0, 0, null);

		// draw current button, if any
		if(choice >= 0) {
			g.drawImage(buttonImages.get(choice), BUTTON_COORDS[choice][0],
					BUTTON_COORDS[choice][1], null);
			repaint();
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		mouseDownCompCoords = e.getPoint();
		Rectangle rect = null;
		for (int i = 0; i<BUTTON_COORDS.length; i++) {
			rect = new Rectangle(BUTTON_COORDS[i][0], BUTTON_COORDS[i][1],
					BUTTON_COORDS[i][2], BUTTON_COORDS[i][3]);
			if(rect.contains(mouseDownCompCoords)) {
				choice = i;
				break;
			}
		}
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		mouseDownCompCoords = null;
		switch (choice) {
			case 0:		newGame();
						break;
			case 1:		System.out.println(">> load game");
						break;
			case 2:		System.out.println(">> highscores");
						break;
			case 3:		System.out.println(">> credits");
						break;
			case 4:		System.out.println(">> quit");
						break;
			default:	break;
		}
		choiceOut = choice;
		choice = -1;
		repaint();
	}
	
	public void newGame() {
		dialog = new HeroesDialog(menuImages.get(0));
		menuMoverListener = new WindowMoverListener(dialog);
		listenerUpdate(true); // turn off listeners for title, on for menu
		dialog.setFocusable(true);
		dialog.setAutoRequestFocus(true);
		dialog.addImage(menuButtonImages.get(0)); // add player select buttons
		dialog.display();
		playerSelect = dialog.getChoice();
		System.out.println("playerSelect: " + playerSelect);
		if(playerSelect < 3) {
			dialog.flushImages(); // remove previously added buttons
			dialog.addImage(menuButtonImages.get(1)); // add colour select buttons
			dialog.display();
			colourSelect = dialog.getChoice();
			System.out.println("colourSelect: " + colourSelect);
			if(playerSelect == 2 && colourSelect < 3) { // if two players
				dialog.flushImages();
				dialog.addImage(menuButtonImages.get(2)); // add network select buttons
				dialog.display();
				networkSelect = dialog.getChoice();
				System.out.println("networkSelect: " + networkSelect);
				
				// open new dialog gui for entering password
				while (text.isEmpty()) {
					dialog = new HeroesDialog(menuImages.get(1));
					menuMoverListener = new WindowMoverListener(dialog);
					listenerUpdate(true);
					dialog.setIsMenu(false);
					dialog.display();
					text = dialog.getText();
				}
			}
		}
		listenerUpdate(false); // turn on listeners for title, off for menu
	}
	
	public void listenerUpdate(boolean focusMenu) {
		if(focusMenu) {
	 		removeMouseListener(winMoverListener);
			removeMouseMotionListener(winMoverListener);
			dialog.addMouseMotionListener(menuMoverListener);
		} else {
			dialog.removeMouseMotionListener(menuMoverListener);
			addMouseListener(winMoverListener);
			addMouseMotionListener(winMoverListener);
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

	public int getPlayerSelect() {
		return playerSelect;
	}

	public int getColourSelect() {
		return colourSelect;
	}

	public int getNetworkSelect() {
		return networkSelect;
	}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}
}