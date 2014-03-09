package game;

public class Moves {
	public static final long FILE_AB=217020518514230019L; // 1's on all of column A + B (left side)
	public static final long FILE_GH=-4557430888798830400L; // 1's on all of column G + H (right side)
	public static final long CENTRE=103481868288L; // 1's on the 4 most centered squares
	public static final long EXTENDED_CENTRE=66229406269440L; // 1's on the 16 most centered squares
	public static final long KING_SIDE=-1085102592571150096L; // 1's on all squares on right half of board
	public static final long QUEEN_SIDE=1085102592571150095L; // 1's on all squares on left half of board
	public static final long KING_B7=460039L;
	public static final long KNIGHT_C6=43234889994L;
	public static final int CASTL_WR_L = 56, CASTL_WR_R = 63, CASTL_BR_L = 0, CASTL_BR_R = 7;
	public static long OCCUPIED = 0L;
	public static long BLACK_PIECES = 0L;
	public static long WHITE_PAWNS_HAS_MOVED = 0L;
	public static long EMPTY = 0L;

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
		
	HeroesChessGame hcg;
	BitBoard gameBB;
	
	public Moves(HeroesChessGame hcg) {
		this.hcg = hcg;
		this.gameBB = hcg.getBitBoard();
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
		long binPos = 1L<<pos; // convert pos to binary
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
		int[] rookPos = testBB.getMultiPos(rooksBB);
		for (int i = 0; i < rookPos.length; i++)
			rookMoveBits |= getHoriVertMoves(rookPos[i], colour, testBB);
		return rookMoveBits;
	}
	
	// bishop
	public long getDiagAntiDiagMoves(int pos, int colour, BitBoard testBB) {
		long binPos = 1L<<pos; // convert pos to binary
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
		int[] bishopPos = testBB.getMultiPos(bishopsBB);
		for (int i = 0; i < bishopPos.length; i++)
			bishopMoveBits |= getDiagAntiDiagMoves(bishopPos[i], colour, testBB);
		return bishopMoveBits;
	}
	
	// knight
	public long getKnightMoves(int pos, int colour) {
		long binPos = 1L<<pos; // convert pos to binary
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
		int[] knightPos = testBB.getMultiPos(knightsBits);
		for (int i = 0; i < knightPos.length; i++)
			knightMoveBits |= getKnightMoves(knightPos[i], colour);
		return knightMoveBits;
	}
	
	// pawn
	public long getPawnMoves(String history, int pos, int colour, boolean attackOnly, BitBoard testBB) {
		long binPos;
		long andBits = 0L;
		long empty = testBB.getEmpty();
		if(pos < 0) { // if pos < 0 return moves of all pawns
			binPos = testBB.getBB((colour == 0) ? 'P' : 'p').getBits();
			pos = 0;
		} else
			binPos = 1L<<pos; // convert pos to binary
		
		// handle history if not ""
		if(!history.isEmpty()) {
			int[] hCoords = hcg.parseMove(history); // convert history string to int array
	//		System.out.println(hcg.makeStdMove(hCoords[0], hCoords[1], hCoords[2], hCoords[3]));
			int hStart = hCoords[0] * 8 + hCoords[1]; // convert history start row&column to 0-63 pos
			int hEnd = hCoords[2] * 8 + hCoords[3]; // convert history end row&column to 0-63 pos
			long binHStart = 1L<<hStart; // convert history start pos to binary
			long binHEnd = 1L<<hEnd; // convert history end pos to binary
			
			// en passant
			long validEnPassantsMask = (colour == 0) ? 
					((binHStart & rankMask[1])<<15) & binHEnd>>>1 | // w left of b
					((binHStart & rankMask[1])<<17) & binHEnd<<1 : // w right of b
					((binHStart & rankMask[6])>>>15) & binHEnd<<1 | // b left of w
					((binHStart & rankMask[6])>>>17) & binHEnd>>>1; // b right of w
				
			andBits |= (colour == 0) ? // the square white would attack
						(((validEnPassantsMask & binPos)>>>7 & empty) | // test left side of b
						((validEnPassantsMask & binPos)>>>9 & empty)) & // test right side of b
							binHStart<<8 : // the square black would attack
						(((validEnPassantsMask & binPos)<<7 & empty) | // test left side of w
						((validEnPassantsMask & binPos)<<9 & empty)) & // test right side of w
							binHStart>>>8;
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
		int queenPos = testBB.getPos(testBB.getBB((colour == 0) ? 'Q' : 'q'));
		long moveBits = getHoriVertMoves(queenPos, colour, testBB);
		moveBits |= getDiagAntiDiagMoves(queenPos, colour, testBB); 
		return moveBits; 
	}
	
	// king
	public long getKingMoves(String history, int colour, BitBoard testBB) {
		long binPos = 1L<<testBB.getKingPos(colour); // convert pos to binary
		// (clock-wise) n, n-e, e, s-e, s, s-w, w, n-w
		long andBits = ((binPos>>>8 | binPos>>>7 | binPos<<1 | binPos<<9) & ~fileMask[0]) |
				((binPos<<8 | binPos<<7 | binPos>>>1 | binPos>>>9) & ~fileMask[7]);
		
		// castling
		String cMoves = getCastling(colour);
		if(!cMoves.isEmpty())
			for(int i = 0; i < cMoves.length(); i+=4)
				andBits |= 1L<<gameBB.getPosFromCoords(gameBB.getPosFromMove(cMoves.substring(i, i+4), 2),
						gameBB.getPosFromMove(cMoves.substring(i, i+4), 3));
		return getKingSafe(colour, andBits);
	}
	
	// return bitboard of type attacks from player colour, type = 'a' returns all types
	public long getAttacks(char type, int colour, BitBoard testBB) {
		long inversePieces = ~testBB.getColourPieces(colour);
		switch(type) {
			case 'p':	return getPawnMoves("", -1, colour, true, testBB);
			case 'r':	return getHoriVertMoves(colour, testBB) & inversePieces;
			case 'n':	return getKnightMoves(colour, testBB) & inversePieces;
			case 'b':	return getDiagAntiDiagMoves(colour, testBB) & inversePieces;
			case 'q':	return getQueenMoves(colour, testBB) & inversePieces;
			case 'a':	long pawns = getPawnMoves("", -1, colour, true, testBB);
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
		if(pieceTypeChecking(colour, testBB) > 0) return true;
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
        	if(HeroesChessGame.kSideCastling[colour])
		        if (((1L<<(CASTL_BR_L + offset)) & rookBB) != 0)
		            if ((OCCUPIED & ((1L<<(5 + offset))|(1L<<(6 + offset)))) == 0) {
		                list += (colour == 0) ? "7476" : "0406"; // king and rook moves
		            }
	        
	        // queen side castling
        	if(HeroesChessGame.qSideCastling[colour])
		        if (((1L<<(CASTL_BR_R + offset)) & rookBB) != 0)
		            if ((OCCUPIED & ((1L<<1)|(1L<<2)|(1L<<3))) == 0)
		                list += (colour == 0) ? "7472" : "0402"; // king and rook moves
        }
        return list;
    }	

	public void promotePawn(int pos, int colour) {	
		BB pawnBB = gameBB.getBB((colour == 0) ? 'P' : 'p'); // pawn to promote
		char knight = (colour == 0) ? 'N' : 'n'; // promotion choice 0
		char queen = (colour == 0) ? 'Q' : 'q'; // promotion choice 1
		gameBB.drawArray(pawnBB.getBits(), "pawns before");
		String[] choices = {gameBB.getLongName(knight), gameBB.getLongName(queen)};
		int promotionType = Speak.ask("What do you wish the pawn to be promoted to?", choices);
		switch(promotionType) {
			case 0: // if choosing knight
				pawnBB.mulBits(~(1L<<pos)); // set pawn bit to 0 at pos
				gameBB.drawArray(pawnBB.getBits(), "pawns after");
				gameBB.drawArray(gameBB.getBB(knight).getBits(), "knights before");
				gameBB.getBB(knight).addBits(1L<<pos); // set knight bit to 1 at pos
				gameBB.drawArray(gameBB.getBB(knight).getBits(), "knights after");
				break;
			case 1: // if choosing queen
				pawnBB.mulBits(~(1L<<pos)); // set pawn bit to 0 at pos
				gameBB.getBB(queen).addBits(1L<<pos); // set queen bit to 1 at pos
				break;
		}
	}
	
	public void updateBBStates() {
		// setting up bitboard masks
		OCCUPIED = gameBB.getOccupied(); // all pieces on board
		EMPTY = ~OCCUPIED; // all the empty squares
	}

	// get possible moves for current player's piece at position start
	public String possibleMoves(int colour, int start, String history) {	
		long moveBits = 0L;	
		char pieceType = gameBB.getArraySquare(start);
		// test if trying to move piece of wrong colour
		if((Character.isLowerCase(pieceType)&&colour==0) || (Character.isUpperCase(pieceType)&&colour==1)) {
			Speak.say("\n!! => Cannot move opponent's piece!\n");
			return "";
		} else if(pieceType == ' ') {
			Speak.say("\n!! => That square is empty!\n");
			return "";
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
			Speak.say("\n!! => " + pieceType + " cannot move!", true);
			
		// routine for testing if possible moves can resolve king in check	
		} else if(HeroesChessGame.isChecked[colour]) { // if king is in check
			if(testCheck(colour, moveBits)) return "";
		}
		Speak.say("Moving: " + pieceType);		
		
		if(moveBits > 0L)
			gameBB.drawArray(moveBits);

	    return makeMove(moveBits, start);
	}
	
	// tests if bitboard of moves and bits of opponent piece checking king, will be zero.
	// if zero, king is still in check and true is returned. otherwise return false (no longer in check)
	public boolean testCheck(int colour, long moveBits) {
		long culpritBits = 0L; // bitboard for opponent type checking's bits
		char oppTypeChecking = pieceTypeChecking(colour, gameBB);
		if(oppTypeChecking > 0) { // if it's 0, then pieceTypeChecking() didn't find the culprit type
			culpritBits |= gameBB.getBB(oppTypeChecking).getBits(); // set to opponent type checking's bits
			if((moveBits & culpritBits) == 0L) {
				Speak.say("\n!! => king still in check!", true);
				return true;					
			}
		}
		return false;
	}
	
    public String makeMove(long moveBits, int start) {
    	String moves = "";
	    for (int i=Long.numberOfTrailingZeros(moveBits); i<64-Long.numberOfLeadingZeros(moveBits); i++)
	        if (((moveBits>>i) & 1) == 1)
	        	moves += ""+(start/8)+(start%8)+(i/8)+(i%8);
    	return moves;
    }
}
