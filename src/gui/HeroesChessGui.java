package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import players.Player;
import game.ChessGame;
import interfaces.Vals;
import game.HeroesChessGame;
import game.Move;
import game.MoveValidator;
import game.Piece;

/**
 * all x and y coordinates point to the upper left position of a component all
 * lists are treated as 0 being the bottom and size-1 being the top piece
 *
 */
//public class ChessGui extends JPanel implements IPlayerHandler, Declarations {
public class HeroesChessGui extends Player implements Vals {

	private static final long serialVersionUID = -8207574964820892354L;
	
	private static final int BOARD_START_X = 218;
	private static final int BOARD_START_Y = 123;

	private static final int SQUARE_WIDTH = 74; // full = 100
	private static final int SQUARE_HEIGHT = 74;

	private static final int PIECE_WIDTH = 66; // full = 88
	private static final int PIECE_HEIGHT = 66;
	
	private static final int GUI_PIECE_SCALE = 13; // 10 = 100 % scale

	private static final int PIECES_START_X = BOARD_START_X + (int)(SQUARE_WIDTH/2.0 - PIECE_WIDTH/2.0);
	private static final int PIECES_START_Y = BOARD_START_Y + (int)(SQUARE_HEIGHT/2.0 - PIECE_HEIGHT/2.0);

	private static final int DRAG_TARGET_SQUARE_START_X = BOARD_START_X - (int)(PIECE_WIDTH/2.0);
	private static final int DRAG_TARGET_SQUARE_START_Y = BOARD_START_Y - (int)(PIECE_HEIGHT/2.0);

	private Image imgBackground;
	private JLabel lblGameState;

	private HeroesChessGame hcg;
	private List<GuiPiece> guiPieces = new ArrayList<GuiPiece>();

	private GuiPiece dragPiece;

	private Move lastMove;
	private Move currentMove;
	private double getScaleAdd;

	private boolean draggingGamePiecesEnabled;
	
	// from Heroes class
	BufferedImage sprite;
    SpriteSheet ss;
	Image dbImage;
    Graphics dbg;
    Reader reader = new Reader();
    ArrayList<Unit> data;
    double scale = 1.0;
    int speed = 200; // smaller = faster
    int whichUnit = 0;
    int whichState = 0;

    // constructor - creating the user interface
	public HeroesChessGui(HeroesChessGame hcg) throws IOException {
		// create chess game
		this.hcg = hcg;
		
		this.setLayout(null);

		// background
		this.imgBackground = new ImageIcon(ImageIO.read(new File("/HeroesChess/img/battleBG1024x730_topL218x123_sq74.png"))).getImage();
		
		getScaleAdd = ((double)GUI_PIECE_SCALE / 12.0) - 1.0;

		init();
		
		//wrap game pieces into their graphical representation
		for (Piece piece : hcg.getPieces()) {
			createAndAddGuiPiece(piece);
		}
			
		// label to display game state
		String labelText = this.getGameStateAsText();
		this.lblGameState = new JLabel(labelText);
		lblGameState.setBounds(0, 30, 80, 30);
		lblGameState.setForeground(Color.WHITE);
		this.add(lblGameState);

		// create application frame and set visible
		//
		HeroesFrame f = new HeroesFrame();
		f.setSize(80, 80);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.add(this);
		f.setSize(imgBackground.getWidth(null) + 0, imgBackground.getHeight(null) + 0);
		
		// add listeners to enable drag and drop
		PiecesDragAndDropListener listener = new PiecesDragAndDropListener(this.guiPieces, this, f);
		this.addMouseListener(listener);
		this.addMouseMotionListener(listener);
	}

	private String getGameStateAsText() {
		String state = "unknown";
		switch (this.chessGame.getGameState()) {
			case ChessGame.GAME_STATE_BLACK: state = "black";break;
			case ChessGame.GAME_STATE_END_WHITE_WON: state = "white won";break;
			case ChessGame.GAME_STATE_END_BLACK_WON: state = "black won";break;
			case ChessGame.GAME_STATE_WHITE: state = "white";break;
		}
		if(this.isCheck())
			state = "in check!";
		
		return state;
	}

	// create a game piece
	private void createAndAddGuiPiece(Piece piece) throws IOException {
//		Image img = this.getImageForPiece(piece.getColor(), piece.getType(), piece.getNbr());		
		GuiPiece guiPiece = new GuiPiece(getAnimStates(piece), piece);
		// start animation on the guiPiece
		guiPiece.getAnim(guiPiece.getState()).setSpeed(200);
		guiPiece.getAnim(guiPiece.getState()).play();
		// add guiPiece to list
		this.guiPieces.add(guiPiece);
	}
	
    private void init() throws IOException {
        BufferedImageLoader loader = new BufferedImageLoader();
        BufferedImage spriteSheet = null;

        try {
			// load all units as array list of Unit objects
			data = reader.readData(IMG_PATH + "heroes/heroesFrames_sheet.txt");
            spriteSheet = loader.loadImage("file:" + IMG_PATH + "heroes/heroesFrames_sheet.png");
        } catch (IOException exc) {}

        ss = new SpriteSheet(spriteSheet);
        
    }
    
    public ArrayList<Animator> getAnimStates(Piece piece) {
		// create 4 Animator objects, one per state, feed into arraylist
        ArrayList<Animator> animSprites = new ArrayList<Animator>();
		for (int i = 0; i < 4; i++) {
			Animator tmpAnim = new Animator(getSprites(ss, piece.getUnitNbr(), i));
			tmpAnim.setUnit(findUnit(piece.getUnitNbr(), GuiPiece.STATE_IDLE));
			animSprites.add(tmpAnim);	
		}
		
		return animSprites;
	}

    public ArrayList<BufferedImage> getSprites(SpriteSheet ss, int unitNbr, int stateNbr) {
		ArrayList<BufferedImage> sprites = new ArrayList<BufferedImage>();
		Unit currentUnit = findUnit(unitNbr, stateNbr);

		if(currentUnit != null) {
			for (int i = 0; i < currentUnit.frames; i++) {
				sprites.add(ss.grabSprite(currentUnit.getFrm(i).x, currentUnit.getFrm(i).y,
											currentUnit.getFrm(i).w, currentUnit.getFrm(i).h));			
			}
		}
        return sprites;
	}
    
    public Unit findUnit(int unitNbr, int stateNbr) {
		for (Unit unit : data) {
			if(unit.toString().equals(UNITS[unitNbr] + "_" + STATES[stateNbr])) {
				return unit;
			}
		}
		return null;
    }

	@Override
	protected void paintComponent(Graphics g) {
		// draw background
		g.drawImage(this.imgBackground, 0, 0, null);

		// draw pieces
		int w = -1, b = -1;
		for (GuiPiece guiPiece : this.guiPieces) {
			if( !guiPiece.isCaptured()){
//				g.drawImage(guiPiece.getImage(), guiPiece.getX(), guiPiece.getY(), null);
				if(guiPiece.getAnim(guiPiece.getState()) != null) {
					guiPiece.getAnim(guiPiece.getState()).update(System.currentTimeMillis());
					g.drawImage(guiPiece.getImage(guiPiece.getState()), guiPiece.getX(), guiPiece.getY() - GUI_PIECE_SCALE,
								guiPiece.getAnim(guiPiece.getState()).unit.getModFrmDim(SQUARE_HEIGHT, getScaleAdd).w,
								guiPiece.getAnim(guiPiece.getState()).unit.getModFrmDim(SQUARE_HEIGHT, getScaleAdd).h, null);
				}
			} else {
				// draw dead pieces on each side of board, white on the left
				g.drawImage(guiPiece.getAnim(STATE_DEATH).getSpriteAtFrame(guiPiece.getAnim(STATE_DEATH).getSizeOfFrames()-1), 
						(guiPiece.getColor() == Piece.COLOR_WHITE) ? 30 : 900, 
						100 + ( ((guiPiece.getColor() == Piece.COLOR_WHITE) ? ++w : ++b) * 
								(int)(guiPiece.getAnim(STATE_DEATH).unit.getModFrmDim(SQUARE_HEIGHT, getScaleAdd).h * 0.8)) - GUI_PIECE_SCALE,
						guiPiece.getAnim(STATE_DEATH).unit.getModFrmDim(SQUARE_HEIGHT, getScaleAdd).w,
						guiPiece.getAnim(STATE_DEATH).unit.getModFrmDim(SQUARE_HEIGHT, getScaleAdd).h, null);	
			}
		}

		// draw last move, if user is not dragging game piece
		if( !isUserDraggingPiece() && this.lastMove != null ){
			int highlightSourceX = convertColumnToX(this.lastMove.sourceColumn);
			int highlightSourceY = convertRowToY(this.lastMove.sourceRow);
			int highlightTargetX = convertColumnToX(this.lastMove.targetColumn);
			int highlightTargetY = convertRowToY(this.lastMove.targetRow);

			g.setColor(Color.YELLOW);
			g.drawRoundRect( highlightSourceX, highlightSourceY, SQUARE_WIDTH-12, SQUARE_HEIGHT-12,10,10);
			g.drawRoundRect( highlightTargetX, highlightTargetY, SQUARE_WIDTH-12, SQUARE_HEIGHT-12,10,10);
		}

		// draw valid target locations, if user is dragging a game piece
		if( isUserDraggingPiece() ) {

			MoveValidator moveValidator = this.chessGame.getMoveValidator();

			// iterate the complete board to check if target locations are valid
			for (int column = Piece.COLUMN_A; column <= Piece.COLUMN_H; column++) {
				for (int row = Piece.ROW_1; row <= Piece.ROW_8; row++) {
					int sourceRow = this.dragPiece.getPiece().getRow();
					int sourceColumn = this.dragPiece.getPiece().getColumn();

					// check if target location is valid
					if( moveValidator.isMoveValid( new Move(sourceRow, sourceColumn, row, column), false) ) {
						int highlightX = convertColumnToX(column);
						int highlightY = convertRowToY(row);
						// draw the highlight
						g.setColor(Color.GREEN);
						g.drawRoundRect( highlightX, highlightY, SQUARE_WIDTH-12, SQUARE_HEIGHT-12,10,10);
					}
				}
			}
		}
		// draw game state label
		this.lblGameState.setText(this.getGameStateAsText());
		
		repaint();
	}

	// check if the user is currently dragging a game piece
	private boolean isUserDraggingPiece() {
		return this.dragPiece != null;
	}

	// return current game state
	public int getGameState() {
		return this.chessGame.getGameState();
	}

	// convert logical column into x coordinate
	public static int convertColumnToX(int column){
		return PIECES_START_X + SQUARE_WIDTH * column;
	}

	// convert logical row into y coordinate
	public static int convertRowToY(int row){
		return PIECES_START_Y + SQUARE_HEIGHT * (Piece.ROW_8 - row);
	}

	// convert x coordinate into logical column
	public static int convertXToColumn(int x){
		return (x - DRAG_TARGET_SQUARE_START_X)/SQUARE_WIDTH;
	}

	// convert y coordinate into logical row
	public static int convertYToRow(int y){
		return Piece.ROW_8 - (y - DRAG_TARGET_SQUARE_START_Y)/SQUARE_HEIGHT;
	}

	// change location of given piece, if valid location, else move back to original position.
	public void setNewPieceLocation(GuiPiece dragPiece, int x, int y) {
		int targetRow = ChessGui.convertYToRow(y);
		int targetColumn = ChessGui.convertXToColumn(x);

		Move move = new Move(dragPiece.getPiece().getRow(), dragPiece.getPiece().getColumn()
							, targetRow, targetColumn);
		if( this.chessGame.getMoveValidator().isMoveValid(move, true) ) {
			this.currentMove = move;
		} else {
			dragPiece.resetToUnderlyingPiecePosition();
		}
	}

	// set the game piece that is currently dragged by the user
	public void setDragPiece(GuiPiece guiPiece) {
		this.dragPiece = guiPiece;
	}

	// @return the gui piece that the user is currently dragging
	public GuiPiece getDragPiece(){
		return this.dragPiece;
	}

	@Override
	public Move getMove() {
		this.draggingGamePiecesEnabled = true;
		Move moveForExecution = this.currentMove;
		this.currentMove = null;
		return moveForExecution;
	}

	@Override
	public void moveSuccessfullyExecuted(Move move) {
		// adjust GUI piece
		GuiPiece guiPiece = this.getGuiPieceAt(move.targetRow, move.targetColumn);
		if( guiPiece == null) {
			throw new IllegalStateException("no guiPiece at "+move.targetRow+"/"+move.targetColumn);
		}
//		guiPiece.update(System.currentTimeMillis());
		guiPiece.resetToUnderlyingPiecePosition();

		// remember last move
		this.lastMove = move;

		// disable dragging until asked by ChessGame for the next move
		this.draggingGamePiecesEnabled = false;

		// repaint the new state
		this.repaint();
	}

	// return true - if the user is allowed to drag game pieces
	public boolean isDraggingGamePiecesEnabled(){
		return draggingGamePiecesEnabled;
	}

	// get non-captured the gui piece at the specified position
	private GuiPiece getGuiPieceAt(int row, int column) {
		for (GuiPiece guiPiece : this.guiPieces) {
			if( guiPiece.getPiece().getRow() == row
					&& guiPiece.getPiece().getColumn() == column
					&& guiPiece.isCaptured() == false){
				return guiPiece;
			}
		}
		return null;
	}
}
