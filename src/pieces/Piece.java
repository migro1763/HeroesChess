package pieces;

import game.BB;
import game.Game;

public class Piece extends Movable {
		
	protected boolean isCaptured = false;
	protected char type;
	private BB moveBits;

	public Piece(int colour, char type, int pos) {
		super(colour, pos);
		this.type = type;
		this.moveBits = new BB(0L, colour);
	}

	public void isCaptured(boolean isCaptured) {
		this.isCaptured = isCaptured;
	}

	public boolean isCaptured() {
		return this.isCaptured;
	}

	public char getType() {
		return type;
	}

	public void setType(char type) {
		this.type = type;
	}

	public BB getMoveBits() {
		return moveBits;
	}

	public void setMoveBits(BB moveBits) {
		this.moveBits = moveBits;
	}
	
	// get unit nbr for animated sprite loading
	public int getUnitNbr() {
		switch(Character.toLowerCase(getType())) {
		case 'r':
			return (colour == 0) ? 5 : 11; // titan/black dragon
		case 'n':
			return (colour == 0) ? 1 : 7; // champion/death knight
		case 'b':
			return (colour == 0) ? 2 : 8; // druid/power lich
		case 'q':
			return (colour == 0) ? 3 : 9; // naga queen/medusa
		case 'k':
			return (colour == 0) ? 0 : 6; // arch angel/arch devil
		case 'p':
			return (colour == 0) ? 4 : 10; // peasant/skeleton		
		}
		return -1;
	}
	
	@Override
	public String toString() {
		return Game.getLongName(type, colour) + " at " + Game.makeStdPos(pos);
	}
}
