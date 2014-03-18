package interfaces;

public interface Declarations {
	// white units					king		knight		bishop	queen		pawn		rook
	public static final String UNITS[] = {"archangel", "cavalry", "druid", "nagaqueen", "peasant", "titan",
	// black units					king		knight			bishop		queen		pawn		rook		
									"archdevil", "blackknight", "powerlich", "medusa", "skeleton", "blackdragon"};
	// animation states
	public static final String STATES[] = {"idle", "walk", "attack", "death"};	
	public static final int STATE_IDLE = 0, STATE_WALK = 1, STATE_ATTACK = 2, STATE_DEATH = 3;
	
	public static final String IMG_PATH = "C:/Users/Mikkel/Java/_projects/sjakk/img/";
	public static final String IMG_PATH_GITHUB = "C:/Users/Mikkel/Documents/GitHub/HeroesChess/img/";
	
	// --- GUI dimensions ---
	
	public static final int BOARD_START_X = 218;
	public static final int BOARD_START_Y = 123;

	public static final int SQUARE_WIDTH = 74; // full = 100
	public static final int SQUARE_HEIGHT = 74;
	
	public static final int BOARD_WIDTH = SQUARE_WIDTH * 8;
	public static final int BOARD_HEIGHT = SQUARE_HEIGHT * 8;

	public static final int PIECE_WIDTH = 66; // full = 88
	public static final int PIECE_HEIGHT = 66;
	
	public static final int GUI_PIECE_SCALE= 13; // 10 = 100% scale

	public static final int PIECES_START_X = BOARD_START_X + (int)(SQUARE_WIDTH/2.0 - PIECE_WIDTH/2.0);
	public static final int PIECES_START_Y = BOARD_START_Y + (int)(SQUARE_HEIGHT/2.0 - PIECE_HEIGHT/2.0);

	public static final int DRAG_TARGET_SQUARE_START_X = BOARD_START_X - (int)(PIECE_WIDTH/2.0);
	public static final int DRAG_TARGET_SQUARE_START_Y = BOARD_START_Y - (int)(PIECE_HEIGHT/2.0);

}