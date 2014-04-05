package game;

import gui.OptionsDiag;
import interfaces.Declarations;
import interfaces.Vals;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class MoveGenerator implements Vals, Declarations {
	private static final long FILE_AB = 217020518514230019L; // 1's on all of column A + B (left side)
	private static final long FILE_GH = -4557430888798830400L; // 1's on all of column G + H (right side)
//	private static final long CENTRE=103481868288L; // 1's on the 4 most centered squares
//	private static final long EXTENDED_CENTRE=66229406269440L; // 1's on the 16 most centered squares
//	private static final long KING_SIDE=-1085102592571150096L; // 1's on all squares on right half of board
//	private static final long QUEEN_SIDE=1085102592571150095L; // 1's on all squares on left half of board
	private static long OCCUPIED = 0L;
	private static long EMPTY = 0L;

	// mask arrays	
    static long rankMask[] =/*from rank1 to rank8*/
    {
        0xFFL, 0xFF00L, 0xFF0000L, 0xFF000000L, 0xFF00000000L, 0xFF0000000000L, 0xFF000000000000L, 0xFF00000000000000L
    };
    static long fileMask[] =/*from fileA to FileH*/
    {
        0x101010101010101L, 0x202020202020202L, 0x404040404040404L, 0x808080808080808L,
        0x1010101010101010L, 0x2020202020202020L, 0x4040404040404040L, 0x8080808080808080L
    };
    static long diagMask[] =/*from top left to bottom right*/
    {
	0x1L, 0x102L, 0x10204L, 0x1020408L, 0x102040810L, 0x10204081020L, 0x1020408102040L,
	0x102040810204080L, 0x204081020408000L, 0x408102040800000L, 0x810204080000000L,
	0x1020408000000000L, 0x2040800000000000L, 0x4080000000000000L, 0x8000000000000000L
    };
    static long aDiagMask[] =/*from top right to bottom left*/
    {
	0x80L, 0x8040L, 0x804020L, 0x80402010L, 0x8040201008L, 0x804020100804L, 0x80402010080402L,
	0x8040201008040201L, 0x4020100804020100L, 0x2010080402010000L, 0x1008040201000000L,
	0x804020100000000L, 0x402010000000000L, 0x201000000000000L, 0x100000000000000L
    };
		
	public Game game;
	public BitBoard gameBB;
	
	public MoveGenerator(Game game) {
		this.game = game;
		this.gameBB = game.getBitBoard();
	}
	
	// return boolean for whether space between start and end is empty
	public boolean isEmptyBetween(int start, int end) {
		long testBits = 0L;
		EMPTY = gameBB.getEmpty();
		// if start is the higher value, swap
		if(start > end) {
			int temp = end;
			end = start;
			start = temp;
		}
		// if space between start/end is modulo 7, 8 or 9 = 0 then do vertical
		int space = end - start;
		int step = 1;
		if(space % 8 == 0)
			step = 8;
		else if(space % 7 == 0)
			step = 7;
		else if(space % 9 == 0)
			step = 9;
		start += step; // increase start by step, so to not include start pos in test
		for (int i = start; i < end; i += step)
			testBits |= 1L<<i;

		return (((EMPTY & testBits) ^ testBits) == 0L);
	}
	
	// rook, whichDir: (int) 0 = horizontal, 1 = vertical, >1 = both
	public long getHoriVertMoves(int pos, int colour, BitBoard testBB, int whichDir) {
		long binPos = 1L<<verifyPosition(pos); // convert pos to binary
		long occupied = testBB.getOccupied();
		long possibleHoris =  ((occupied - 2 * binPos)^Long.reverse(Long.reverse(occupied)-2 * Long.reverse(binPos)));
		possibleHoris &= rankMask[(pos / 8)];
		long possibleVerts = ((occupied & fileMask[(pos % 8)]) - (2 * binPos))^
				Long.reverse(Long.reverse(occupied & fileMask[(pos % 8)]) - (2 * Long.reverse(binPos)));
		possibleVerts &= fileMask[(pos % 8)];
		switch(whichDir) {
			case 0 : return possibleHoris;
			case 1 : return possibleVerts;
			default: return (possibleHoris | possibleVerts);
		}
	}
	
	// rook
	public long getHoriVertMoves(int pos, int colour, BitBoard testBB) {
		return getHoriVertMoves(pos, colour, testBB, 2);
	}
	
	// all rooks
	public long getHoriVertMoves(int colour, BitBoard testBB) {
		long rookMoveBits = 0L;
		long rooksBB = testBB.getBB((colour == 0) ? 'R' : 'r').getBits();
		int[] rookPos = BitBoard.getMultiPos(rooksBB);
		for (int i = 0; i < rookPos.length; i++)
			rookMoveBits |= getHoriVertMoves(verifyPosition(rookPos[i]), colour, testBB);
		return rookMoveBits;
	}
	
	// bishop, whichDiah: (int) 0 = diag, 1 = antiDiag, >1 = both
	public long getDiagAntiDiagMoves(int pos, int colour, BitBoard testBB, int whichDiag) {
		long binPos = 1L<<verifyPosition(pos); // convert pos to binary
		long occupied = testBB.getOccupied();
		long possibleDiags = ((occupied & diagMask[pos / 8 + pos % 8]) - (2 * binPos)) ^ 
				Long.reverse(Long.reverse(occupied & diagMask[pos / 8 + pos % 8]) - (2 * Long.reverse(binPos)));
		possibleDiags &= diagMask[pos / 8 + pos % 8];
		long possibleAntiDiags = ((occupied & aDiagMask[pos / 8 + 7 - pos % 8]) - (2 * binPos)) ^ 
				Long.reverse(Long.reverse(occupied & aDiagMask[pos / 8 + 7 - pos % 8]) - (2 * Long.reverse(binPos)));
		possibleAntiDiags &= aDiagMask[pos / 8 + 7 - pos % 8];
		switch(whichDiag) {
			case 0 : return possibleDiags;
			case 1 : return possibleAntiDiags;
			default: return (possibleDiags | possibleAntiDiags);
		}
	}
	
	// bishop
	public long getDiagAntiDiagMoves(int pos, int colour, BitBoard testBB) {	
		return getDiagAntiDiagMoves(pos, colour, testBB, 2);
	}
	
	// all bishops
	public long getDiagAntiDiagMoves(int colour, BitBoard testBB) {
		long bishopMoveBits = 0L;
		long bishopsBB = testBB.getBB((colour == 0) ? 'B' : 'b').getBits();
		int[] bishopPos = BitBoard.getMultiPos(bishopsBB);
		for (int i = 0; i < bishopPos.length; i++)
			bishopMoveBits |= getDiagAntiDiagMoves(verifyPosition(bishopPos[i]), colour, testBB);
		return bishopMoveBits;
	}
	
	// knight
	public long getKnightMoves(int pos, int colour) {
		long binPos = 1L<<verifyPosition(pos); // convert pos to binary
		// (clock-wise) n-e, s-e, s-w, n-w, e-n, e-s, w-s, w-n
		return ((binPos>>>15 | binPos<<17) & ~fileMask[0]) |
				((binPos<<15 | binPos>>>17) & ~fileMask[7]) |
				((binPos>>>6 | binPos<<10) & ~FILE_AB) |
				((binPos<<6 | binPos>>>10) & ~FILE_GH);
	}
	
	// all knights
	public long getKnightMoves(int colour, BitBoard testBB) {
		long knightMoveBits = 0L;
		long knightsBits = testBB.getBB((colour == 0) ? 'N' : 'n').getBits();
		int[] knightPos = BitBoard.getMultiPos(knightsBits);
		for (int i = 0; i < knightPos.length; i++)
			knightMoveBits |= getKnightMoves(verifyPosition(knightPos[i]), colour);
		return knightMoveBits;
	}
	
	// pawn
	public long getPawnMoves(Move history, int pos, int colour, boolean attackOnly, BitBoard testBB) {
		long binPos;
		long andBits = 0L;
		long empty = testBB.getEmpty();
		if(pos < 0) { // if pos < 0 return moves of all pawns
			binPos = testBB.getBB((colour == COLOR_WHITE) ? 'P' : 'p').getBits();
			pos = 0;
		} else
			binPos = 1L<<verifyPosition(pos); // convert pos to binary
		
		// attacks, if opponents present in attackable squares. 
		// ~fileMasks are for removing wraps over to opposite sides
		andBits = ( (colour == COLOR_WHITE) ? (binPos>>>7 & ~fileMask[0]) | (binPos>>>9 & ~fileMask[7]) :
					(binPos<<7 & ~fileMask[7]) | (binPos<<9 & ~fileMask[0]) );
		
		// check for and add en passant attack if history is not empty
		long enPassantAttackBits = 0L;
		if(history != null) {		
			// en passant
			long enemyPawnNextToMask = (1L<<history.getTrg()>>>1 | // left of
										1L<<history.getTrg()<<1); // right of
				
			enPassantAttackBits |= (colour == COLOR_WHITE) ? 
						// the square white would attack
						(((enemyPawnNextToMask & binPos)>>>7 & empty) | // test left side of b
						((enemyPawnNextToMask & binPos)>>>9 & empty)) & (1L<<history.getSrc())<<8 : // test right side of b
						// the square black would attack
						(((enemyPawnNextToMask & binPos)<<7 & empty) | // test left side of w
						((enemyPawnNextToMask & binPos)<<9 & empty)) & (1L<<history.getSrc())>>>8; // test right side of w
						
			andBits |= enPassantAttackBits;
		}

		if(!attackOnly) {
			// AND attack bits with opponent's pieces, 
			andBits &= (testBB.getColourPieces(1-colour) | enPassantAttackBits);
			// regular moves, only legal on empty squares
			// single push
			long singlePush = (colour == 0 ? binPos>>>8 : binPos<<8) & empty;
			andBits |= singlePush;
			// double push
			if(singlePush != 0L) // can't do double push if not single push (pawns can't jump)
				andBits |= (colour == 0 ? (((binPos & rankMask[6])>>>16) & binPos>>>16) : 
					(((binPos & rankMask[1])<<16) & binPos<<16)) & empty;
		}
		if(enPassantAttackBits != 0L)
			game.setEnPassantPos(history.getTrg());
		return andBits;
	}
	
	public long getQueenMoves(int pos, int colour, BitBoard testBB) {
		long moveBits = getHoriVertMoves(pos, colour, testBB);
		return moveBits |= getDiagAntiDiagMoves(pos, colour, testBB);
	}
		
	// all queens
	public long getQueenMoves(int colour, BitBoard testBB) {
		BB queensBB = testBB.getBB((colour == 0) ? 'Q' : 'q');
		int queensCount = Long.bitCount(queensBB.getBits());
		int[] queenPositions = BitBoard.getMultiPos(queensBB.getBits());
		long moveBits = 0L;
		for(int pos = 0; pos < queensCount; pos++) {
			moveBits |= getHoriVertMoves(queenPositions[pos], colour, testBB);
			moveBits |= getDiagAntiDiagMoves(queenPositions[pos], colour, testBB);
		}
		return moveBits; 
	}
	
	// king
	public long getKingMoves(int colour, BitBoard testBB) {
		long binPos = 1L<<testBB.getKingPos(colour); // convert pos to binary
		// (clock-wise) n, n-e, e, s-e, s, s-w, w, n-w
		long andBits = ((binPos>>>8 | binPos>>>7 | binPos<<1 | binPos<<9) & ~fileMask[0]) |
				((binPos<<8 | binPos<<7 | binPos>>>1 | binPos>>>9) & ~fileMask[7]);
		
		// castling, check both sides (king & queen)
		Move castlingMove = null;
		boolean[] sidesCastling = {game.getPlayer(colour).iskSideCastling(),
				game.getPlayer(colour).isqSideCastling()};
		for (int side = 0; side < 2; side++) {
	    	if(sidesCastling[side]) {
	    		castlingMove = getCastling(colour, side);
	    		if(castlingMove != null)
	    			andBits |= 1L<<castlingMove.getTrg();
	    	}
		}	
		return getKingSafe(colour, andBits);
	}
	
	// return bitboard of type attacks from player colour, type = 'a' returns all types
	public long getAttacks(char type, int colour, BitBoard testBB) {
		long inversePieces = ~testBB.getColourPieces(colour);
		switch(type) {
			case 'p':	return getPawnMoves(null, -1, colour, true, testBB);
			case 'r':	return getHoriVertMoves(colour, testBB) & inversePieces;
			case 'n':	return getKnightMoves(colour, testBB) & inversePieces;
			case 'b':	return getDiagAntiDiagMoves(colour, testBB) & inversePieces;
			case 'q':	return getQueenMoves(colour, testBB) & inversePieces;
			case 'a':	long pawns = getPawnMoves(null, -1, colour, true, testBB);
						long rooks = getHoriVertMoves(colour, testBB) & inversePieces;			
						long knights = getKnightMoves(colour, testBB) & inversePieces;
						long bishops = getDiagAntiDiagMoves(colour, testBB) & inversePieces;
						long queens = getQueenMoves(colour, testBB) & inversePieces;
						return pawns|rooks|knights|bishops|queens;
			default:	return 0L;
		}
	}
	
	// returns the type of opponent piece that is checking current player's king
	public char pieceTypeChecking(int colour, BitBoard testBB) {
		long kingBinPos = 1L<<testBB.getKingPos(colour);
		if((getAttacks('p', 1-colour, testBB) & kingBinPos) != 0L) {
			return 'p'; // test if pawns attack
		}
		else if((getAttacks('r', 1-colour, testBB) & kingBinPos) != 0L) {
			return 'r'; // test if rooks attack
		}
		else if((getAttacks('n', 1-colour, testBB) & kingBinPos) != 0L) {
			return 'n'; // test if knights attack
		}
		else if((getAttacks('b', 1-colour, testBB) & kingBinPos) != 0L) {
			return 'b'; // test if bishops attack
		}
		else if((getAttacks('q', 1-colour, testBB) & kingBinPos) != 0L) {
			return 'q'; // test if queen(s) attack(s)
		}
		else return 0;		
	}
	
	// get total OR'ed bits of all attacks from king pos as rook, bishop, pawn and knight,
	// AND'ed with corresponding enemy type
	public long attackFromKing(int colour, BitBoard testBB) {
		int kingPos = testBB.getKingPos(colour);
		long rookAttacks = getHoriVertMoves(kingPos, colour, testBB) & ~gameBB.getColourPieces(colour);
		long bishopAttacks = getDiagAntiDiagMoves(kingPos, colour, testBB) & ~gameBB.getColourPieces(colour);
		long pawnAttacks = getPawnMoves(null, kingPos, colour, true, testBB) & ~gameBB.getColourPieces(colour);
		long knightAttacks = getKnightMoves(kingPos, colour) & ~gameBB.getColourPieces(colour);
		
		rookAttacks &= gameBB.getBB(colour==COLOR_WHITE ? 'r':'R').getBits();
		bishopAttacks &= gameBB.getBB(colour==COLOR_WHITE ? 'b':'B').getBits();
		pawnAttacks &= gameBB.getBB(colour==COLOR_WHITE ? 'p':'P').getBits();
		knightAttacks &= gameBB.getBB(colour==COLOR_WHITE ? 'n':'N').getBits();
		
		return (rookAttacks|bishopAttacks|pawnAttacks|knightAttacks);		
	}
	
	public boolean isInCheck(int colour, BitBoard testBB) {
		if((pieceTypeChecking(colour, testBB) == 0)) 
			return false; // not in check
		else 
			return true; // in check!
	}
	
	public boolean isInCheck(int colour) {
		return isInCheck(colour, gameBB);
	}
	
//	// tests if bitboard of moves and bits of opponent piece checking king, will be zero.
//	// if zero, king is still in check and true is returned. otherwise return false (no longer in check)
//	public boolean testCheck(int colour, long moveBits) {
//		long culpritBits = 0L; // bitboard for opponent type checking's bits
//		char oppTypeChecking = pieceTypeChecking(colour, gameBB);
//		Speak.say("testCheck", true);
//		if(oppTypeChecking > 0) { // if it's zero (no char value), then pieceTypeChecking() didn't find the culprit type
//			culpritBits |= gameBB.getBB(oppTypeChecking).getBits(); // set to opponent type checking's bits
//			if((moveBits & culpritBits) == 0L) {
//				game.getBoard().setDebugText("King still in check!");
//				return true;					
//			}
//		}
//		game.getActivePlayer().setCheck(false); // current player is no longer in check
//		return false;
//	}
	
	public long validMovesNotCausingCheck(char pieceType, long moveBits, int startPos) {
		int[] movePositions = BitBoard.getMultiPos(moveBits);
		char oppType;
		for(int posi : movePositions) {
			BitBoard testBB = new BitBoard(gameBB);
			oppType = testBB.getArraySquare(posi); // get possible opponent at target pos
			if(oppType != ' ')
				testBB.getBB(oppType).mulBits(~(1L<<posi)); // remove opponent from target pos
			testBB.movePieceBits(new Move(startPos, posi)); // move piece to target pos
			if(isInCheck(Game.getTypeColour(pieceType), testBB)) {
				moveBits &= ~(1L<<posi); // remove the move pos if it causes check(s)
			}
		}
		return moveBits;
	}
	
	public boolean testForCheckMate(int colour) {
		for (char type : PIECE_NAME) {
			long typeBits = gameBB.getBB(type, colour).getBits();
			if(typeBits != 0L) {
				int[] positions = BitBoard.getMultiPos(typeBits);
				for (int pos : positions) {
					if(possibleMoves(colour, pos, game.getPawnHistory()).getBits() != 0L) 
						return false;
				}
			}
		}	
		return true;
	}
	
	// get bitboard of safe moves for king, i.e. moves not resulting in a check
	public long getKingSafe(int colour, long kingMoves, BitBoard testBB) {
		// return (bitboard of all opponent's attacks AND king's moves) XOR king's moves
		return (getAttacks('a', 1-colour, testBB) & kingMoves) ^ kingMoves;
	}
	
	public long getKingSafe(int colour, long kingMoves) {
		return getKingSafe(colour, kingMoves, gameBB);
	}
	
	public Move getCastling(int colour, int side) {
        Move move = null;
        long rookBB = gameBB.getBB((colour == 0) ? 'R' : 'r').getBits();
        int offset = (colour == 0) ? 56 : 0; // offset castling rooks by 56 if white
        
        if(!isInCheck(colour)) {
        	switch(side) {   	
	        	case 0:
			        // king side castling
			        if (((1L<<(0 + offset)) & rookBB) != 0L)
			            if ((OCCUPIED & ((1L<<(5 + offset))|(1L<<(6 + offset)))) == 0)
			                move = K_CASTLING_MOVE[colour];
			        break;
	        	case 1:
			        // queen side castling
			        if (((1L<<(7 + offset)) & rookBB) != 0L)
			            if ((OCCUPIED & ((1L<<(1 + offset))|(1L<<(2 + offset))|(1L<<(3 + offset)))) == 0)
				            move = Q_CASTLING_MOVE[colour];
			        break;
        	}
        }
        return move;
    }
	
	// since king move was a castling, also move the corresponding rook
	public void moveRookInCastling(Move selectedMove) {
		game.getBoard().setDebugText("moving rook in castling");
		int offset = (game.getPlayerTurn() == COLOR_WHITE) ? 56 : 0;
		if(selectedMove.getTrg() == 6 + offset) // king side castling
			game.movePiece(new Move(7 + offset, 5 + offset));
		else if(selectedMove.getTrg() == 2 + offset) // queen side castling
			game.movePiece(new Move(0 + offset, 3 + offset));
	}
	
	public void pawnPromotion(Move selectedMove) {
		// pawn promotion?
		int posOfMovedPiece = selectedMove.getTrg();
		BB moveBits = new BB(1L<<posOfMovedPiece, game.getPlayerTurn());
		if(Character.toUpperCase(gameBB.getArraySquare(posOfMovedPiece)) == 'P' && 
				(moveBits.getBits() & rankMask[(game.getPlayerTurn() * 7)]) != 0L)
			promotePawn(posOfMovedPiece, game.getPlayerTurn());
	}

	public void promotePawn(int posOfMovedPiece, int colour) {	
		BB pawnBB = gameBB.getBB((colour == COLOR_WHITE) ? 'P' : 'p'); // pawn to promote
		char queen = (colour == COLOR_WHITE) ? 'Q' : 'q'; // promotion choice 0
		char knight = (colour == COLOR_WHITE) ? 'N' : 'n'; // promotion choice 1
		char rook = (colour == COLOR_WHITE) ? 'R' : 'r'; // promotion choice 2
		char bishop = (colour == COLOR_WHITE) ? 'B' : 'b'; // promotion choice 3
		String[] choices = {Game.getLongName(queen, colour), Game.getLongName(knight, colour), 
				Game.getLongName(rook, colour), Game.getLongName(bishop, colour)};
		
		final OptionsDiag optionsDiag = new OptionsDiag(
		"What do you wish the pawn to be promoted to?\n", choices, 1, BOARD_WIDTH/2, BOARD_HEIGHT/2);
		
		String value = (String)optionsDiag.getOptionPane().getValue();
		int choice = Arrays.asList(choices).indexOf(value);
		pawnBB.mulBits(~(1L<<posOfMovedPiece)); // set pawn bit to 0 at pos
		switch(choice) {
			case 0: // if choosing queen
				gameBB.getBB(queen).addBits(1L<<posOfMovedPiece); // set queen bit to 1 at pos
				break;
			case 1: // if choosing knight
				gameBB.getBB(knight).addBits(1L<<posOfMovedPiece); // set knight bit to 1 at pos
				break;
			case 2: // if choosing rook
				gameBB.getBB(rook).addBits(1L<<posOfMovedPiece); // set rook bit to 1 at pos
				break;
			case 3: // if choosing bishop
				gameBB.getBB(bishop).addBits(1L<<posOfMovedPiece); // set bishop bit to 1 at pos
				break;
			default:
				break;
		}
		game.getBoard().reloadGuiPieceAtPos(posOfMovedPiece);
	}
	
	public void updateBBStates() {
		// setting up bitboard masks
		OCCUPIED = gameBB.getOccupied(); // all pieces on board
		EMPTY = ~OCCUPIED; // all the empty squares
	}

	// get possible moves for current player's piece at position start
	public BB possibleMoves(int colour, int pos, Move history) {	
		long moveBits = 0L;
		char pieceType = gameBB.getArraySquare(pos);
		boolean isKing = Character.toUpperCase(pieceType) == 'K' ? true : false;
		// test if trying to move piece of wrong colour
		if((Character.isLowerCase(pieceType) && colour == 0) || 
				(Character.isUpperCase(pieceType) && colour == 1)) {
			game.getBoard().setDebugText("Cannot move opponent's piece!");
			return null;
		// test if square is empty
		} else if(pieceType == ' ')
			return null;
		
		switch(Character.toUpperCase(pieceType)) {
			case 'R': moveBits = getHoriVertMoves(pos, colour, gameBB); break;
			case 'B': moveBits = getDiagAntiDiagMoves(pos, colour, gameBB); break;
			case 'N': moveBits = getKnightMoves(pos, colour); break;
			case 'Q': moveBits = getQueenMoves(pos, colour, gameBB); break;
			case 'P': moveBits = getPawnMoves(history, pos, colour, false, gameBB); break;
			case 'K': moveBits = getKingMoves(colour, gameBB); break;
			default: moveBits = 0L;
		}
		// AND'ing bitboard of moves with mask of everything not containing white pieces,
		// resulting in the white pieces blocking moves
		moveBits &= ~gameBB.getColourPieces(colour);
		
		if(moveBits == 0L) // if moveBits is all 0's
			game.getBoard().setDebugText(Game.getLongName(pieceType) + " cannot move!");
			
		// routine for finding valid moves not causing check
		else	
			moveBits = validMovesNotCausingCheck(pieceType, moveBits, pos);

		return new BB(moveBits, colour);
	}
	
	private int verifyPosition(int start) {
		int pos = (start < 0) ? 0 : start;
		return (pos > 63) ? 63 : pos;
	}

	public static Move makeMove(long moveBits, int start) {
    	Move move = null;
	    for (int pos=Long.numberOfTrailingZeros(moveBits); pos<64-Long.numberOfLeadingZeros(moveBits); pos++)
	        if (((moveBits>>pos) & 1) == 1) {
	        	move = new Move(start, pos);
	        	break;
	        }
    	return move;
    }
	
    public static List<Move> makeMoveList(long moveBits, int start) {
    	List<Move> moves = new ArrayList<Move>();
	    for (int i=Long.numberOfTrailingZeros(moveBits); i<64-Long.numberOfLeadingZeros(moveBits); i++)
	        if (((moveBits>>i) & 1) == 1)
	        	moves.add(new Move(start, i));
    	return moves;
    }
    
	public static List<Move> bitsAsMoveList(BB moveBitsBB, int start) {	
		return makeMoveList(moveBitsBB.getBits(), start);
	} 
}
