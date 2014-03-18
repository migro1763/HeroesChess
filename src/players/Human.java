package players;

import game.Move;

public class Human extends Player {
	
	public Human() {
		dragPiecesEnabled = true;
	}
	
	@Override
	public Move getMove() {
		setDragPiecesEnabled(true);
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

		// repaint the new state
	}
}
