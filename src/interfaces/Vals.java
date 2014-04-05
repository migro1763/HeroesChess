package interfaces;

import game.Move;
import java.awt.Color;

public interface Vals {
	public static final String[] COLOUR_NAME = {"WHITE", "black"};
	
	// player types
	public static final int PLAYER_OPTION_HUMAN = 0;
	public static final int PLAYER_OPTION_AI = 1;
	public static final int PLAYER_OPTION_NETWORK = 2;
	
	// game states
	public static final int COLOR_WHITE = 0;
	public static final int COLOR_BLACK = 1;
	public static final int BLACK_WON = 2;
	public static final int WHITE_WON = 3;
		
	public static final Color COLOUR_YELLOW = new Color(255, 255, 0, 64);
	public static final Color COLOUR_CYAN = new Color(64, 192, 255, 192);
	public static final Color COLOUR_GREEN = new Color(0, 255, 0, 160);
	
	public static final int ROW_1 = 0, ROW_2 = 1, ROW_3 = 2, ROW_4 = 3, 
							ROW_5 = 4, ROW_6 = 5, ROW_7 = 6, ROW_8 = 7;
	
	public static final int COLUMN_A = 0, COLUMN_B = 1, COLUMN_C = 2, COLUMN_D = 3,
							COLUMN_E = 4, COLUMN_F = 5, COLUMN_G = 6, COLUMN_H = 7;
	
	public static final char[] PIECE_NAME = {'P', 'N', 'B', 'R', 'Q', 'K'};
	public static final String[] LONG_PIECE_NAME = {"Pawn", "Knight", "Bishop", "Rook", "Queen", "King"};
	
	public static final String[] FILE_NAME = {"a", "b", "c", "d", "e", "f", "g", "h"};
	
	public static final Move[] K_CASTLING_MOVE = {new Move(60, 62), new Move(4, 6)};
	public static final Move[] Q_CASTLING_MOVE = {new Move(60, 58), new Move(4, 2)};
}
