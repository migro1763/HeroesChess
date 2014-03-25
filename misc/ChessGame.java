package game;

import java.util.ArrayList;
import java.util.List;
//import java.util.HashMap;
//import gui.ChessGui;
//import interfaces.IPlayerHandler;
//import java.io.IOException;

import players.Player;

public class ChessGame implements Runnable {

	public int gameState = GAME_STATE_WHITE;
	public static final int GAME_STATE_WHITE = 0;
	public static final int GAME_STATE_BLACK = 1;
	public static final int GAME_STATE_END_BLACK_WON = 2;
	public static final int GAME_STATE_END_WHITE_WON = 3;

	// 0 = bottom, size = top
	public List<Piece> pieces = new ArrayList<Piece>();
	private List<Piece> capturedPieces = new ArrayList<Piece>();
//	private Square[][] squares = new Square[8][8];
//	private HashMap<Coordinate, Square> squareCoordinates = new HashMap<Coordinate, Square>();

	private MoveValidator moveValidator;
//	private IPlayerHandler blackPlayerHandler;
//	private IPlayerHandler whitePlayerHandler;
//	private IPlayerHandler activePlayerHandler;
	private Player blackPlayerHandler, whitePlayerHandler, activePlayerHandler;

	public ChessGame() {
		this.moveValidator = new MoveValidator(this);
		positionPieces();
	}
	
	public ChessGame(ChessGame chessGame) {
//		initializeSquares();
		this.pieces = new ArrayList<Piece>();
		for (Piece p : chessGame.pieces) {
			Piece newPiece = p.clone();
			this.pieces.add(newPiece);
		}
		this.capturedPieces = new ArrayList<Piece>();
		for (Piece cp : chessGame.capturedPieces) {
			Piece newCapturedPiece = cp.clone();
			this.capturedPieces.add(newCapturedPiece);
		}
		this.gameState = chessGame.gameState;
		this.moveValidator = new MoveValidator(chessGame);
		setPlayer(Piece.COLOR_WHITE, chessGame.whitePlayerHandler);
		setPlayer(Piece.COLOR_BLACK, chessGame.blackPlayerHandler);
		this.activePlayerHandler = chessGame.activePlayerHandler;
	}
	
	public void positionPieces() {
		// create and place pieces
		// rook, knight, bishop, queen, king, bishop, knight, and rook
		createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_ROOK, 0, Piece.ROW_1, Piece.COLUMN_A, 0);
		createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_KNIGHT, 0, Piece.ROW_1, Piece.COLUMN_B, 1);
		createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_BISHOP, 0, Piece.ROW_1, Piece.COLUMN_C, 2);
		createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_QUEEN, 0, Piece.ROW_1, Piece.COLUMN_D, 3);
		createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_KING, 0, Piece.ROW_1, Piece.COLUMN_E, 4);
		createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_BISHOP, 1, Piece.ROW_1, Piece.COLUMN_F, 5);
		createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_KNIGHT, 1, Piece.ROW_1, Piece.COLUMN_G, 6);
		createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_ROOK, 1, Piece.ROW_1, Piece.COLUMN_H, 7);

		// pawns
		int currentColumn = Piece.COLUMN_A;
		for (int i = 0; i < 8; i++) {
			createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_PAWN, 0, Piece.ROW_2, currentColumn, 8 + i);
			currentColumn++;
		}
		
		// black pawns
		currentColumn = Piece.COLUMN_A;
		for (int i = 0; i < 8; i++) {
			createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_PAWN, 0, Piece.ROW_7, currentColumn, 15 + i);
			currentColumn++;
		}

		createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_ROOK, 0, Piece.ROW_8, Piece.COLUMN_A, 23);
		createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_KNIGHT, 0, Piece.ROW_8, Piece.COLUMN_B, 24);
		createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_BISHOP, 0, Piece.ROW_8, Piece.COLUMN_C, 25);
		createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_QUEEN, 0, Piece.ROW_8, Piece.COLUMN_D, 26);
		createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_KING, 0, Piece.ROW_8, Piece.COLUMN_E, 27);
		createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_BISHOP, 1, Piece.ROW_8, Piece.COLUMN_F, 28);
		createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_KNIGHT, 1, Piece.ROW_8, Piece.COLUMN_G, 29);
		createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_ROOK, 1, Piece.ROW_8, Piece.COLUMN_H, 30);
	}
	
	// set the client/player for the specified piece color
//	public void setPlayer(int pieceColor, IPlayerHandler playerHandler){
	public void setPlayer(int pieceColor, Player playerHandler) {
		switch (pieceColor) {
			case Piece.COLOR_BLACK: this.blackPlayerHandler = playerHandler; break;
			case Piece.COLOR_WHITE: this.whitePlayerHandler = playerHandler; break;
			default: throw new IllegalArgumentException("Invalid pieceColor: "+pieceColor);
		}
	}
	
//    private void initializeSquares() {
//        for (int i = 0; i < squares.length; i++)
//                for (int j = 0; j < squares[i].length; j++) {
//                        Coordinate coordinate = new Coordinate(i, j);
//                        squares[i][j] = new Square(coordinate);
//                        squareCoordinates.put(coordinate, squares[i][j]);
//                }
//    }
    
	// create piece instance and add it to the internal list of pieces
	public void createAndAddPiece(int color, int type, int nbr, int row, int column, int id) {
		Piece piece = new Piece(color, type, nbr, row, column, id);
		this.pieces.add(piece);
	}

	// ------=====>>> Start main game flow! <<<=====------
	//                иииииииииииииииииииии
	public void startGame() {
		// check if all players are ready
		System.out.println("ChessGame: waiting for players");
		while (this.blackPlayerHandler == null || this.whitePlayerHandler == null){
			// players are still missing
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		// set start player, white always starts game
		this.activePlayerHandler = this.whitePlayerHandler;
		
		
		// ----------------------------- Main game flow <-
		// start game flow
		System.out.println("ChessGame: starting game flow");
		while(!isGameEndConditionReached()) {
			waitForMove();
			swapActivePlayer();
		}
		// ----------------------------- Main game flow ->
		
		
		System.out.println("ChessGame: game ended");
		if(this.gameState == ChessGame.GAME_STATE_END_BLACK_WON) {
			System.out.println("Black won!");
			
		} else if(this.gameState == ChessGame.GAME_STATE_END_WHITE_WON) {
			System.out.println("White won!");
			
		} else {
			throw new IllegalStateException("Illegal end state: "+this.gameState);
		}
	}

	/**
	 * Wait for client/player move and execute it.
	 * Notify all clients/players about successful execution of move.
	 */
	private void waitForMove() {
		Move move = null;
		// loop until a valid move has been made
		do {
			move = this.activePlayerHandler.getMove();
			try {
				Thread.sleep(100); // pause 0.1 seconds before move is verified/moving on
			} catch (InterruptedException e) {}
			if( move != null && this.moveValidator.isMoveValid(move, false) ) {
				break; // a valid move has been made. exiting loop.
			} else if( move != null && !this.moveValidator.isMoveValid(move, true)) {
				System.out.println("provided move was invalid: " + move);
				move = null;
			}
		} while(move == null);
		
		if(!move.isInCheck(this)) {	
			//execute valid move
			this.activePlayerHandler.setCheck(false);
			boolean success = this.movePiece(move, false);
			if(success) {
				this.blackPlayerHandler.moveSuccessfullyExecuted(move);
				this.whitePlayerHandler.moveSuccessfullyExecuted(move);
			} else {
				throw new IllegalStateException("move was valid, but failed to execute it");
			}
		} else {
			this.activePlayerHandler.setCheck(true);
		}
	}

	/**
	 * Move piece to the specified location. If the target location is occupied
	 * by an opponent piece, that piece is marked as 'captured'. If the move
	 * could not be executed successfully, 'false' is returned and the game
	 * state does not change.
	 */
	public boolean movePiece(Move move, boolean debug) {
		// set captured piece in move
		// this information is needed in the undoMove() method.
		move.capturedPiece = this.getNonCapturedPieceAtLocation(move.targetRow, move.targetColumn);	
		Piece piece = getNonCapturedPieceAtLocation(move.sourceRow, move.sourceColumn);
		
		// is move capturing an opponent piece? (1-piece.getColor() = opponent's color)
		if (isNonCapturedPieceAtLocation(GameMethods.opponentOf(
				piece.getColor()), move.targetRow, move.targetColumn)) {
			// handle captured piece
			Piece opponentPiece = move.capturedPiece;
			this.pieces.remove(opponentPiece);
			opponentPiece.isCaptured(true);
			this.getCapturedPieces().add(opponentPiece);
		}

		// move piece to new position
		if(piece.getType() == Piece.TYPE_KING) {
			if(Math.abs(move.targetColumn - piece.getColumn()) > 1) { // castling attempt?
				KingSideCastling ksc = new KingSideCastling(this, move.start, move.end);
				ksc.makeMove(this);
			}
		} else {
			piece.setHasMoved(); // only applies to pawns
			piece.setRow(move.targetRow);
			piece.setColumn(move.targetColumn);
		}
		return true;
	}
	
	// Undo the specified move. It will also adjust the game state appropriately.
	public void undoMove(Move move){
		Piece piece = getNonCapturedPieceAtLocation(move.targetRow, move.targetColumn);
		piece.setRow(move.sourceRow);
		piece.setColumn(move.sourceColumn);
		
		if(move.capturedPiece != null){
			move.capturedPiece.setRow(move.targetRow);
			move.capturedPiece.setColumn(move.targetColumn);
			move.capturedPiece.isCaptured(false);
			this.getCapturedPieces().remove(move.capturedPiece);
			this.pieces.add(move.capturedPiece);
		}
		
		if(piece.getColor() == Piece.COLOR_BLACK) {
			this.gameState = ChessGame.GAME_STATE_BLACK;
		} else {
			this.gameState = ChessGame.GAME_STATE_WHITE;
		}
	}

	/**
	 * check if the games end condition is met: One color has a captured king
	 * @return true if the game end condition is met
	 */
	private boolean isGameEndConditionReached() {
		for (Piece piece : this.getCapturedPieces()) {
			if (piece.getType() == Piece.TYPE_KING ) {
				return true;
			} else {
				// continue iterating
			}
		}

		return false;
	}
	
	/**
	 * swap active player and change game state
	 */
	private void swapActivePlayer() {
		if( this.activePlayerHandler == this.whitePlayerHandler ) {
			this.activePlayerHandler = this.blackPlayerHandler;
		} else {
			this.activePlayerHandler = this.whitePlayerHandler;
		}	
		this.changeGameState();
	}

	/**
	 * returns the first piece at the specified location that is not marked as
	 * 'captured'.
	 */
	public Piece getNonCapturedPieceAtLocation(int row, int column) {
		for (Piece piece : this.pieces) {
			if (piece.getRow() == row && piece.getColumn() == column) {
				return piece;
			}
		}
		return null;
	}
	
	public Piece getPiece(Coordinate coordinate) {
		return getNonCapturedPieceAtLocation(coordinate.getRow(), coordinate.getColumn());
	}

	/**
	 * Checks whether there is a piece at the specified location that is not
	 * marked as 'captured' and has the specified color.
	 */
	public boolean isNonCapturedPieceAtLocation(int color, int row, int column) {
		for (Piece piece : this.pieces) {
			if (piece.getColor() == color && piece.getRow() == row && piece.getColumn() == column)
				return true;
		}
		return false;
	}


	// Checks whether there is a non-captured piece at the specified location
	boolean isNonCapturedPieceAtLocation(int row, int column) {
		for (Piece piece : this.pieces) {
			if (piece.getRow() == row && piece.getColumn() == column) {
				return true;
			}
		}
		return false;
	}

	public int getGameState() {
		return this.gameState;
	}

	// get pieces of specific color
	public List<Piece> getPieces(int color) {
		List<Piece> pieceSet = new ArrayList<Piece>();
		for(Piece p : this.pieces)
			if(p.getColor() == color)
				pieceSet.add(p);
		return pieceSet;
	}
	
	public List<Piece> getPieces() {
		return this.pieces;
	}

	public List<Piece> getCapturedPieces() {
		return capturedPieces;
	}

	/**
	 * switches the game state depending on the current board situation.
	 */
	public void changeGameState() {
		// check if game end condition has been reached
		//
		if (this.isGameEndConditionReached()) {

			if (this.gameState == ChessGame.GAME_STATE_BLACK) {
				this.gameState = ChessGame.GAME_STATE_END_BLACK_WON;
			} else if(this.gameState == ChessGame.GAME_STATE_WHITE) {
				this.gameState = ChessGame.GAME_STATE_END_WHITE_WON;
			} else {
				// leave game state as it is
			}
			return;
		}

		if(this.gameState >= 0 && this.gameState <= 1) { // black or white turn
			this.gameState = 1 - this.gameState; // swap gameState
		} else if(this.gameState >= 2 && this.gameState <= 3) { // black or white has won, do nothing
		} else {
			throw new IllegalStateException("unknown game state:" + this.gameState);
		}
	}
	
	public MoveValidator getMoveValidator(){
		return this.moveValidator;
	}
	
	public void setMoveValidator(MoveValidator moveValidator){
		this.moveValidator = moveValidator;
	}

	@Override
	public void run() {
		this.startGame();
	}

}
