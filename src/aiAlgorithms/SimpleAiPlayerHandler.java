package aiAlgorithms;

import java.util.ArrayList;
import java.util.List;

import players.Player;
import game.ChessGame;
import game.Game;
import game.GameMethods;
import game.HeroesChessGame;
import game.Move;
import game.MoveValidator;
import game.MoveGenerator;
import game.Piece;

//public class SimpleAiPlayerHandler implements IPlayerHandler {
public class SimpleAiPlayerHandler extends Player {

	private static final long serialVersionUID = 273343436651733806L;
	private Game actualGame, game;
	private MoveGenerator moves;
	
	// number of moves to look into the future
	public int maxDepth = 1;

	public SimpleAiPlayerHandler(Game game) {
		this.actualGame = game;
	}

	@Override
	public Move getMove() {
		this.game = new Game(this.actualGame);
		this.moves = new MoveGenerator(this.game);
		return getBestMove();
	}

	/**
	 * get best move for current game situation
	 * @return a valid Move instance
	 */
	private Move getBestMove() {
		System.out.println("Thinking...");
		
		List<Move> validMoves = moves.possibleMoves(game.getPlayerTurn(), start, history);
		int bestResult = Integer.MIN_VALUE;
		Move bestMove = null;
		for (Move move : validMoves) {
			executeMove(move, false);
			int evaluationResult = -1 * negaMax(this.maxDepth, "");
			undoMove(move);
			if( evaluationResult > bestResult){
				bestResult = evaluationResult;
				bestMove = move;
			}
		}
		return bestMove;
	}

	@Override
	public void moveSuccessfullyExecuted(Move move) {
		// we are using the same chessGame instance, so no need to do anything here.
		System.out.println("executed: " + move);		
	}

	/**
	 * evaluate current game state according to nega max algorithm
	 *
	 * @param depth - current depth level (number of counter moves that still need to be evaluated)
	 * @param indent - debug string, that is placed in front of each log message
	 * @return integer score of game state after looking at "depth" counter moves
	 */
	private int negaMax(int depth, String indent) {
		if (depth <= 0
			|| this.chessGame.getGameState() == ChessGame.GAME_STATE_END_WHITE_WON
			|| this.chessGame.getGameState() == ChessGame.GAME_STATE_END_BLACK_WON) {
			
			return evaluateState();
		}
		
		List<Move> moves = generateMoves(false);
		int currentMax = Integer.MIN_VALUE;

		for(Move currentMove : moves) {
			executeMove(currentMove, true);
			int score = -1 * negaMax(depth - 1, indent + " ");
			undoMove(currentMove);
			
			if( score > currentMax){
				currentMax = score;
			}
		}
		return currentMax;
	}

	/**
	 * undo specified move
	 */
	public void undoMove(Move move) {
//		this.chessGame.undoMove(move);
		Piece piece = this.chessGame.getNonCapturedPieceAtLocation(move.targetRow, move.targetColumn);
		piece.setRow(move.sourceRow);
		piece.setColumn(move.sourceColumn);
		
		if(move.capturedPiece != null){
			move.capturedPiece.setRow(move.targetRow);
			move.capturedPiece.setColumn(move.targetColumn);
			move.capturedPiece.isCaptured(false);
			this.chessGame.getCapturedPieces().remove(move.capturedPiece);
			this.chessGame.pieces.add(move.capturedPiece);
		}
		
		if(piece.getColor() == Piece.COLOR_BLACK) {
			this.chessGame.gameState = ChessGame.GAME_STATE_BLACK;
		} else {
			this.chessGame.gameState = ChessGame.GAME_STATE_WHITE;
		}
	}

	/**
	 * Execute specified move. This will also change the game state after the
	 * move has been executed.
	 */
	private void executeMove(Move move, boolean negaMax) {
		//System.out.println("executing move");
//		this.chessGame.movePiece(move, true);	
		move.capturedPiece = this.chessGame.getNonCapturedPieceAtLocation(move.targetRow, move.targetColumn);	
		Piece piece = this.chessGame.getNonCapturedPieceAtLocation(move.sourceRow, move.sourceColumn);
		
		if(move.capturedPiece != null && move.capturedPiece.getColor() == 
									GameMethods.opponentOf(piece.getColor())) {
			// handle captured piece
			Piece opponentPiece = move.capturedPiece;
			this.chessGame.pieces.remove(opponentPiece);
			opponentPiece.isCaptured(true);
			this.chessGame.getCapturedPieces().add(opponentPiece);
		}

		// move piece to new position
		piece.setHasMoved(); // only applies to pawns and kings		
		piece.setRow(move.targetRow);
		piece.setColumn(move.targetColumn);	
		
		this.chessGame.changeGameState();
	}

	/**
	* generate all possible/valid moves for the specified game
	* @param state - game state for which the moves should be generated
	* @return list of all possible/valid moves
	*/
	private List<Move> generateMoves(boolean debug) {
		List<Move> validMoves = new ArrayList<Move>();
		Move testMove = new Move(0, 0, 0, 0);

		// only look at pieces of current players color
		List<Piece> pieces = this.chessGame.getPieces(this.chessGame.getGameState());

		// iterate over all non-captured pieces
		for (Piece piece : pieces) {
			// start generating move
			testMove.sourceRow = piece.getRow();
			testMove.sourceColumn = piece.getColumn();
	
			// iterate over all board rows and columns
			for (int targetRow = Piece.ROW_1; targetRow <= Piece.ROW_8; targetRow++) {
				for (int targetColumn = Piece.COLUMN_A; targetColumn <= Piece.COLUMN_H; targetColumn++) {
					// generating move...
					testMove.targetRow = targetRow;
					testMove.targetColumn = targetColumn;
					
					// check if generated move is valid
					if (this.validator.isMoveValid(testMove, true)) {
						// valid move
						Move newValidMove = testMove.clone();
						validMoves.add(newValidMove);
					} else {
						// generated move is invalid, so we skip it
					}
				}
			}
		}
		return validMoves;
	}

	/**
	 * evaluate the current game state from the view of the
	 * current player. High numbers indicate a better situation for
	 * the current player.
	 *
	 * @return integer score of current game state
	 */
	private int evaluateState() {
		// add up score
		int scoreWhite = 0;
		int scoreBlack = 0;
		for (Piece piece : this.chessGame.getPieces()) {
			if(piece.getColor() == Piece.COLOR_BLACK) {
				scoreBlack += getScoreForPieceType(piece.getType());
				scoreBlack += getScoreForPiecePosition(piece.getRow(), piece.getColumn());
			} else if( piece.getColor() == Piece.COLOR_WHITE) {
				scoreWhite += getScoreForPieceType(piece.getType());
				scoreWhite += getScoreForPiecePosition(piece.getRow(),piece.getColumn());
			} else {
				throw new IllegalStateException(
						"unknown piece color found: " + piece.getColor());
			}
		}
		
		// return evaluation result depending on who's turn it is
		int gameState = this.chessGame.getGameState();
		
		if( gameState == ChessGame.GAME_STATE_BLACK) {
			return scoreBlack - scoreWhite;
		
		} else if(gameState == ChessGame.GAME_STATE_WHITE) {
			return scoreWhite - scoreBlack;
		
		} else if(gameState == ChessGame.GAME_STATE_END_WHITE_WON
				|| gameState == ChessGame.GAME_STATE_END_BLACK_WON) {
			return Integer.MIN_VALUE + 1;
		
		} else {
			throw new IllegalStateException("unknown game state: " + gameState);
		}
	}
	
	/**
	 * get the evaluation bonus for the specified position
	 * @param row - one of Piece.ROW_..
	 * @param column - one of Piece.COLUMN_..
	 * @return integer score
	 */
	private int getScoreForPiecePosition(int row, int column) {
		byte[][] positionWeight =
		{ {1,1,1,1,1,1,1,1}
		 ,{2,2,2,2,2,2,2,2}
		 ,{2,2,3,3,3,3,2,2}
		 ,{2,2,3,4,4,3,2,2}
		 ,{2,2,3,4,4,3,2,2}
		 ,{2,2,3,3,3,3,2,2}
		 ,{2,2,2,2,2,2,2,2}
		 ,{1,1,1,1,1,1,1,1}
		 };
		return positionWeight[row][column];
	}

	/**
	 * get the evaluation score for the specified piece type
	 * @param type - one of Piece.TYPE_..
	 * @return integer score
	 */
	private int getScoreForPieceType(int type){
		switch (type) {
			case Piece.TYPE_BISHOP: return 333;
			case Piece.TYPE_KING: return Integer.MAX_VALUE;
			case Piece.TYPE_KNIGHT: return 320;
			case Piece.TYPE_PAWN: return 100;
			case Piece.TYPE_QUEEN: return 880;
			case Piece.TYPE_ROOK: return 510;
			default: throw new IllegalArgumentException("unknown piece type: "+type);
		}
	}
}

