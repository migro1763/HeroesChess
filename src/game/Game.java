package game;

import gui.ChessBoardGui;
import gui.PieceGui;
import interfaces.Vals;

import java.util.ArrayList;
import java.util.List;

import players.Player;

public class Game implements Runnable, Vals {
	private BitBoard btb;
	private ChessBoardGui board;
	private MoveGenerator moveGen;
	private int playerTurn = COLOR_WHITE; // who's turn it is
	public boolean[] kSideCastling = {true, true}; // true if player colour can castle king side
	public boolean[] qSideCastling = {true, true}; // true if player colour can castle queen side
	private Player blackPlayer = null, whitePlayer = null, activePlayer = null;
	
	public Game() {
		btb = new BitBoard(this);
		moveGen = new MoveGenerator(this);
		board = new ChessBoardGui(this);
	}
	
	// cloning constructor
	public Game(Game clone) {
		this.btb = clone.btb;
		this.moveGen = clone.moveGen;
		this.board = clone.board;
		this.playerTurn = clone.playerTurn;
		this.kSideCastling = clone.kSideCastling;
		this.qSideCastling = clone.qSideCastling;
		this.whitePlayer = clone.whitePlayer;
		this.blackPlayer = clone.blackPlayer;
		this.activePlayer = clone.activePlayer;
	}
	
	public void startGame() {
		Move history = null;
		
		// check if all players are ready
		System.out.println("ChessGame: waiting for players");
		while (blackPlayer == null || whitePlayer == null) {
			// players are still missing
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
		}
		// set start player, white always starts game
		setPlayerTurn(playerTurn);				
		System.out.println("ChessGame: starting game flow");
		
		// start game flow loop
		boolean didMove = false;
		while(!isGameOver()) {
			didMove = gameLoop(history);
			changePlayerTurnAfterMove(didMove);
		}
	}

	public boolean gameLoop(Move history) {
		int posOfPieceToMove = -1;
		Move selectedMove = null;
		boolean hasMoved = false;
		
		// update OCCUPIED and EMPTY in the MoveGenerator object
		moveGen.updateBBStates();
		// repaint graphics
		board.repaint();
		
		// generate bitboard of moves in Moves->possibleMoves(), and set
		// gui board's dragpiece's moveBits to it.
		BB dragPieceMoveBits = new BB();
		PieceGui dragPiece = board.getDragPiece();
		if(dragPiece != null) {
			// is active player in check?
			activePlayer.setCheck(moveGen.isInCheck(playerTurn));
			
			posOfPieceToMove = dragPiece.getPos();
			board.setDebugText("posOfPieceToMove: " + posOfPieceToMove);
			dragPieceMoveBits = moveGen.possibleMoves(playerTurn, posOfPieceToMove, history);
			dragPiece.setMoveBits(dragPieceMoveBits);
			
//			// DEBUG:
//			List<Move> moves = MoveGenerator.bitsAsMoveList(dragPieceMoveBits, dragPiece.getPos());
//			Speak.say("Moves possible: ");
//			for (Move move : moves)
//				Speak.say(Move.makeStdMove(move.getSrc(), move.getTrg()) + ", ");
//			Speak.say();
//			// DEBUG END
		
			if(dragPieceMoveBits.getBits() != 0L) {
				long moveBits = 0L;
				if(activePlayer.getCurrentMove() != null)
					moveBits = 1L<<activePlayer.getCurrentMove().getTrg();
				else
					return false;
				
				if((dragPieceMoveBits.getBits() & moveBits) != 0L) {			
					// if move is (still) causing a check
//				    if(moveGen.testCheck(playerTurn, moveBits))
//				    	return false;    
//				    else {
				    	selectedMove = activePlayer.getMove();
				    	// move the selected piece by selected move
			    		btb.movePiece(selectedMove);		
			    		// snap the moved piece to its nearest square
			    		dragPiece.snapToNearestSquare(selectedMove.getTrg());
						// set history to last move
				    	history = selectedMove;
				    	// finish move setup for active player
				    	activePlayer.moveSuccessfullyExecuted(selectedMove);
				    	// recreate and reposition all gui pieces, according to latest bitboards
						board.setupGuiPieceArray();
						// do pawn promotion and finish castling setup (move rook etc.)
						moveGen.pawnPromoAndCastling(selectedMove);
						// repaint graphics
				    	board.repaint();
						
				    hasMoved = true;
				} else {
					// -1 indicates no move was done, possibly due to wrong square selected or
					// selected square's piece cannot move at all
					hasMoved = false;
				}			
			}
		}
		return hasMoved;
	}
	
	private void changePlayerTurnAfterMove(boolean didMove) {
		if(didMove) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {}
			setPlayerTurn(1-playerTurn);
			board.setDebugText("Player turn: " + COLOUR_NAME[playerTurn]);
		}		
	}
	
	public int getPlayerTurn() {
		return playerTurn;
	}

	public void setPlayerTurn(int playerTurn) {
		this.playerTurn = playerTurn;
		setActivePlayer((this.playerTurn == COLOR_WHITE) ? whitePlayer : blackPlayer);
		activePlayer.setDragPiecesEnabled(true);
	}
	
	public Player getActivePlayer() {
		return activePlayer;
	}

	public void setActivePlayer(Player activePlayer) {
		this.activePlayer = activePlayer;
		board.setDebugText("active player changed to: " + this.activePlayer);
	}
	
	public void setPlayer(int pieceColor, Player playerHandler) {
		switch (pieceColor) {
			case 0: this.whitePlayer = playerHandler; break;
			case 1: this.blackPlayer = playerHandler; break;
			default: 
				throw new IllegalArgumentException("Invalid pieceColor: " + pieceColor);
		}
	}
	
	private boolean isGameOver() {
		// TODO: return whether game is over
		return false;
	}
	
    public static String getLongName(char type, int colour) {
    	switch(Character.toUpperCase(type)) {
    		case 'P':	return COLOUR_NAME[colour] + " " + LONG_PIECE_NAME[0];
    		case 'R':	return COLOUR_NAME[colour] + " " + LONG_PIECE_NAME[3];
    		case 'N':	return COLOUR_NAME[colour] + " " + LONG_PIECE_NAME[1];
    		case 'B':	return COLOUR_NAME[colour] + " " + LONG_PIECE_NAME[2];
    		case 'Q':	return COLOUR_NAME[colour] + " " + LONG_PIECE_NAME[4];
    		case 'K':	return COLOUR_NAME[colour] + " " + LONG_PIECE_NAME[5];
    		default:	return "";
    	}
    }
    
    public static String getLongName(char type) {
    	int colour = (Character.isUpperCase(type)) ? 0 : 1;
    	return getLongName(type, colour);
    }
	
	public static String makeStdPos(int pos) {
		return FILE_NAME[pos % 8] + (8-(pos / 8));
	}
	
	public BitBoard getBitBoard() {
		return btb;
	}

	public void setBitBoard(BitBoard bitBoard) {
		this.btb = bitBoard;
	}

	public ChessBoardGui getBoard() {
		return board;
	}

	@Override
	public void run() {
		this.startGame();		
	}
}
