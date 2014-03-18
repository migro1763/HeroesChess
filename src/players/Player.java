package players;

import game.Move;

public abstract class Player {

	private boolean isCheck;
	protected Move lastMove, currentMove;
	protected boolean dragPiecesEnabled;
	
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

}
