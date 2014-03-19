package players;

import game.Move;
import game.Speak;

public abstract class Player {

	protected boolean isCheck;
	protected boolean dragPiecesEnabled;
	protected Move lastMove, currentMove;
	private boolean isDebugging = false;
	
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
		Speak.say("player => lastMove set to: " + this.lastMove, true);
	}

	public Move getCurrentMove() {
		return currentMove;
	}

	public void setCurrentMove(Move currentMove) {
		this.currentMove = currentMove;
		Speak.say("player => currentMove set to: " + this.currentMove, true);
	}

	public boolean isDebugging() {
		return isDebugging;
	}

	public void setDebugging(boolean isDebugging) {
		this.isDebugging = isDebugging;
	}
}
