package game;

import interfaces.Vals;

public class Move implements Vals {
	
	// source and target positions, 0-63
	private int src, trg;
	
	public Move(int src, int trg) {
		this.src = src;
		this.trg = trg;
	}
	
	public Move() {
		this.src = 0;
		this.trg = 0;
	}

	public int getSrc() {
		return src;
	}

	public void setSrc(int src) {
		this.src = src;
	}

	public int getTrg() {
		return trg;
	}

	public void setTrg(int trg) {
		this.trg = trg;
	}
	
	public String makeStdMove() {
		return makeStdMove(src/8, src%8, trg/8, trg%8);
	}
	
	public static String makeStdMove(int src, int trg) {
		return ("" + FILE_NAME[src/8] + (8-src%8) + "->" + FILE_NAME[trg/8] + (8-trg%8));
	}
	
	public static String makeStdMove(int x1, int y1, int x2, int y2) {
		return ("" + FILE_NAME[y1] + (8-x1) + "->" + FILE_NAME[y2] + (8-x2));
	}
	
	@Override
	public String toString() {
		return "" + String.format("%02d", src) + String.format("%02d", trg);
	}
	
	public boolean equals(Move move) {
		return (this.src == move.getSrc() && this.trg == move.getTrg());
	}
}
