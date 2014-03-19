package players;

import game.Move;

public class Human extends Player {
	
	private String name;
	
	public Human(String name) {
		this.name = name;
		dragPiecesEnabled = true;
	}
	
	public Human() {
		this("");
	}
	
	@Override
	public Move getMove() {
		Move moveForExecution = currentMove;
		currentMove = null;
		return moveForExecution;
	}

	@Override
	public void moveSuccessfullyExecuted(Move move) {
		// remember last move
		setLastMove(move);
		// disable dragging until asked by ChessGame for the next move
		setDragPiecesEnabled(false);
	}
	
	@Override
	public String toString() {
		return name + ", is in check: " + isCheck;
	}
}
