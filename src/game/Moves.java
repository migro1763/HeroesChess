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
		// (clock-wise) n-e, e-n, e-s, s-e, s-w, w-s, w-n, n-w
		return ((binPos>>>15 | binPos>>>6 | binPos<<10 | binPos<<17) & ~FILE_AB) |
				((binPos<<15 | binPos<<6 | binPos>>>10 | binPos>>>17) & ~FILE_GH);
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
		return (getHoriVertMoves(queenPos, colour, testBB) | getDiagAntiDiagMoves(queenPos, colour, testBB)); 
	}
	
	// king
	public long getKingMoves(String history, int colour, BitBoard testBB) {
		long binPos = 1L<<testBB.getKingPos(colour); // convert pos to binary
		// (clock-wise) n, n-e, e, s-e, s, s-w, w, n-w
		long andBits = ((binPos>>>8 | binPos>>>7 | binPos<<1 | binPos<<9) & ~fileMask[0]) |
				((binPos<<8 | binPos<<7 | binPos>>>1 | binPos>>>9) & ~fileMask[7]);	
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
						gameBB.drawArray(pawns, "pawns:");
						long rooks = getHoriVertMoves(colour, testBB) & inversePieces;			
						gameBB.drawArray(rooks, "rooks:");
						long knights = getKnightMoves(colour, testBB) & inversePieces;
						gameBB.drawArray(knights, "knights:");
						long bishops = getDiagAntiDiagMoves(colour, testBB) & inversePieces;
						gameBB.drawArray(bishops, "bishops:");
						long queen = getQueenMoves(colour, testBB) & inversePieces;
						gameBB.drawArray(queen, "queen:");
						return pawns|rooks|knights|bishops|queen;
			default:	return 0L;
		}
	}
	
	public boolean isInCheck(int colour) {
		long bitboard = (getAttacks('a', 1-colour, gameBB) & 1L<<gameBB.getKingPos(colour));
		gameBB.drawArray(getAttacks('a', 1-colour, gameBB));
		gameBB.drawArray(bitboard);
		return ((bitboard) != 0);
	}
	
	public boolean isInCheck(int colour, BitBoard testBB) {
		return ((getAttacks('a', 1-colour, gameBB) & 1L<<testBB.getKingPos(colour)) != 0);
	}
	
	// get bitboard of safe moves for king, i.e. moves not resulting in a check
	public long getKingSafe(int colour, long kingMoves) {
		// return (bitboard of all opponent's attacks AND king's moves) XOR king's moves
		return (getAttacks('a', 1-colour, gameBB) & kingMoves) ^ kingMoves;
	}
	
	public String getCastling(long rookBB, int colour, boolean kingHasMoved) {
        String list = "";
        int offset = (colour == 0) ? 56 : 0; // offset castling rooks by 56 if white
        
        if(!isInCheck(colour)) {
	        // TODO: test if king has moved before
	        // TODO: test if either rook has moved before  
	        // TODO: return bitboard of moves instead of string list
	         
	        // king side castling
	        if (((1L<<(CASTL_BR_L + offset)) & rookBB) != 0)
	            if ((OCCUPIED & ((1L<<(5 + offset))|(1L<<(6 + offset)))) == 0)
	                list += (colour == 0) ? "7476" : "0406";
	        
	        // queen side castling
	        if (((1L<<(CASTL_BR_R + offset)) & rookBB) != 0)
	            if ((OCCUPIED & ((1L<<1)|(1L<<2)|(1L<<3))) == 0)
	                list += (colour == 0) ? "7472" : "0402";
        }
        return list;
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
			System.out.println("\n!! => Cannot move opponent's piece!");
			return "";
		} else if(pieceType == ' ') {
			System.out.println("\n!! => That square is empty!");
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
		if((moveBits & ~gameBB.getColourPieces(colour)) == 0L) { // if moveBits is all 0's
			System.out.println("\n!! => " + pieceType + " cannot move!");
			
		// routine for testing if possible moves can resolve king in check	
		} else if(HeroesChessGame.isChecked) { // if king is in check
			BitBoard testBB = new BitBoard(gameBB); // make a clone of gameBB for testing
			long testMoveBits = moveBits &= ~testBB.getColourPieces(colour);
			String testMoves = makeMove(testMoveBits, start);
			for(int i = 0; i < testMoves.length(); i += 4) {
				System.out.println("\nTest => move: " + hcg.makeStdMove(hcg.parseMove(testMoves.substring(i, i+4))));
				testBB.movePiece(testMoves.substring(i, i+4));
				HeroesChessGame.isChecked = isInCheck(colour, testBB);
				if(HeroesChessGame.isChecked) { // doesn't moveBits uncheck king?
					System.out.println("\n!! => King is still in check!");
					testBB = null;
					return "";
				} else
					break; // no longer in check by one of the possible moves
			}
			testBB = null;
			
		} else
			System.out.println("Moving: " + pieceType);		

		// AND'ing bitboard of moves with mask of everything not containing white pieces,
		// resulting in the white pieces blocking moves
		moveBits &= ~gameBB.getColourPieces(colour);
		
		if(moveBits > 0L)
			gameBB.drawArray(moveBits);

	    return makeMove(moveBits, start);
	}
	
    public String makeMove(long moveBits, int start) {
    	String moves = "";
	    for (int i=Long.numberOfTrailingZeros(moveBits); i<64-Long.numberOfLeadingZeros(moveBits); i++)
	        if (((moveBits>>i) & 1) == 1)
	        	moves += ""+(start/8)+(start%8)+(i/8)+(i%8);
    	return moves;
    }
}
