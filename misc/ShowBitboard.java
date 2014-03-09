import java.util.Arrays;
import java.math.BigInteger;

public class ShowBitboard {

	public static long bitboard = 9032316223488L;

	public static void main(String[] args) {
		drawArray(bitboard);
	}

    public static void drawArray(long bitboard) {
    	char[][] newBoard = new char[8][8];
		for (int i=0; i<64; i++)
			newBoard[i/8][i%8] = (BigInteger.valueOf(bitboard).testBit(i)) ? '1' : ' ';
        System.out.println("      ______________________");
        for (int i=0; i<8; i++)
            System.out.println((8-i) + "|" + i*8 + ((i<2) ? "  " : " ") + Arrays.toString(newBoard[i]));
        System.out.println("      ______________________");
        System.out.println("      0  1  2  3  4  5  6  7");
        System.out.println("      a  b  c  d  e  f  g  h");
    }
}