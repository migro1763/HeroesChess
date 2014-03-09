package gui;

//import java.awt.Image;
import game.BB;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import pieces.Piece;

public class GuiPiece {
	
	// animation states
	private int state = 0;
	public static final int STATE_IDLE = 0;
	public static final int STATE_WALK = 1;
	public static final int STATE_ATTACK = 2;
	public static final int STATE_DEATH = 3;
	
	private BufferedImage img;
	private ArrayList<Animator> anim;
	private int x;
	private int y;
	private int prevX = 0;
	private int prevY = 0;
	
	private long previousTime;
	private long speed = 150;
	private volatile boolean running = false;

	public GuiPiece(ArrayList<Animator> anim) {
		this.anim = anim;
		this.resetToUnderlyingPiecePosition();
	}
	
	public BufferedImage getImage(int state) {
		return anim.get(state).sprite;
	}

	public Animator getAnim(int state) {
		return anim.get(state);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void setX(int x) {
		this.prevX = this.x;
		this.x = x;
	}

	public void setY(int y) {
		this.prevY = this.y;
		this.y = y;
	}

	public int getPrevX() {
		return prevX;
	}

	public void setPrevX(int prevX) {
		this.prevX = prevX;
	}

	public int getPrevY() {
		return prevY;
	}

	public void setPrevY(int prevY) {
		this.prevY = prevY;
	}

	public int getWidth() {
		return anim.get(state).sprite.getHeight(null);
	}

	public int getHeight() {
		return anim.get(state).sprite.getHeight(null);
	}

	/**
	 * snap the guiPiece back to the coordinates that
	 * correspond with the underlying piece's row and column
	 */
	public void resetToUnderlyingPiecePosition() {
		this.x = ChessGui.convertColumnToX(piece.getColumn());
		this.y = ChessGui.convertRowToY(piece.getRow());
	}
	
	// unfinished!
	public void update(long time) {
        if(isRunning()) {
        	int currentX = this.x;
            if(time - previousTime >= speed) {
                //Update the animation
                if(currentX < this.x) {
                	this.x++;
               	} else {
               		currentX = 0;
				}
				this.x = currentX;

                previousTime = time;
            }
        }
    }

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public Piece getPiece() {
		return piece;
	}

	public boolean isCaptured() {
		return this.piece.isCaptured();
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public long getSpeed() {
		return speed;
	}

	public void setSpeed(long speed) {
		this.speed = speed;
	}
}
