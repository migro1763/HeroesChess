package game;

import gui.ChessBoardGui;
import gui.PieceGui;
import interfaces.Vals;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class MoveGenerator implements Vals {
	private static final long FILE_AB = 217020518514230019L; // 1's on all of column A + B (left side)
	private static final long FILE_GH = -4557430888798830400L; // 1's on all of column G + H (right side)
//	private static final long CENTRE=103481868288L; // 1's on the 4 most centered squares
//	private static final long EXTENDED_CENTRE=66229406269440L; // 1's on the 16 most centered squares
//	private static final long KING_SIDE=-1085102592571150096L; // 1's on all squares on right half of board
//	private static final long QUEEN_SIDE=1085102592571150095L; // 1's on all squares on left half of board
	private static final int CASTL_BR_L = 0, CASTL_BR_R = 7;
//	private static final int CASTL_WR_L = 56, CASTL_WR_R = 63;
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
	
	// rook
	public long getHoriVertMoves(int pos, int colour, BitBoard testBB) {
		long binPos = 1L<<verifyPosition(pos); // convert pos to binary
		long occupied = testBB.getOccupied();
		long possibleHoris =  ((occupied - 2 * binPos)^Long.reverse(Long.reverse(occupied)-2 * Long.reverse(binPos)));
		possibleHoris &= rankMask[(pos / 8)];
		long possibleVerts = ((occupied & fileMask[(pos % 8)]) - (2 * binPos))^
				Long.reverse(Long.reverse(occupied & fileMask[(pos % 8)]) - (2 * Long.reverse(binPos)));
		possibleVerts &= fileMask[(pos % 8)];	
		return (possibleHoris | possibleVerts);
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
	
	// bishop
	public long getDiagAntiDiagMoves(int pos, int colour, BitBoard testBB) {
		long binPos = 1L<<verifyPosition(pos); // convert pos to binary
		long occupied = testBB.getOccupied();
		long possibleDiags = ((occupied & diagMask[pos / 8 + pos % 8]) - (2 * binPos)) ^ 
				Long.reverse(Long.reverse(occupied & diagMask[pos / 8 + pos % 8]) - (2 * Long.reverse(binPos)));
		possibleDiags &= diagMask[pos / 8 + pos % 8];
		long possibleAntiDiags = ((occupied & aDiagMask[pos / 8 + 7 - pos % 8]) - (2 * binPos)) ^ 
				Long.reverse(Long.reverse(occupied & aDiagMask[pos / 8 + 7 - pos % 8]) - (2 * Long.reverse(binPos)));
		possibleAntiDiags &= aDiagMask[pos / 8 + 7 - pos % 8];
		
		return (possibleDiags | possibleAntiDiags);
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
			binPos = testBB.getBB((colour == 0) ? 'P' : 'p').getBits();
			pos = 0;
		} else
			binPos = 1L<<verifyPosition(pos); // convert pos to binary
		
		// handle history if not null
		if(history != null) {
//			long binHEnd = 1L<<history.getTrg(); // convert history end pos to binary
			
			// en passant
			long validEnPassantsMask = (colour == 0) ? 
					((history.getSrc() & rankMask[1])<<15) & history.getTrg()>>>1 | // w left of b
					((history.getSrc() & rankMask[1])<<17) & history.getTrg()<<1 : // w right of b
					((history.getSrc() & rankMask[6])>>>15) & history.getTrg()<<1 | // b left of w
					((history.getSrc() & rankMask[6])>>>17) & history.getTrg()>>>1; // b right of w
				
			andBits |= (colour == 0) ? // the square white would attack
						(((validEnPassantsMask & binPos)>>>7 & empty) | // test left side of b
						((validEnPassantsMask & binPos)>>>9 & empty)) & // test right side of b
							history.getSrc()<<8 : // the square black would attack
						(((validEnPassantsMask & binPos)<<7 & empty) | // test left side of w
						((validEnPassantsMask & binPos)<<9 & empty)) & // test right side of w
							history.getSrc()>>>8;
		}
		
		// attacks, if opponents present in attackable squares. 
		// ~fileMasks are for removing wraps over to opposite sides
		andBits = ( (colour == 0) ? (binPos>>>7 & ~fileMask[0]) | (binPos>>>9 & ~fileMask[7]) :
					(binPos<<7 & ~fileMask[7]) | (binPos<<9 & ~fileMask[0]) );

		if(!attackOnly) {
			// AND attack bits with opponent's pieces, 
			andBits &= testBB.getColourPieces(1-colour);
			// regular moves, only legal on empty squares
			andBits |= ((colour == 0) ? (binPos>>>8 | ((binPos & rankMask[6])>>>16) & binPos>>>16) :
										(binPos<<8 | ((binPos & rankMask[1])<<16) & binPos<<16)) & 
										empty;
		}
		
		return andBits;
	}
	
	// queen
	public long getQueenMoves(int colour, BitBoard testBB) {
		int queenPos = verifyPosition(BitBoard.getPos(testBB.getBB((colour == 0) ? 'Q' : 'q')));
		long moveBits = getHoriVertMoves(queenPos, colour, testBB);
		moveBits |= getDiagAntiDiagMoves(queenPos, colour, testBB); 
		return moveBits; 
	}
	
	// king
	public long getKingMoves(Move history, int colour, BitBoard testBB) {
		long binPos = 1L<<testBB.getKingPos(colour); // convert pos to binary
		// (clock-wise) n, n-e, e, s-e, s, s-w, w, n-w
		long andBits = ((binPos>>>8 | binPos>>>7 | binPos<<1 | binPos<<9) & ~fileMask[0]) |
				((binPos<<8 | binPos<<7 | binPos>>>1 | binPos>>>9) & ~fileMask[7]);
		
		// castling
		String cMoves = getCastling(colour);
		if(!cMoves.isEmpty())
			for(int i = 0; i < cMoves.length(); i+=4)
				andBits |= 1L<<ChessBoardGui.getPosFromCoords(BitBoard.getPosFromMove(cMoves.substring(i, i+4), 2),
						BitBoard.getPosFromMove(cMoves.substring(i, i+4), 3));
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
						long queen = getQueenMoves(colour, testBB) & inversePieces;
						return pawns|rooks|knights|bishops|queen;
			default:	return 0L;
		}
	}
	
	// returns the type of opponent piece that is checking current player's king
	public char pieceTypeChecking(int colour, BitBoard testBB) {
		long kingBinPos = 1L<<testBB.getKingPos(colour);
		if((getAttacks('p', 1-colour, testBB) & kingBinPos) != 0L) return 'p';
		else if((getAttacks('r', 1-colour, testBB) & kingBinPos) != 0L) return 'r';
		else if((getAttacks('n', 1-colour, testBB) & kingBinPos) != 0L) return 'n';
		else if((getAttacks('b', 1-colour, testBB) & kingBinPos) != 0L) return 'b';
		else if((getAttacks('q', 1-colour, testBB) & kingBinPos) != 0L) return 'q';
		else return 0;		
	}
	
	public boolean isInCheck(int colour, BitBoard testBB) {
		if(pieceTypeChecking(colour, testBB) != 0L) return true;
		else return false;
	}
	
	public boolean isInCheck(int colour) {
		return(isInCheck(colour, gameBB));
	}
	
	// get bitboard of safe moves for king, i.e. moves not resulting in a check
	public long getKingSafe(int colour, long kingMoves, BitBoard testBB) {
		// return (bitboard of all opponent's attacks AND king's moves) XOR king's moves
		return (getAttacks('a', 1-colour, testBB) & kingMoves) ^ kingMoves;
	}
	
	public long getKingSafe(int colour, long kingMoves) {
		return getKingSafe(colour, kingMoves, gameBB);
	}
	
	public String getCastling(int colour) {
        String list = "";
        long rookBB = gameBB.getBB((colour == 0) ? 'R' : 'r').getBits();
        int offset = (colour == 0) ? 56 : 0; // offset castling rooks by 56 if white
        
        if(!isInCheck(colour)) { 	         
	        // king side castling
        	if(game.kSideCastling[colour])
		        if (((1L<<(CASTL_BR_L + offset)) & rookBB) != 0)
		            if ((OCCUPIED & ((1L<<(5 + offset))|(1L<<(6 + offset)))) == 0) {
		                list += (colour == 0) ? "7476" : "0406"; // king and rook moves
		            }
	        
	        // queen side castling
        	if(game.qSideCastling[colour])
		        if (((1L<<(CASTL_BR_R + offset)) & rookBB) != 0)
		            if ((OCCUPIED & ((1L<<1)|(1L<<2)|(1L<<3))) == 0)
		                list += (colour == 0) ? "7472" : "0402"; // king and rook moves
        }
        return list;
    }
	
	public void pawnPromoAndCastling(Move selectedMove) {
		// pawn promotion?
		int posOfMovedPiece = selectedMove.getTrg();
		char typeToMove = gameBB.getArraySquare(posOfMovedPiece);
//		PieceGui movedGuiPiece = game.getBoard().getGuiPiece(posOfMovedPiece);
		BB moveBits = new BB(1L<<posOfMovedPiece, game.getPlayerTurn());
		if(Character.toUpperCase(typeToMove) == 'P' && 
				(moveBits.getBits() & MoveGenerator.rankMask[(game.getPlayerTurn() * 7)]) != 0L)
			promotePawn(posOfMovedPiece, game.getPlayerTurn());
		// if moved piece is a king, negate both castling possibilities
		if(Character.toUpperCase(typeToMove) == 'K') {
			game.kSideCastling[game.getPlayerTurn()] = game.qSideCastling[game.getPlayerTurn()] = false;
			int offset = (game.getPlayerTurn() == 0) ? 56 : 0;
			// if king move was a castling, also move the corresponding rook
			if(selectedMove.equals(new Move(4 + offset, 6 + offset)))
				gameBB.movePiece(new Move(7 + offset, 5 + offset));
			else if(selectedMove.equals(new Move(4 + offset, 2 + offset)))
				gameBB.movePiece(new Move(0 + offset, 3 + offset));
		}			
		// if moved piece is a rook, negate corresponding side castling possibility
		if(Character.toUpperCase(typeToMove) == 'R')
			if(posOfMovedPiece == 0 || posOfMovedPiece == 56) // if left side of board
				game.qSideCastling[game.getPlayerTurn()] = false;
			else if(posOfMovedPiece == 7 || posOfMovedPiece == 63) // if right side of board
				game.kSideCastling[game.getPlayerTurn()] = false;
	}

	public void promotePawn(int pos, int colour) {	
		BB pawnBB = gameBB.getBB((colour == COLOR_WHITE) ? 'P' : 'p'); // pawn to promote
		char knight = (colour == COLOR_WHITE) ? 'N' : 'n'; // promotion choice 0
		char queen = (colour == COLOR_WHITE) ? 'Q' : 'q'; // promotion choice 1
		String[] choices = {BitBoard.getLongName(knight), BitBoard.getLongName(queen)};
		final JOptionPane optionPane = new JOptionPane(
                "What do you wish the pawn to be promoted to?\n",
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.YES_NO_OPTION, null, choices, choices[1]);
				final JDialog dialog = new JDialog(new JFrame(), "Click a button", true);
				dialog.setContentPane(optionPane);
				dialog.setDefaultCloseOperation(
				    JDialog.DO_NOTHING_ON_CLOSE);
				dialog.addWindowListener(new WindowAdapter() {
				    public void windowClosing(WindowEvent we) {}
				});
		optionPane.addPropertyChangeListener(
		    new PropertyChangeListener() {
		        public void propertyChange(PropertyChangeEvent e) {
		            String prop = e.getPropertyName();
		
		            if (dialog.isVisible() 
		             && (e.getSource() == optionPane)
		             && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
		                dialog.setVisible(false);
		            }
		        }
		    });
		dialog.pack();
		dialog.setVisible(true);		
		int promotionType = (((String)optionPane.getValue()).equals(choices[0])) ? 0 : 1;
		
		switch(promotionType) {
			case 0: // if choosing knight
				pawnBB.mulBits(~(1L<<pos)); // set pawn bit to 0 at pos
				gameBB.getBB(knight).addBits(1L<<pos); // set knight bit to 1 at pos
				break;
			default: // if choosing queen
				pawnBB.mulBits(~(1L<<pos)); // set pawn bit to 0 at pos
				gameBB.getBB(queen).addBits(1L<<pos); // set queen bit to 1 at pos
				break;		
		}
		// recreate and reposition all gui pieces
		game.getBoard().setupGuiPieceArray();
	}
	
	public void updateBBStates() {
		// setting up bitboard masks
		OCCUPIED = gameBB.getOccupied(); // all pieces on board
		EMPTY = ~OCCUPIED; // all the empty squares
	}

	// get possible moves for current player's piece at position start
	public BB possibleMoves(int colour, int start, Move history) {	
		long moveBits = 0L;
		char pieceType = gameBB.getArraySquare(start);
		// test if trying to move piece of wrong colour
		if((Character.isLowerCase(pieceType)&&colour==0) || (Character.isUpperCase(pieceType)&&colour==1)) {
			game.getBoard().setDebugText("Cannot move opponent's piece!");
			return null;
		} else if(pieceType == ' ') {
			game.getBoard().setDebugText("That square is empty!");
			return null;
		}
		switch(Character.toUpperCase(pieceType)) {
			case 'R': moveBits = getHoriVertMoves(start, colour, gameBB); break;
			case 'B': moveBits = getDiagAntiDiagMoves(start, colour, gameBB); break;
			case 'N': moveBits = getKnightMoves(start, colour); break;
			case 'Q': moveBits = getQueenMoves(colour, gameBB); break;
			case 'P': moveBits = getPawnMoves(history, start, colour, false, gameBB); break;
			case 'K': moveBits = getKingMoves(history, colour, gameBB); break;
			default: moveBits = 0L;
		}
		// AND'ing bitboard of moves with mask of everything not containing white pieces,
		// resulting in the white pieces blocking moves
		moveBits &= ~gameBB.getColourPieces(colour);
		
		if(moveBits == 0L) { // if moveBits is all 0's
			game.getBoard().setDebugText(pieceType + " cannot move!");
			
		// routine for testing if possible moves can resolve king in check	
//		} else if(game.getActivePlayer().isCheck()) { // if king is in check
//			if(testCheck(colour, moveBits)) return null;
		}
		return new BB(moveBits, colour);
	}
	
	private int verifyPosition(int start) {
		int pos = (start < 0) ? 0 : start;
		pos = (pos > 63) ? 63 : pos;
		return pos;
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
	
	// tests if bitboard of moves and bits of opponent piece checking king, will be zero.
	// if zero, king is still in check and true is returned. otherwise return false (no longer in check)
	public boolean testCheck(int colour, long moveBits) {
		long culpritBits = 0L; // bitboard for opponent type checking's bits
		char oppTypeChecking = pieceTypeChecking(colour, gameBB);
		if(oppTypeChecking > 0) { // if it's 0, then pieceTypeChecking() didn't find the culprit type
			culpritBits |= gameBB.getBB(oppTypeChecking).getBits(); // set to opponent type checking's bits
			if((moveBits & culpritBits) == 0L) {
				game.getBoard().setDebugText("King still in check!");
				return true;					
			}
		}
		return false;
	}
}
