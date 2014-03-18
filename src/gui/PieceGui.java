package gui;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import interfaces.Declarations;
import pieces.Piece;

public class PieceGui extends Piece implements Declarations {
	
	// animation states
	private int state = 0;
	
	private BufferedImage img;
	private ArrayList<Animator> anim;
	private int x;
	private int y;
	private int prevX = 0;
	private int prevY = 0;
	
	private long previousTime;
	private long speed = 150;
	private volatile boolean running = false;

	public PieceGui(Piece piece, ArrayList<Animator> anim) {
		super(piece.getColour(), piece.getType(), piece.getPos(), piece.getId());
		this.anim = anim;
		snapToNearestSquare();
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
		prevX = this.x;
		this.x = x;
	}

	public void setY(int y) {
		prevY = this.y;
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
		return anim.get(state).sprite.getWidth(null);
	}

	public int getHeight() {
		return anim.get(state).sprite.getHeight(null);
	}

	/**
	 * snap the guiPiece back to the coordinates that
	 * correspond with the underlying piece's row and column
	 */
	public void snapToNearestSquare() {
		x = ChessBoardGui.convertColumnToX(getColumn());
		y = ChessBoardGui.convertRowToY(getRow());
	}
	
	// unfinished!
	public void update(long time) {
        if(isRunning()) {
        	int currentX = x;
            if(time - previousTime >= speed) {
                //Update the animation
                if(currentX < x) {
                	x++;
               	} else {
               		currentX = 0;
				}
				x = currentX;

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
