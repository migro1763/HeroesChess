package players;

import game.Move;
import game.Speak;

public abstract class Player {

	protected boolean isCheck;
	protected boolean dragPiecesEnabled;
	protected boolean kSideCastling, qSideCastling;
	protected Move lastMove, currentMove;
	protected boolean isDebugging = false;
	
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

	public boolean isDebugging() {
		return isDebugging;
	}

	public void setDebugging(boolean isDebugging) {
		this.isDebugging = isDebugging;
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
}
