package game;

import java.math.BigInteger;
import java.util.Arrays;

public class BitBoard {
	
	// variable declarations
	private char[][] chessBoard = {
        {'r','n','b','q','k','b','n','r'}, // ' 0',' 1',' 2',' 3',' 4',' 5',' 6',' 7'
        {'p','p','p','p','p','p','p','p'}, // ' 8',' 9','10','11','12','13','14','15'
        {' ',' ',' ',' ',' ',' ',' ',' '}, // '16','17','18','19','20','21','22','23'
        {' ',' ',' ',' ',' ',' ',' ',' '}, // '24','25','26','27','28','29','30','31'
        {' ',' ',' ',' ',' ',' ',' ',' '}, // '32','33','34','35','36','37','38','39'
        {' ',' ',' ',' ',' ',' ',' ',' '}, // '40','41','42','43','44','45','46','47'
        {'P','P','P','P','P','P','P','P'}, // '48','49','50','51','52','53','54','55'
        {'R','N','B','Q','K','B','N','R'}  // '56','57','58','59','60','61','62','63'	
	};
	
	private BB wp = new BB(0L, 0);
	private BB wr = new BB(0L, 0);
	private BB wn = new BB(0L, 0);
	private BB wb = new BB(0L, 0);
	private BB wq = new BB(0L, 0);
	private BB wk = new BB(0L, 0);
	private BB bp = new BB(0L, 1);
	private BB br = new BB(0L, 1);
	private BB bn = new BB(0L, 1);
	private BB bb = new BB(0L, 1);
	private BB bq = new BB(0L, 1);
	private BB bk = new BB(0L, 1);	
	
	private Game game;
	// end variable declarations
	
	public BitBoard(Game game) {
		this.game = game;
		initializeBoard();
	}
	
	// cloning constructor
	public BitBoard(BitBoard clone) {
		this.game = clone.game;
		this.wp = clone.wp;
		this.wr = clone.wr;
		this.wn = clone.wn;
		this.wb = clone.wb;
		this.wq = clone.wq;
		this.wk = clone.wk;
		this.bp = clone.bp;
		this.br = clone.br;
		this.bn = clone.bn;
		this.bb = clone.bb;
		this.bq = clone.bq;
		this.bk = clone.bk;
		this.chessBoard = clone.chessBoard;
	}
	
	// defines bitboards for all piece types and for both colours
    public void initializeBoard() {
        for (int i=0; i<64; i++) {
            switch (chessBoard[i/8][i%8]) {
                case 'P': wp.addBits(1L << i);
                    break;
                case 'N': wn.addBits(1L << i);
                    break;
                case 'B': wb.addBits(1L << i);
                    break;
                case 'R': wr.addBits(1L << i);
                    break;
                case 'Q': wq.addBits(1L << i);
                    break;
                case 'K': wk.addBits(1L << i);
                    break;
                case 'p': bp.addBits(1L << i);
                    break;
                case 'n': bn.addBits(1L << i);
                    break;
                case 'b': bb.addBits(1L << i);
                    break;
                case 'r': br.addBits(1L << i);
                    break;
                case 'q': bq.addBits(1L << i);
                    break;
                case 'k': bk.addBits(1L << i);
                    break;
            }
        }
    }
    
    public long getOccupied() {
    	return (wp.getBits()|wn.getBits()|wb.getBits()|wr.getBits()|wq.getBits()|wk.getBits()|
    			bp.getBits()|bn.getBits()|bb.getBits()|br.getBits()|bq.getBits()|bk.getBits());
    }
    
    public long getColourPieces(int colour) {
    	return (colour == 0) ? wp.getBits()|wn.getBits()|wb.getBits()|wr.getBits()|wq.getBits()|wk.getBits() : 
    							bp.getBits()|bn.getBits()|bb.getBits()|br.getBits()|bq.getBits()|bk.getBits();
    }   
	
	public long getNotColour(int colour) {
		return (colour == 0) ? ~(wp.getBits()|wn.getBits()|wb.getBits()|wr.getBits()|wq.getBits()|bk.getBits()) :
								~(bp.getBits()|bn.getBits()|bb.getBits()|br.getBits()|bq.getBits()|wk.getBits()); 
	}
	
	public long getEmpty() {
		return ~getOccupied(); 
	}
    
    public void drawArray(long bitboard) {
    	char[][] newBoard = new char[8][8];
    	if(bitboard == -1L)
    		newBoard = getArray();
    	else
	        for (int i=0; i<64; i++)
	        	newBoard[i/8][i%8] = (BigInteger.valueOf(bitboard).testBit(i)) ? '1' : ' ';
        Speak.say("      ______________________", true);
        for (int i=0; i<8; i++)   	
            Speak.say((8-i) + "|" + i*8 + ((i<2) ? "  " : " ") + Arrays.toString(newBoard[i]), true);
        Speak.say("      ииииииииииииииииииииии", true);
        Speak.say("      0  1  2  3  4  5  6  7", true);
        Speak.say("      a  b  c  d  e  f  g  h", true);
    }
    
    public void drawArray(long bitboard, String label) {
    	Speak.say(label);
    	drawArray(bitboard);
    }
    
    public void drawArray() { 	
    	drawArray(-1L); // call drawArray(bitboard) with all bits filled (= -1)
    }
    
    public char[][] getArray() {
        char newBoard[][] = new char[8][8];     
        for (int i=0; i<64; i++)
        	newBoard[i/8][i%8] = getArraySquare(i);
    	return newBoard;
    }
    
    public char getArraySquare(int position) {
    	if(position>=0 && position<64) {
	    	if (BigInteger.valueOf(wp.getBits()).testBit(position)) return 'P';
	    	if (BigInteger.valueOf(wn.getBits()).testBit(position)) return 'N';
	    	if (BigInteger.valueOf(wb.getBits()).testBit(position)) return 'B';
	    	if (BigInteger.valueOf(wr.getBits()).testBit(position)) return 'R';
	    	if (BigInteger.valueOf(wq.getBits()).testBit(position)) return 'Q';
	    	if (BigInteger.valueOf(wk.getBits()).testBit(position)) return 'K';
	    	if (BigInteger.valueOf(bp.getBits()).testBit(position)) return 'p';
	    	if (BigInteger.valueOf(bn.getBits()).testBit(position)) return 'n';
	    	if (BigInteger.valueOf(bb.getBits()).testBit(position)) return 'b';
	    	if (BigInteger.valueOf(br.getBits()).testBit(position)) return 'r';
	    	if (BigInteger.valueOf(bq.getBits()).testBit(position)) return 'q';
	    	if (BigInteger.valueOf(bk.getBits()).testBit(position)) return 'k';
    	}
    	return ' ';
    }
    
    // *** OBSOLETE METHODS! ***
    // coord 0 = x1, 1 = y1, 2 = x2, 3 = y2
    public int getPosFromMove(String move, int coord) {
    	switch(coord) {
	    	case 0: return Integer.parseInt(move.substring(0,1));
	    	case 1: return Integer.parseInt(move.substring(1,2));
	    	case 2: return Integer.parseInt(move.substring(2,3));
	    	case 3: return Integer.parseInt(move.substring(3,4));
	    	default: return 0;
    	}
    }
    
    public static int getPosFromCoords(int x, int y) {
    	return y + (x * 8);
    }
    // *** OBSOLETE METHODS END ***
    
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
 		
	public void movePiece(Move move) {
		BB bitboard;
        char type = getArraySquare(move.getSrc());
        char oppType = getArraySquare(move.getTrg());
        if(oppType != ' ') {
        	setBB(oppType, move.getTrg(), 0); // setting piece on target square's bitboard to 0 at target pos
        	// PS: setBB has a debug-print of bitboard of remaining opponents type
        } else
        	Speak.say("", true);
        switch (type) {
	        case 'P':	bitboard = wp;
	            break;
	        case 'N': 	bitboard = wn;
	            break;
	        case 'B': 	bitboard = wb;
	            break;
	        case 'R': 	bitboard = wr;
	            break;
	        case 'Q': 	bitboard = wq;
	            break;
	        case 'K': 	bitboard = wk;
	            break;
	        case 'p': 	bitboard = bp;
	            break;
	        case 'n': 	bitboard = bn;
	            break;
	        case 'b': 	bitboard = bb;
	            break;
	        case 'r': 	bitboard = br;
	            break;
	        case 'q': 	bitboard = bq;
	            break;
	        case 'k': 	bitboard = bk;
	            break;
	        default:	bitboard = new BB();
        }
        bitboard.mulBits((bitboard.getBits() & ~(1L << move.getSrc()))); // set source to false (0)
    	bitboard.addBits((bitboard.getBits() | (1L << move.getTrg()))); // set target to true (1)
	}
	
	public int getKingPos(int colour) {
		return getPos((colour == 0) ? wk : bk);
	}
	
	public static int getPos(BB bb) {
		int pos;
		for (pos = 0; pos < 64; pos++)
			if(BigInteger.valueOf(bb.getBits()).testBit(pos))
				break;
		return pos;
	}
	
	public static int getPos(long bitboard) {
		return getPos(new BB(bitboard, 0));
	}
	
	public static int[] getMultiPos(long bitboard) {
		int howManyBits = Long.bitCount(bitboard);
		int[] positions = new int[howManyBits];
		int pos = 0;
		for (int i = 0; i < 64; i++)
			if(BigInteger.valueOf(bitboard).testBit(i)) {
				positions[pos] = i;
				if(++pos >= howManyBits) 
					break; // exit for loop when all 1's are found
			}
		return positions;
	}
	
	public BB getBB(char type) {
		switch(type) {
		// whites
		case 'P':	return wp;
		case 'R':	return wr;
		case 'N':	return wn;
		case 'B':	return wb;
		case 'Q':	return wq;
		case 'K':	return wk;
		// blacks
		case 'p':	return bp;
		case 'r':	return br;
		case 'n':	return bn;
		case 'b':	return bb;
		case 'q':	return bq;
		case 'k':	return bk;
		default:	return new BB();
		}
	}
	
	public void setBB(char type, int pos, int bit) {
		if(bit == 0)
			getBB(type).mulBits(getBB(type).getBits() & ~(1L << pos)); // set pos to false (0)
		else
			getBB(type).addBits(getBB(type).getBits() | 1L << pos); // set pos to true (1)
	}
	
	public void setBB(char type, long bits, int bit) {
		if(bit == 0)
			getBB(type).mulBits(bits);
		else
			getBB(type).addBits(bits);
	}

}
