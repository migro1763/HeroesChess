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
			dragPieceMoveBits = moveGen.possibleMoves(playerTurn, posOfPieceToMove, history);
			dragPiece.setMoveBits(dragPieceMoveBits);
			
//			// DEBUG:
//			List<Move> moves = MoveGenerator.bitsAsMoveList(dragPieceMoveBits, dragPiece.getPos());
//			Speak.say("Moves possible: ");
//			for (Move move : moves)
//				Speak.say(Move.makeStdMove(move.getSrc(), move.getTrg()) + ", ");
//			Speak.say();
//			// DEBUG END
		
			if(dragPieceMoveBits.getBits() > 0L) {
				long moveBits = 0L;
				if(activePlayer.getCurrentMove() != null)
					moveBits = 1L<<activePlayer.getCurrentMove().getTrg();
				else
					return false;
				
				if((dragPieceMoveBits.getBits() & moveBits) > 0L) {			
					// if move is (still) causing a check
				    if(moveGen.testCheck(playerTurn, moveBits))
				    	return false;    
				    else {
				    	selectedMove = activePlayer.getMove();
				    	// move the selected piece by selected move
			    		btb.movePiece(selectedMove);
			    		dragPiece.snapToNearestSquare(selectedMove.getTrg());
						// set history to last move
				    	history = selectedMove;
				    	activePlayer.moveSuccessfullyExecuted(selectedMove);
				    	// recreate and reposition all gui pieces, according to latest bitboards
						board.createGuiPieceArray();
						// repaint graphics
				    	board.repaint();
						
				    	// pawn promotion?
						char typeToMove = btb.getArraySquare(posOfPieceToMove);
						if(Character.toUpperCase(typeToMove) == 'P' && 
								(moveBits & MoveGenerator.rankMask[((playerTurn == 0) ? 0 : 7)]) != 0L)
							moveGen.promotePawn(BitBoard.getPos(moveBits), playerTurn);
						
						// if moved piece is a king, negate both castling possibilities
						if(Character.toUpperCase(typeToMove) == 'K') {
							kSideCastling[playerTurn] = qSideCastling[playerTurn] = false;
							int offset = (playerTurn == 0) ? 56 : 0;
							// if king move was a castling, also move the corresponding rook
							if(selectedMove.equals(new Move(4 + offset, 6 + offset)))
								btb.movePiece(new Move(7 + offset, 5 + offset));
							else if(selectedMove.equals(new Move(4 + offset, 2 + offset)))
								btb.movePiece(new Move(0 + offset, 3 + offset));
						}			
						// if moved piece is a rook, negate corresponding side castling possibility
						if(Character.toUpperCase(typeToMove) == 'R')
							if(posOfPieceToMove == 0 || posOfPieceToMove == 56) // if left side of board
								qSideCastling[playerTurn] = false;
							else if(posOfPieceToMove == 7 || posOfPieceToMove == 63) // if right side of board
								kSideCastling[playerTurn] = false;	
					}
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
			Speak.say("\n====> Player turn: " + COLOUR_NAME[playerTurn], true);
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
		Speak.say("active player changed to: " + this.activePlayer, true);
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
	
    public static String getLongName(char type) {
    	String colour = (Character.isUpperCase(type)) ? "White " : "Black ";
    	switch(Character.toUpperCase(type)) {
    		case 'P':	return colour + "pawn";
    		case 'R':	return colour + "rook";
    		case 'N':	return colour + "knight";
    		case 'B':	return colour + "bishop";
    		case 'Q':	return colour + "queen";
    		case 'K':	return colour + "king";
    		default:	return "";
    	}
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

	@Override
	public void run() {
		this.startGame();		
	}
}
