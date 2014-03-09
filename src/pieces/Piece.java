package pieces;

public abstract class Piece {
		
	protected boolean isCaptured = false;

	public Piece() {
		
	}
	
	// get unit nbr for animated sprite loading
//	public int getUnitNbr() {
//		switch(this.type) {
//		case TYPE_ROOK:
//			return (this.color) ? 5 : 11;
//		case TYPE_KNIGHT:
//			return (this.color) ? 1 : 7;
//		case TYPE_BISHOP:
//			return (this.color) ? 2 : 8;
//		case TYPE_QUEEN:
//			return (this.color) ? 3 : 9;
//		case TYPE_KING:
//			return (this.color) ? 0 : 6;
//		case TYPE_PAWN:
//			return (this.color) ? 4 : 10;		
//		}
//		return -1;
//	}
	
	@Override
	public String toString() {	
		return "";
	}

	public void isCaptured(boolean isCaptured) {
		this.isCaptured = isCaptured;
	}

	public boolean isCaptured() {
		return this.isCaptured;
	}
	
	public Piece clone() {
		return null;
	};
	
	public abstract String possibleMoves(long bitBoard);

}
