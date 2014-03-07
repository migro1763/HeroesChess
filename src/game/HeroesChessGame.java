package game;

import java.util.ArrayList;
import java.util.List;

import interfaces.Vals;

public class HeroesChessGame {
	
	public static final String[] COLOUR = {"WHITE", "black"};
	private BitBoard btb;
	private Moves moves;
	private int playerTurn = 1; // who's turn it is, 0 = white, 1 = black	
	public static long empty = 0L;
	public static boolean isChecked = false;

	public HeroesChessGame() {
		this.btb = new BitBoard(this);
		this.moves = new Moves(this);
		gameLoop();
	}
	
	public void gameLoop() {
		String moveList = "";
		String history = "1333"; // bp d7->d5
		int selectedMove = 0, start = 0, end = 0;
		List<String> listOfMoves = new ArrayList<String>(); // list of moves in "x1y1x2y2"-format
		List<String> listOfStdMoves = new ArrayList<String>(); // list of moves in standard form (i.e. "a1->a2")
		
		// main game loop
		do {
			// if selectedMove is -1 then player gets another chance (turn doesn't change)
			if(selectedMove > -1) 
				playerTurn = 1-playerTurn;
			System.out.println("\n====> Player turn: " + COLOUR[playerTurn]);
			
			moves.updateBBStates(); // update OCCUPIED and EMPTY in the Moves object
			btb.drawArray(); 		// draw current chess board
			listOfMoves.clear(); 	// clear/empty the moves list
			listOfStdMoves.clear(); // clear/empty the moves in standard form list
			
			isChecked = moves.isInCheck(playerTurn);
			if(isChecked)	// tell whether current player is in check
				System.out.println("\n!! => " + COLOUR[playerTurn] + " is in check!");
			
//			System.out.print("\n-1 for testing whether start/end space is empty.");
			int pieceToMove = -1;
			while(!(pieceToMove > -1 && pieceToMove < 64))
				pieceToMove = Integer.parseInt(HeroesChess.ask("Select square of piece to move [0-63]: "));
			if(pieceToMove < 0) {
				start = Integer.parseInt(HeroesChess.ask("Select start square [0-63]: "));
				end = Integer.parseInt(HeroesChess.ask("Select end square [0-63]: "));
				boolean test = moves.isEmptyBetween(start, end);
				String msg = "\nInfo => Space is " + ((test) ? "empty" : "not empty!");
				System.out.println(msg);
			} else {
				moveList = moves.possibleMoves(playerTurn, pieceToMove, history);
				if(!moveList.isEmpty()) {
					System.out.println("Moves possible for " + btb.getArraySquare(pieceToMove) + " at " + makeStdPos(pieceToMove));		
					for(int i = 0; i < moveList.length(); i+=4) {
						listOfMoves.add(moveList.substring(i, i+4));
						int[] coords = parseMove(moveList.substring(i, i+4));
						listOfStdMoves.add(makeStdMove(coords[0], coords[1], coords[2], coords[3]));
					}	
				}
			
				if(listOfMoves.size() > 0) {
					selectedMove = HeroesChess.ask("Select move [-2 = quit] =>", listOfStdMoves.toArray(new String[listOfStdMoves.size()]));
					if(selectedMove >= 0)
						btb.movePiece(listOfMoves.get(selectedMove));
				} else
					// -1 indicates no move was done, possibly due to wrong square selected or
					// selected square's piece cannot move at all
					selectedMove = -1; 
			}
		} while (selectedMove > -2);
	}
	
//	public String makeMove(Coordinate start, Coordinate end) {
////		String capturedPiece = chessBoard[end.getRow()][end.getColumn()];
//		return ("" + start.getRow() + start.getColumn() + 
//					end.getRow() + end.getColumn());
//	}
//	
//	public String makeMove(int x1, int y1, int x2, int y2) {
//		return makeMove(new Coordinate(x1, y1), new Coordinate(x2, y2));
//	}
	
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
