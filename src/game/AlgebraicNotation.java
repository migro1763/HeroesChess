package game;

import interfaces.Vals;

public class AlgebraicNotation implements Vals {
	
	private BitBoard postGameBB;
	private String notation;
	private int callCounter = 0;

	public AlgebraicNotation(BitBoard postGameBB) {
		this.postGameBB = postGameBB;
		notation = "";
	}
	
	public String getNotation(Move move, BitBoard preGameBB) {
		String output = null;
		callCounter++;
		char srcType = Character.toUpperCase(preGameBB.getArraySquare(move.getSrc()));
		char trgType = Character.toUpperCase(preGameBB.getArraySquare(move.getTrg()));
		String srcFile = FILE_NAME[BitBoard.getX(move.getSrc())];
		String trgFile = FILE_NAME[BitBoard.getX(move.getTrg())];
		int trgRank = 8 - BitBoard.getY(move.getTrg());
		String capture = trgType == ' ' ? "" : "x";
		String pieceA = srcType == 'P' ? "" : Character.toString(srcType);
		
		if(srcType == 'P') {
			notation += (capture == "x" ? srcFile : "") + capture + trgFile + trgRank;
			// pawn promotion?
			if(trgRank == 1 || trgRank == 8)
				notation += "=" + Character.toUpperCase(postGameBB.getArraySquare(move.getTrg()));
		}
		// castling?
		else if(move.equals(K_CASTLING_MOVE[0]) || move.equals(K_CASTLING_MOVE[1]))
			notation += "o-o";
		else if(move.equals(Q_CASTLING_MOVE[0]) || move.equals(Q_CASTLING_MOVE[1]))
			notation += "o-o-o";
		else
			notation += pieceA + capture + trgFile + trgRank;
		
		notation += " ";
		if(callCounter%2 == 0) {
			output = notation;
			notation = "";
		}		
		return output;
	}
}
