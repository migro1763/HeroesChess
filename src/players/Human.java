package players;

import game.Move;

public class Human extends Player {
	
	public Human(String name) {
		super(name);
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
		return "Human: " + name;
	}
}
