package game;

import interfaces.Vals;

public class Move implements Vals {
	
	// source and target positions, 0-63
	private int src, trg;
	private int colour;
	
	public Move(int src, int trg, int colour) {
		this.src = src;
		this.trg = trg;
		this.colour = colour;
	}
	
	public Move(int src, int trg) {
		this.src = src;
		this.trg = trg;
		this.colour = COLOR_NEUTRAL;
	}
	
	public Move() {
		this.src = 0;
		this.trg = 0;
		this.colour = COLOR_NEUTRAL;
	}
	
	// cloning constructor
	public Move(Move clone) {
		this.src = clone.src;
		this.trg = clone.trg;
		this.colour = clone.colour;
	}

	public int getSrc() {
		return src;
	}
	
	public String getStringSrc() {
		return String.format("%02d", src);
	}

	public void setSrc(int src) {
		this.src = src;
	}

	public int getTrg() {
		return trg;
	}
	
	public String getStringTrg() {
		return String.format("%02d", trg);
	}

	public void setTrg(int trg) {
		this.trg = trg;
	}
	
	public static Move makeMoveFromString(String moveString) {
		int src = Integer.parseInt(moveString.substring(0, 2));
		int trg = Integer.parseInt(moveString.substring(2, 4));
		return new Move(src, trg);
	}
	
	public int getColour() {
		return colour;
	}

	public void setColour(int colour) {
		this.colour = colour;
	}
	
	@Override
	public String toString() {
		return getStringSrc() + getStringTrg();
	}
	
	public boolean equals(Move move) {
		return (this.src == move.getSrc() && this.trg == move.getTrg());
	}
}
