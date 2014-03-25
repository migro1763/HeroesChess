package game;

import gui.ChessBoardGui;
import gui.PieceGui;
import interfaces.Vals;
import players.Player;

public class Game implements Runnable, Vals {
	private BitBoard btb;
	private ChessBoardGui board;
	private MoveGenerator moveGen;
	private int playerTurn = COLOR_WHITE; // who's turn it is
	private Player blackPlayer = null, whitePlayer = null, activePlayer = null;
	private boolean hasCheckTestedGameLoop = false;
	private boolean dragPiecePossibleMovesDone = false;
	long testTime = 0L;
	
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
			threadPause(1000);
		}
		// set start player, white always starts game
		setPlayerTurn(playerTurn);				
		System.out.println("ChessGame: starting game flow");
		
		// start game flow loop
		boolean didMove = false;
		testTime = System.currentTimeMillis();
		while(!isGameOver()) {
			didMove = gameLoop(history);
			changePlayerTurnAfterMove(didMove);
		}
	}

	public boolean gameLoop(Move history) {
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
		
		// do this block if player has picked and is holding a piece
		// ---------------------------------------------------------
		if(dragPiece != null) {
			dragPieceMoveBits = dragPiece.getMoveBits();

			if(activePlayer.getCurrentMove() != null) {
				long moveBits = 1L<<activePlayer.getCurrentMove().getTrg();
				
				if((dragPieceMoveBits.getBits() & moveBits) != 0L) {
			    	selectedMove = activePlayer.getMove();
			    	// move the selected piece by selected move
		    		movePiece(selectedMove);		
		    		// snap the moved piece to its nearest square
		    		dragPiece.snapToNearestSquare(selectedMove.getTrg());
					// set history to last move
			    	history = selectedMove;
			    	// finish move setup for active player
			    	activePlayer.moveSuccessfullyExecuted(selectedMove);
					// do pawn promotion and finish castling setup (move rook etc.)
					moveGen.pawnPromoAndCastling(selectedMove);
			    	// recreate and reposition all gui pieces, according to latest bitboards
					board.setupGuiPieceArray();
					// repaint graphics
//			    	board.repaint();
				    hasMoved = true;
				} else {
					// -1 indicates no move was done, possibly due to wrong square selected or
					// selected square's piece cannot move at all
					hasMoved = false;
				}			
			}
		}
		// -----------------------------------------
		// end of block when player is holding piece
		
		return hasMoved;
	}
	
	public boolean isHasCheckTestedGameLoop() {
		return hasCheckTestedGameLoop;
	}

	public void setHasCheckTestedGameLoop(boolean hasGameLoopCheckTested) {
		this.hasCheckTestedGameLoop = hasGameLoopCheckTested;
	}

	public void movePiece(Move move) {
        char oppType = btb.getArraySquare(move.getTrg());
        
        // if target square is not empty = attack!
        if(oppType != ' ') {
        	// add captured piece to list of captured pieces
        	PieceGui deathPiece = new PieceGui(board.getGuiPiece(move.getTrg()));
        	board.addToCapturedGuiPieces(deathPiece);
        	// set position bit of oppType's bitboard to 0
        	btb.setBB(oppType, move.getTrg(), 0);
        	// set the guiPiece in the guiPiece array to null
        	board.setGuiPiece(move.getTrg(), null);
        	// setup and start attack animation
        	board.playAttackAnim(move, board.getGuiPiece(move.getSrc()), deathPiece);
        }
		btb.movePieceBits(move);		
	}

	private void changePlayerTurnAfterMove(boolean didMove) {
		if(didMove) {
			threadPause(50);
			setPlayerTurn(1-playerTurn);
			moveGen.isInCheck(playerTurn);
			hasCheckTestedGameLoop = false;
			dragPiecePossibleMovesDone = false;
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
	
	public Player getPlayer(int colour) {
		return (colour == COLOR_WHITE) ? whitePlayer : blackPlayer;
	}

	public void setActivePlayer(Player activePlayer) {
		this.activePlayer = activePlayer;
		board.setDebugText("active player changed to: " + this.activePlayer);
	}
	
	public void setPlayer(int pieceColor, Player playerHandler) {
		switch (pieceColor) {
			case COLOR_WHITE: this.whitePlayer = playerHandler; break;
			case COLOR_BLACK: this.blackPlayer = playerHandler; break;
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
    
    // return number for type for use with Declarations interface
    public static int getTypeNumber(char type, int colour) {
    	switch(Character.toUpperCase(type)) {
    		case 'K':	return 0 + colour * 6;
    		case 'N':	return 1 + colour * 6;
    		case 'B':	return 2 + colour * 6;
    		case 'Q':	return 3 + colour * 6;
    		case 'P':	return 4 + colour * 6;
    		case 'R':	return 5 + colour * 6;
    		default:	return -1;
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
	
	public MoveGenerator getMoveGen() {
		return moveGen;
	}
	
	public static void threadPause(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Speak.say("Thread-sleep interrupted at: " + e.getStackTrace(), true);
		}
	}

	@Override
	public void run() {
		this.startGame();		
	}
}
