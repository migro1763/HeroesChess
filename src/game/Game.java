package game;

import gui.ChessBoardGui;
import interfaces.Vals;

import java.util.ArrayList;
import java.util.List;

import players.Player;

public class Game implements Runnable, Vals {
	private BitBoard btb;
	private ChessBoardGui board;
	private MoveGenerator moveGen;
	private int playerTurn = COLOR_WHITE; // who's turn it is
	public boolean[] isChecked = {false, false}; // set for each player if checked
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
		this.isChecked = clone.isChecked;
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
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		// set start player, white always starts game
		activePlayer = whitePlayer;		
		
		// start game flow
		System.out.println("ChessGame: starting game flow");
		Move selectedMove = null;
		while(!isGameOver()) {
			if(selectedMove != null) {
				activePlayer.setDragPiecesEnabled(false);
				setPlayerTurn(1-playerTurn);	
				Speak.say("\n====> Player turn: " + COLOUR_NAME[playerTurn], true);
				selectedMove = null;
				activePlayer.setDragPiecesEnabled(true);
			}
			selectedMove = gameLoop(history);
//			activePlayer.moveSuccessfullyExecuted(selectedMove);
		}
	}
	
	public Move gameLoop(Move history) {
		int selectedMoveNbr = -1;
		int posOfPieceToMove = -1;
		Move selectedMove = null;
		
		// list of moves as Move objects
		List<Move> listOfMoves = new ArrayList<Move>();
		
		// update OCCUPIED and EMPTY in the MoveGenerator object
		moveGen.updateBBStates();
		
		// TODO: update & repaint GUI board
		board.repaint();
//		btb.drawArray(); 		// draw current chess board
			
		isChecked[playerTurn] = moveGen.isInCheck(playerTurn);
		if(isChecked[playerTurn])	// tell whether current player is in check
			Speak.say("\n!! => " + COLOUR_NAME[playerTurn] + " is in check!", true);
		
		// generate bitboard of moves in Moves->possibleMoves(), and set
		// gui board's dragpiece's moveBits to it.
		if(board.getDragPiece() != null) {
			posOfPieceToMove = board.getDragPiece().getPos();
			board.getDragPiece().setMoveBits(moveGen.possibleMoves(playerTurn, posOfPieceToMove, history));
			// get list of moves from the bitboard
			listOfMoves = MoveGenerator.bitsAsMoveList(
					board.getDragPiece().getMoveBits(), posOfPieceToMove);
		}
		
		if(!listOfMoves.isEmpty()) {
			char typeToMove = btb.getArraySquare(posOfPieceToMove);
			
			// TODO: get selected move/target position from user as selectedMoveNbr (int)
		
			if((selectedMoveNbr >= 0) && (selectedMoveNbr < listOfMoves.size())) {
			    long moveBits = 1L<<listOfMoves.get(selectedMoveNbr).getTrg();
				// if move is (still) causing a check
			    if(moveGen.testCheck(playerTurn, moveBits))
			    	selectedMoveNbr = -1;
			    else {			
					// move the selected piece by selected move
					btb.movePiece(listOfMoves.get(selectedMoveNbr));
					// set history to last move
					history = listOfMoves.get(selectedMoveNbr);
					
			    	// pawn promotion?
					if(Character.toUpperCase(typeToMove) == 'P' && 
							(moveBits & MoveGenerator.rankMask[((playerTurn == 0) ? 0 : 7)]) != 0L)
						moveGen.promotePawn(BitBoard.getPos(moveBits), playerTurn);
					
					// if moved piece is a king, negate both castling possibilities
					if(Character.toUpperCase(typeToMove) == 'K') {
						kSideCastling[playerTurn] = qSideCastling[playerTurn] = false;
						int offset = (playerTurn == 0) ? 56 : 0;
						// if king move was a castling, also move the corresponding rook
						if(listOfMoves.get(selectedMoveNbr).equals(new Move(4 + offset, 6 + offset)))
							btb.movePiece(new Move(7 + offset, 5 + offset));
						else if(listOfMoves.get(selectedMoveNbr).equals(new Move(4 + offset, 2 + offset)))
							btb.movePiece(new Move(0 + offset, 3 + offset));
					}			
					// if moved piece is a rook, negate corresponding side castling possibility
					if(Character.toUpperCase(typeToMove) == 'R')
						if(posOfPieceToMove == 0 || posOfPieceToMove == 56) // if left side of board
							qSideCastling[playerTurn] = false;
						else if(posOfPieceToMove == 7 || posOfPieceToMove == 63) // if right side of board
							kSideCastling[playerTurn] = false;	
				}
			    selectedMove = listOfMoves.get(selectedMoveNbr);
			} else {
				// -1 indicates no move was done, possibly due to wrong square selected or
				// selected square's piece cannot move at all
				selectedMoveNbr = -1;
				
			} // end if(selectedMoveNbr >= 0)
			
		} // end if(!listOfMoves.isEmpty())
		return selectedMove;
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
	
	public int getPlayerTurn() {
		return playerTurn;
	}

	public void setPlayerTurn(int playerTurn) {
		this.playerTurn = playerTurn;
		activePlayer = (playerTurn == COLOR_WHITE) ? whitePlayer : blackPlayer;
	}
	
	public Player getActivePlayer() {
		return activePlayer;
	}

	public void setActivePlayer(Player activePlayer) {
		this.activePlayer = activePlayer;
	}

	@Override
	public void run() {
		this.startGame();		
	}
}
