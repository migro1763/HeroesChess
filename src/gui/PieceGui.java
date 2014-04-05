package gui;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import interfaces.Declarations;
import pieces.Piece;

public class PieceGui extends Piece implements Declarations {
	
	// animation states
	private int state = STATE_IDLE;
	
	private ArrayList<Animator> anim;
	private int x;
	private int y;
	private long speed = 150;
	private volatile boolean running = false;

	public PieceGui(Piece piece, ArrayList<Animator> anim) {
		super(piece.getColour(), piece.getType(), piece.getPos());
		this.anim = anim;
		snapToNearestSquare();
	}
	
	// clone constructor
	public PieceGui(PieceGui clone) {
		super(clone.colour, clone.type, clone.pos);
		this.anim = clone.anim;
		this.state = clone.state;
		snapToNearestSquare();
	}
	
	public BufferedImage getImage(int state) {
		return anim.get(state).sprite;
	}

	public Animator getAnim(int state) {
		return anim.get(state);
	}
	
	public ArrayList<Animator> getAllAnim() {
		return anim;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}
	
	@Override
	public void setPos(int pos) {
		this.pos = pos;
		snapToNearestSquare();
	}

	public int getWidth() {
		return anim.get(state).sprite.getWidth(null);
	}

	public int getHeight() {
		return anim.get(state).sprite.getHeight(null);
	}

	// snap the guiPiece back to the coordinates that correspond with 
	// the underlying piece's row and column
	public void snapToNearestSquare(int pos, boolean isAttack, boolean isAttacker) {
		int offset = 0;
		if(isAttack)
			offset = isAttacker ? -SQUARE_WIDTH>>1 : SQUARE_WIDTH>>1;		
		x = ChessBoardGui.convertColumnToX(pos%8) + offset;
		y = ChessBoardGui.convertRowToY(pos/8);
	}
	
	public void snapToNearestSquare(boolean isAttack, boolean isAttacker) {
		snapToNearestSquare(pos, isAttack, isAttacker);
	}
	
	public void snapToNearestSquare(int pos) {
		snapToNearestSquare(pos, false, false);
	}
	
	public void snapToNearestSquare() {
		snapToNearestSquare(pos, false, false);
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
