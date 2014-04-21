package game;

import gui.ChessBoardGui;
import gui.PieceGui;
import interfaces.Vals;
import players.Player;

public class Game implements Runnable, Vals {
	private BitBoard btb;
	private ChessBoardGui board;
	private MoveGenerator moveGen;
//	private LinkedList historyList;
	private int playerTurn = COLOR_WHITE; // who's turn it is
	private Player blackPlayer = null, whitePlayer = null, activePlayer = null;
	private Move pawnHistory = null, currentMove = null;
	private int enPassantPos = -1;
	private int gameState = playerTurn;
	private int turn = 1;
	
	public Game() {
		btb = new BitBoard(this);
		moveGen = new MoveGenerator(this);
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
		this.turn = clone.turn;
	}
	
	public void startGame() {
		// check if all players are ready
		System.out.print("HeroesChess: waiting for players");
		while (blackPlayer == null || whitePlayer == null) {
			// players are still missing
			threadPause(1000);
			System.out.print(".");
		}
		System.out.println();
		// set start player, white always starts game
		playerTurn = COLOR_WHITE;
		setGameState(playerTurn);
		activePlayer = whitePlayer;
		System.out.println("Active player: " + activePlayer);
		System.out.println("ChessGame: starting game flow");
		AlgebraicNotation algNot = new AlgebraicNotation(btb);
		board = new ChessBoardGui(this);
		// start game flow loop
		boolean didMove = false;
		while(!isGameOver()) {
			didMove = gameLoop(algNot);
			threadPause(50);
			changePlayerTurnAfterMove(didMove);
		}
		
		/** game won and over
		 * play animation of losing player's king dying
		 */
		setGameState(playerTurn == COLOR_WHITE ? BLACK_WON : WHITE_WON);
		board.playKingDeath(playerTurn);
	}

	public boolean gameLoop(AlgebraicNotation algNot) {
		Move selectedMove = null;
		boolean hasMoved = false;
		// update OCCUPIED and EMPTY in the MoveGenerator object
		moveGen.updateBBStates();
		if(currentMove != null)
			board.setDebugText("Current move: " + currentMove);
		// repaint graphics
		board.repaint();
		// do this block if player has picked and dropped a piece thus making a move
		// ---------------------------------------------------------
		if(currentMove != null && board.getDragPiece() != null) {
			long moveTargetBits = 1L<<currentMove.getTrg();
			PieceGui dragPiece = board.getDragPiece();
			// test if the square dragPiece has been dropped into is part of valid moves
			if((dragPiece.getMoveBits().getBits() & moveTargetBits) != 0L) {
				// make a clone of the current game BitBoard
				BitBoard preGameBB = new BitBoard(btb);
		    	selectedMove = activePlayer.getMove();
		    	Speak.say("selectedMove: " + selectedMove, true);
		    	// move the selected piece by selected move
		    	movePiece(selectedMove);		
	    		// snap the moved piece to its nearest square
	    		dragPiece.snapToNearestSquare(selectedMove.getTrg());
		    	// finish move setup for active player
		    	activePlayer.moveSuccessfullyExecuted(selectedMove);
				// do pawn promotion and finish castling setup (move rook etc.)
				moveGen.pawnPromotion(selectedMove);
				// derive algebraic chess notation of move
				String notation = algNot.getNotation(selectedMove, preGameBB);
		        if(notation != null)
		        	board.setHistoryText(String.format("%03d", turn) + ". " + notation + " ");
				// repaint graphics
				board.repaint();
			    hasMoved = true;
			} 
		}			
		// -----------------------------------------
		// end of block when player is holding piece
		
		return hasMoved;
	}
	
	public void setCurrentMove(Move move) {
		currentMove = move;
	}
	
	public Move getCurrentMove() {
		return currentMove;
	}

	public void movePiece(Move move) {
		char type = btb.getArraySquare(move.getSrc());
        char oppType = btb.getArraySquare(move.getTrg());
        int posOfMovedPiece = move.getTrg();
        int attackedPiecePos = posOfMovedPiece;
        
        // en passant attack
        // if enPassantPos is not -1, that means an en passant attack has been made
        if(enPassantPos >= 0 && Math.abs(move.getSrc() - enPassantPos) == 1) {
        	 oppType = btb.getArraySquare(enPassantPos);
        	 attackedPiecePos = enPassantPos;
        	 board.setDebugText("en passant!");
        }       
        
        // if target square is not empty = attack!
        if(oppType != ' ') {
        	// add captured piece to list of captured pieces
        	PieceGui deathPiece = new PieceGui(board.getGuiPiece(attackedPiecePos));
        	board.addToCapturedGuiPieces(deathPiece);
        	// set position bit of oppType's bitboard to 0
        	btb.setBB(oppType, attackedPiecePos, 0);
        	// setup and start attack animation
        	board.playAttackAnim(move, board.getGuiPiece(move.getSrc()), deathPiece);
        }
		btb.movePieceBits(move);
		
		// store pawn double push to history for en passant next turn
		if(Character.toUpperCase(type) == 'P' && 
				Math.abs(move.getTrg() - move.getSrc()) == 16) // double push
			pawnHistory = move;
		else
			pawnHistory = null;
		
		// if moved piece is a king, negate both castling possibilities
		if(Character.toUpperCase(type) == 'K') {
			activePlayer.setkSideCastling(false);
			activePlayer.setqSideCastling(false);
			
			// test if move was castling and if so, move corresponding rook
			if(move.equals(K_CASTLING_MOVE[playerTurn]) | move.equals(Q_CASTLING_MOVE[playerTurn]))
				moveGen.moveRookInCastling(move);
		}			
		// if moved piece is a rook, negate corresponding side castling possibility
		if(Character.toUpperCase(type) == 'R')
			if(posOfMovedPiece == 0 || posOfMovedPiece == 56) // if left side of board
				activePlayer.setqSideCastling(false);
			else if(posOfMovedPiece == 7 || posOfMovedPiece == 63) // if right side of board
				activePlayer.setkSideCastling(false);
		
		if(enPassantPos < 0)
			attackedPiecePos = -1;
		enPassantPos = -1; // reset position of en passant attackee
		board.updateGuiPieces(move, attackedPiecePos);
	}
	
//	// called when mouse is released thus dragged piece dropped
//	public void setNewPieceLocation(PieceGui draggedPiece, int targetPos) {
//		// if dragPiece hasn't moved outside of start square, snap back to start
////		if(draggedPiece.getPos() == targetPos)
////			draggedPiece.snapToNearestSquare();
//		// else move dragPiece to targetPos square
//		if(draggedPiece.getMoveBits() != null) {
//			Move move = new Move(draggedPiece.getPos(), targetPos);
//			if((draggedPiece.getMoveBits().getBits() & 1L<<targetPos) > 0L) {
//				activePlayer.setCurrentMove(move);
//			} else {
//				// if target square wasn't part of valid moves, snap back to start
//				draggedPiece.snapToNearestSquare();
//			}
//		}
//		// thread pause 0.1 seconds
//		threadPause(120);
//	}

	private void changePlayerTurnAfterMove(boolean didMove) {
		if(didMove) {
			setPlayerTurn(1-playerTurn);
			// test for check
			activePlayer.setCheck(moveGen.isInCheck(playerTurn));
			// test for check mate
			activePlayer.setCheckMate(moveGen.testForCheckMate(playerTurn));
		}		
	}
	
	public int getPlayerTurn() {
		return playerTurn;
	}

	public void setPlayerTurn(int playerTurn) {
		this.playerTurn = playerTurn;
		turn += this.playerTurn == COLOR_WHITE ? 1 : 0;
		setGameState(playerTurn);
		setActivePlayer(this.playerTurn == COLOR_WHITE ? whitePlayer : blackPlayer);
		activePlayer.setDragPiecesEnabled(true);
	}
	
	public int getGameState() {
		return gameState;
	}
	
	public void setGameState(int gameState) {
		this.gameState = gameState;
	}
	
	public Player getActivePlayer() {
		return activePlayer;
	}
	
	public Player getPlayer(int colour) {
		return (colour == COLOR_WHITE) ? whitePlayer : blackPlayer;
	}

	public void setActivePlayer(Player activePlayer) {
		this.activePlayer = activePlayer;
	}
	
	public void setPlayer(int pieceColor, Player playerHandler) {
		switch (pieceColor) {
			case COLOR_WHITE: this.whitePlayer = playerHandler; break;
			case COLOR_BLACK: this.blackPlayer = playerHandler; break;
			default: 
				throw new IllegalArgumentException("Invalid pieceColor: " + pieceColor);
		}
	}
	
	public Move getPawnHistory() {
		return pawnHistory;
	}

	public void setPawnHistory(Move pawnHistory) {
		this.pawnHistory = pawnHistory;
	}
	

	public void setEnPassantPos(int pos) {
		enPassantPos = pos;
	}

	private boolean isGameOver() {
		if(activePlayer.isCheckMate())
			board.setDebugText(activePlayer + " is check mate!");
		return activePlayer.isCheckMate();
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
    
    public static int getTypeColour(char type) {
    	return Character.isUpperCase(type) ? COLOR_WHITE : COLOR_BLACK;
    }
    
    public static String getLongName(char type) {
    	int colour = (Character.isUpperCase(type)) ? 0 : 1;
    	return getLongName(type, colour);
    }
	
	public static String makeStdPos(int pos) {
		return FILE_NAME[BitBoard.getX(pos)] + (8-(BitBoard.getY(pos)));
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
