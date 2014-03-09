package game;

import java.util.ArrayList;
import java.util.List;

import interfaces.Vals;

public class HeroesChessGame {
	
	public static final String[] COLOUR = {"WHITE", "black"};
	
	private BitBoard btb;
	private Moves moves;
	private int playerTurn = 1; // who's turn it is, 0 = white, 1 = black	
	public static boolean[] isChecked = {false, false}; // set for each player if checked
	public static boolean[] kSideCastling = {true, true}; // true if player colour can castle king side.
	public static boolean[] qSideCastling = {true, true}; // true if player colour can castle king side.
	
	public HeroesChessGame() {
		this.btb = new BitBoard(this);
		this.moves = new Moves(this);
		gameLoop();
	}
	
	public void gameLoop() {
		String moveList = "";
		String history = "";
		int selectedMove = 0, start = 0, end = 0;
		List<String> listOfMoves = new ArrayList<String>(); // list of moves in "x1y1x2y2"-format
		List<String> listOfStdMoves = new ArrayList<String>(); // list of moves in standard form (i.e. "a1->a2")
		
		// main game loop
		do {
			// if selectedMove is -1 then player gets another chance (turn doesn't change)
			if(selectedMove > -1) 
				playerTurn = 1-playerTurn;
			Speak.say("\n====> Player turn: " + COLOUR[playerTurn], true);
			
			Speak.say("Info => Can do king side castling: " + kSideCastling[playerTurn], true);
			Speak.say("Info => Can do queen side castling: " + qSideCastling[playerTurn], true);
			
			moves.updateBBStates(); // update OCCUPIED and EMPTY in the Moves object
			btb.drawArray(); 		// draw current chess board
			listOfMoves.clear(); 	// clear/empty the moves list
			listOfStdMoves.clear(); // clear/empty the moves in standard form list
			
			isChecked[playerTurn] = moves.isInCheck(playerTurn);
			if(isChecked[playerTurn])	// tell whether current player is in check
				Speak.say("\n!! => " + COLOUR[playerTurn] + " is in check!", true);
			
//			Speak.say("\n-1 for testing whether start/end space is empty.");
			int posOfPieceToMove = 64;
			while(posOfPieceToMove > 63)
				posOfPieceToMove = Integer.parseInt(Speak.ask("Select square of piece to move [0-63]: "));
			// if -1 then check start->end space emptiness
			if(posOfPieceToMove < 0) {
				 // if less than -1 then exit program
				if(posOfPieceToMove < -1) {
					Speak.say("\n!! => Goodbye!", true);
					System.exit(0);
				}
				start = Integer.parseInt(Speak.ask("Select start square [0-63]: "));
				end = Integer.parseInt(Speak.ask("Select end square [0-63]: "));
				boolean test = moves.isEmptyBetween(start, end);
				Speak.say("\nInfo => Space is " + ((test) ? "empty" : "not empty!"), true);
			} else {
				// generate string of moves in Moves->possibleMoves() method
				moveList = moves.possibleMoves(playerTurn, posOfPieceToMove, history);
				
				if(!moveList.isEmpty()) {
					char typeToMove = btb.getArraySquare(posOfPieceToMove);
					Speak.say("Moves possible for " + typeToMove + " at " + makeStdPos(posOfPieceToMove));		
					for(int i = 0; i < moveList.length(); i+=4) {
						listOfMoves.add(moveList.substring(i, i+4));
						int[] coords = parseMove(moveList.substring(i, i+4));
						listOfStdMoves.add(makeStdMove(coords[0], coords[1], coords[2], coords[3]));
					}
					selectedMove = Speak.ask("Select move [-2 = quit] =>", listOfStdMoves.toArray(new String[listOfStdMoves.size()]));
					if(selectedMove >= 0) {
					    long moveBits = 1L<<btb.getPosFromCoords(btb.getPosFromMove(listOfMoves.get(selectedMove), 2),
					    									btb.getPosFromMove(listOfMoves.get(selectedMove), 3));
						if(!moves.testCheck(playerTurn, moveBits)) {				
							// move the selected piece by selected move
							btb.movePiece(listOfMoves.get(selectedMove));
							// set history to last move
							history = listOfMoves.get(selectedMove);
							// if moved piece is a king, negate both castling possibilities
							if(Character.toUpperCase(typeToMove) == 'K') {
								kSideCastling[playerTurn] = qSideCastling[playerTurn] = false;
								int offset = (playerTurn == 0) ? 56 : 0;
								// if king move was a castling, also move the corresponding rook
								if(listOfMoves.get(selectedMove).equals(makeMove(4 + offset, 6 + offset)))
									btb.movePiece(makeMove(7 + offset, 5 + offset));
								else if(listOfMoves.get(selectedMove).equals(makeMove(4 + offset, 2 + offset)))
									btb.movePiece(makeMove(0 + offset, 3 + offset));
							}
							
							// if moved piece is a rook, negate corresponding side castling possibility
							if(Character.toUpperCase(typeToMove) == 'R')
								if(posOfPieceToMove == 0 || posOfPieceToMove == 56) // if left side of board
									qSideCastling[playerTurn] = false;
								else if(posOfPieceToMove == 7 || posOfPieceToMove == 63) // if right side of board
									kSideCastling[playerTurn] = false;			
						} else
							selectedMove = -1;
					}
				} else
					// -1 indicates no move was done, possibly due to wrong square selected or
					// selected square's piece cannot move at all
					selectedMove = -1;
				
// the old way of testing the above, saved just in case
//				if(listOfMoves.size() > 0) {
//					selectedMove = Speak.ask("Select move [-2 = quit] =>", listOfStdMoves.toArray(new String[listOfStdMoves.size()]));
//					if(selectedMove >= 0)
//						btb.movePiece(listOfMoves.get(selectedMove));
//				} else
//					// -1 indicates no move was done, possibly due to wrong square selected or
//					// selected square's piece cannot move at all
//					selectedMove = -1; 
			}
		} while (selectedMove > -2);
	}
	
	public String makeMove(int start, int end) {
		return ("" + (start/8) + (start%8) + (end/8) + (end%8));
	}
	
	public String makeMove(int x1, int y1, int x2, int y2) {
		return ("" + x1 + y1 + x2 + y2);
	}
	
	public String makeStdMove(int x1, int y1, int x2, int y2) {
		return ("" + Vals.FILE_NAME[y1] + (8-x1) + "->" + Vals.FILE_NAME[y2] + (8-x2));
	}
	
	public String makeStdMove(int[] coords) {
		return makeStdMove(coords[0], coords[1], coords[2], coords[3]);
	}
	
	public String makeStdPos(int pos) {
		return Vals.FILE_NAME[pos % 8] + (8-(pos / 8));
	}
	
	public int[] parseMove(String move) {
		int[] moveCoords = new int[4];
		for(int i = 0; i < 4; i++)
			moveCoords[i] =	Integer.parseInt(move.substring(i, i+1));
		return moveCoords;
	}

	public BitBoard getBitBoard() {
		return btb;
	}

	public void setBitBoard(BitBoard bitBoard) {
		this.btb = bitBoard;
	}
}
