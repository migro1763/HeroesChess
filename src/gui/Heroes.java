package gui;

import interfaces.Declarations;

import java.awt.Graphics;
//import java.awt.Graphics2D;
import java.awt.Image;
//import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Heroes extends JFrame implements Declarations, KeyListener {

	private static final long serialVersionUID = 1L;
	BufferedImage sprite;
    ArrayList<Animator> animSprites = new ArrayList<Animator>();

    SpriteSheet ss;

	Image dbImage;
    Graphics dbg;
    Reader reader = new Reader();
    ArrayList<Unit> data;

    double scale = 1.0;

    int speed = 150; // smaller = faster
    int whichUnit = 0;
    int whichState = 0;
    boolean playing = true;

    public Heroes() {
		super();
        setSize(800, 600);
        setVisible(true);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        addKeyListener(this);

        init();
    }

    private void init() {
        BufferedImageLoader loader = new BufferedImageLoader();
        BufferedImage spriteSheet = null;

        try {
			// load all units as arraylist of Unit objects
			data = reader.readData(IMG_PATH + "heroes/heroesFrames_sheet.txt");
			// load spritesheet image
            spriteSheet = loader.loadImage("file:" + IMG_PATH + "heroes/heroesFrames_sheet.png");
        } catch (IOException exc) {}

        ss = new SpriteSheet(spriteSheet);

        reinit(true);
    }

    public void reinit(boolean changeUnit) {

		if(changeUnit) {
			// create 4 Animator objects, one per state, feed into arraylist
			animSprites = new ArrayList<Animator>();
			for (int i = 0; i < 4; i++) {
				Animator tmpAnim = new Animator(getSprites(ss, whichUnit, i));
				tmpAnim.setUnit(findUnit(whichUnit, whichState));
				animSprites.add(tmpAnim);
				
			}
		}

		System.out.println("Current sprite: " + UNITS[whichUnit] + "_" + STATES[whichState]);

		animSprites.get(whichState).setSpeed(speed);
		if(playing)
			animSprites.get(whichState).play();
		else
			animSprites.get(whichState).pause();
	}

    public ArrayList<BufferedImage> getSprites(SpriteSheet ss, int unitNbr, int stateNbr) {
		ArrayList<BufferedImage> sprites = new ArrayList<BufferedImage>();
		Unit currentUnit = findUnit(unitNbr, stateNbr);

		System.out.println("Current unit: " + currentUnit);

		if(currentUnit != null) {
			for (int i = 0; i < currentUnit.frames; i++) {
				sprites.add(ss.grabSprite(currentUnit.getFrm(i).x, currentUnit.getFrm(i).y,
											currentUnit.getFrm(i).w, currentUnit.getFrm(i).h));
				
			}
		}
        return sprites;
	}
    
    public Unit findUnit(int unitNbr, int stateNbr) {
		for (Unit unit : data) {
			if(unit.toString().equals(UNITS[unitNbr] + "_" + STATES[stateNbr])) {
				return unit;
			}
		}
		return null;
    }

	@Override
    public void paint(Graphics g) {
        dbImage = createImage(getWidth(), getHeight());
        dbg = dbImage.getGraphics();
        paintComponent(dbg);
        g.drawImage(dbImage, 0, 0, null);
    }

    public void paintComponent(Graphics g) {
		if(!animSprites.isEmpty())
			if(animSprites.get(whichState) != null) {
				animSprites.get(whichState).update(System.currentTimeMillis());
				g.drawImage(animSprites.get(whichState).sprite, 60, 60, 
							(int)(animSprites.get(whichState).unit.getFrm(0).w * scale),
							(int)(animSprites.get(whichState).unit.getFrm(0).h * scale), null);
			}
        repaint();
    }

    public static void main(String[] args) {
        new Heroes();
    }

    @Override
    public void keyPressed(KeyEvent e) {

        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            System.out.println("Cycle units forwards");
            whichUnit++;
            if(whichUnit > 11)
            	whichUnit = 0;
            reinit(true);
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            System.out.println("Cycle units backwards");
			whichUnit--;
			if(whichUnit < 0)
            	whichUnit = 11;
            reinit(true);
        }

		if (e.getKeyCode() == KeyEvent.VK_UP) {
			System.out.println("Cycle states forwards");
			whichState++;
			if(whichState > 3)
            	whichState = 0;
			reinit(true);
		}
		if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			System.out.println("Cycle states backwards");
			whichState--;
			if(whichState < 0)
            	whichState = 3;
			reinit(true);
        }
		if (e.getKeyCode() == KeyEvent.VK_PLUS) {
			System.out.println("Increase speed, now: " + speed);
			speed -= 10;
			if(speed < 0)
            	speed = 0;
			reinit(false);
        }
		if (e.getKeyCode() == KeyEvent.VK_MINUS) {
			System.out.println("Decrease speed, now: " + speed);
			speed += 10;
			reinit(false);
        }
		if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			playing = !playing;
			System.out.println(((playing) ? "Play" : "Pause"));
			reinit(false);
        }
		if (e.getKeyCode() == KeyEvent.VK_PERIOD) {
			scale += 0.02;
			System.out.println("Scaling up, now: " + scale);
			reinit(false);
        }
		if (e.getKeyCode() == KeyEvent.VK_COMMA) {
			scale -= 0.02;
			if(scale < 0.02)
				scale = 0.02;
			System.out.println("Scaling down, now: " + scale);
			reinit(false);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

	@Override
    public void keyTyped(KeyEvent e) {}

}