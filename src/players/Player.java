package players;

import game.Move;

public abstract class Player {

	protected String name;
	protected boolean isCheck, isCheckMate;
	protected boolean dragPiecesEnabled;
	protected boolean kSideCastling = true, qSideCastling = true;
	protected Move lastMove, currentMove;
	protected int promoteTo = 0; // for pawn promotion, 0=no promotion
	// 1=queen, 2=knight, 3=rook, 4=bishop
	
	public Player(String name) {
		this.name = name;
	}
	
	public Player() {
		this.name = "";
	}
	
	public abstract Move getMove();
	
    public abstract void moveSuccessfullyExecuted(Move move);
    
	public boolean isDragPiecesEnabled() {
		return dragPiecesEnabled;
	}

	public void setDragPiecesEnabled(boolean state) {
		this.dragPiecesEnabled = state;
	}

	public boolean isCheck() {
		return isCheck;
	}

	public void setCheck(boolean isCheck) {
		this.isCheck = isCheck;
	}
	
	public boolean isCheckMate() {
		return isCheckMate;
	}

	public void setCheckMate(boolean isCheckMate) {
		this.isCheckMate = isCheckMate;
	}

	public void swapCheck() {
		isCheck = !isCheck;
	}

	public Move getLastMove() {
		return lastMove;
	}

	public void setLastMove(Move lastMove) {
		this.lastMove = lastMove;
	}

	public Move getCurrentMove() {
		return currentMove;
	}

	public void setCurrentMove(Move currentMove) {
		this.currentMove = currentMove;
	}

	public boolean iskSideCastling() {
		return kSideCastling;
	}

	public void setkSideCastling(boolean kSideCastling) {
		this.kSideCastling = kSideCastling;
	}

	public boolean isqSideCastling() {
		return qSideCastling;
	}

	public void setqSideCastling(boolean qSideCastling) {
		this.qSideCastling = qSideCastling;
	}

	public int getPromoteTo() {
		return promoteTo;
	}

	public void setPromoteTo(int promoteTo) {
		this.promoteTo = promoteTo;
	}
}
