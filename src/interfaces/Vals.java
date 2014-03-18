package interfaces;

public interface Vals {
	public static final String[] COLOUR_NAME = {"WHITE", "black"};
	
	public static final int GAME_STATE_WHITE = 0;
	public static final int GAME_STATE_BLACK = 1;
	public static final int GAME_STATE_END_BLACK_WON = 2;
	public static final int GAME_STATE_END_WHITE_WON = 3;
	public static final int COLOR_WHITE = 0, COLOR_BLACK = 1;
	
	public static final int ROW_1 = 0, ROW_2 = 1, ROW_3 = 2, ROW_4 = 3, 
							ROW_5 = 4, ROW_6 = 5, ROW_7 = 6, ROW_8 = 7;
	
	public static final int COLUMN_A = 0, COLUMN_B = 1, COLUMN_C = 2, COLUMN_D = 3,
							COLUMN_E = 4, COLUMN_F = 5, COLUMN_G = 6, COLUMN_H = 7;
	
	public static final String[] PIECE_NAME = {"P", "N", "B", "R", "Q", "K"};
	public static final String[] LONG_PIECE_NAME = {"Pawn", "Knight", "Bishop", "Rook", "Queen", "King"};
	
	public static final String[] FILE_NAME = {"a", "b", "c", "d", "e", "f", "g", "h"};
}
