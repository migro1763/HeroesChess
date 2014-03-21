package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
//import java.util.List;



import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import pieces.Piece;
import game.BitBoard;
import game.ChessGame;
import game.Game;
import game.Move;
import game.Speak;
import interfaces.Declarations;
import interfaces.Vals;

public class ChessBoardGui extends JPanel implements Declarations, Vals {
	
	private static final long serialVersionUID = 1L;
	private Image imgBackground;
	private JLabel lblGameState, debugTextLabel;
	private String debugText = "";

	private Game game;
	private HashMap<String, PieceGui> pieceGuisBuffer = new HashMap<String, PieceGui>();
	private PieceGui[] guiPieces = new PieceGui[64];
	private List<PieceGui> capturedGuiPieces = new ArrayList<PieceGui>();
	private PieceGui dragPiece;
	private int piecesSize = 0;

	private Move lastMove;
	private double getScaleAdd;
	
	// from Heroes class
//	private BufferedImage sprite;
	private SpriteSheet ss;
//	private Image dbImage;
//	private Graphics dbg;
	private Reader reader = new Reader();
	private ArrayList<Unit> data;
    public double scale = 1.0;
    public int speed = 200; // smaller = faster
    public int whichUnit = 0;
    public int whichState = 0;
    
    public ChessBoardGui(Game game) {
		
    	this.setLayout(null);
		
    	// load background image
		try {
			this.imgBackground = new ImageIcon(ImageIO.read(
					new File("./img/battleBG1024x730_topL218x123_sq74.png"))).getImage();
		} catch (IOException exc) {
			System.out.println("image background path illegal!");
		}
		
		// set game object as the created chess game parameter
		this.game = game;
		
		// factor to scale gui pieces to fit board graphics
		getScaleAdd = ((double)GUI_PIECE_SCALE / 12.0) - 1.0;

		// load animation graphics and text file of all sprites
		initAnimationData();
		
		// load piece gui graphics into hashmap buffer
		loadPieceGuisIntoBuffer();
		
		// setup 64-size array of gui pieces, one for each board square
		setupGuiPieceArray();
	
		// label to display game state
		String labelText = this.getGameStateAsText();
		lblGameState = new JLabel(labelText);
		lblGameState.setBounds(10, BOARD_HEIGHT>>1, 250, 100);
		lblGameState.setForeground(Color.WHITE);
		// add JLabel to JPanel (this)
		this.add(lblGameState);
		
		// label to display debug text, via setDebugText(String text)
		debugTextLabel = new JLabel(debugText);
		debugTextLabel.setBounds(10, 10, 400, 80);
		debugTextLabel.setForeground(Color.YELLOW);
		// add JLabel to JPanel (this)
		this.add(debugTextLabel);

		// create application frame and set visible
		HeroesFrame f = new HeroesFrame();
		f.setSize(80, 80);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.add(this);
		f.setSize(imgBackground.getWidth(null) + 0, imgBackground.getHeight(null) + 0);
		
		// add listeners to enable drag and drop pieces
		PiecesDragAndDropListener listener = new PiecesDragAndDropListener(this, f);
		// add listeners to JPanel (this)
		this.addMouseListener(listener);
		this.addMouseMotionListener(listener);
	}

	// initialize animation graphics data
    private void initAnimationData() {
        BufferedImageLoader loader = new BufferedImageLoader();
        BufferedImage spriteSheet = null;
		// load all units as array list of Unit objects
		try {
			data = reader.readData("./img/heroesFrames_sheet2.txt");
		} catch (IOException e) {
			System.out.println("spritesheet text path illegal!");
		}
        
		spriteSheet = loader.loadImage("file:./img/heroesFrames_sheet2.png");
        ss = new SpriteSheet(spriteSheet);     
    }
    
    public ArrayList<Animator> getAnimStates(Piece piece) {
		// create 4 Animator objects, one per state, feed into arraylist
        ArrayList<Animator> animSprites = new ArrayList<Animator>();
		for (int i = 0; i < 4; i++) {
			Animator tmpAnim = new Animator(getSprites(ss, piece.getUnitNbr(), i));
			tmpAnim.setUnit(findUnit(piece.getUnitNbr(), i));
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
    
	// retrieve and store all piece guis into hashmap, using keys with
	// naming convention in lower case: "<colour> <type>". Ex. "white knight"
	private void loadPieceGuisIntoBuffer() {
		int[] colours = {COLOR_WHITE, COLOR_BLACK};
 		for (int colour : colours) {
			for (int pGui = 0; pGui < 6; pGui++) {
				Piece piece = new Piece(colour, PIECE_NAME[pGui].charAt(0), 0, 0);
				PieceGui guiPiece = new PieceGui(piece, getAnimStates(piece));
				guiPiece.getAnim(guiPiece.getState()).setSpeed(200);
				guiPiece.getAnim(guiPiece.getState()).play();
				pieceGuisBuffer.put(Game.getLongName(PIECE_NAME[pGui].charAt(0), colour), guiPiece);
			}
		}
	}
	
	public PieceGui getPieceGuiFromBuffer(String key) {
		return pieceGuisBuffer.get(key);
	}
    
    public void setupGuiPieceArray() {
    	PieceGui guiPiece = null;
    	int colour;
		for(int pos = 0; pos < 64; pos++) {
			colour = -1;
			if(((1L<<pos) & game.getBitBoard().getColourPieces(COLOR_BLACK)) != 0L) {
				colour = COLOR_BLACK;
			} else if(((1L<<pos) & game.getBitBoard().getColourPieces(COLOR_WHITE)) != 0L) {
				colour = COLOR_WHITE;
			} else
				// null when square is empty
				guiPiece = null;
			
			if(colour >= 0) {
				// load guiPiece from PieceGui buffer
				guiPiece = new PieceGui(pieceGuisBuffer.get(Game.getLongName(
						game.getBitBoard().getArraySquare(pos), colour)));
				// set square position
				guiPiece.setPos(pos);
				guiPiece.setId(pos);
			}
			guiPieces[pos] = guiPiece;
		}	
    }
	
	private String getGameStateAsText() {
		String state = "unknown";
		switch (game.getPlayerTurn()) {
			case ChessGame.GAME_STATE_BLACK: state = "black";break;
			case ChessGame.GAME_STATE_END_WHITE_WON: state = "white won";break;
			case ChessGame.GAME_STATE_END_BLACK_WON: state = "black won";break;
			case ChessGame.GAME_STATE_WHITE: state = "white";break;
		}
		if(game.getActivePlayer() != null && game.getActivePlayer().isCheck())
			state += " in check!";
		
		return state;
	}
	
	public String getDebugText() {
		return debugText;
	}

	public void setDebugText(String debugText) {
		this.debugText = debugText;
	}

	public static int convertColumnToX(int column){
		return PIECES_START_X + SQUARE_WIDTH * column;
	}

	public static int convertRowToY(int row){
		return PIECES_START_Y + SQUARE_HEIGHT * row; // OLD: (ROW_8 - row)
	}

	public static int convertXToColumn(int x){
//		return (x - DRAG_TARGET_SQUARE_START_X)/SQUARE_WIDTH;
		return (x - BOARD_START_X)/SQUARE_WIDTH;
	}

	public static int convertYToRow(int y){
		return (y - BOARD_START_Y)/SQUARE_HEIGHT;
	}
	
    public static int getPosFromCoords(int column, int row) {
    	return column + (row * 8);
    }
	
	public void setDragPiece(PieceGui guiPiece) {
		this.dragPiece = guiPiece;
	}

	public PieceGui getDragPiece(){
		return dragPiece;
	}
	
	private boolean isUserDraggingPiece() {
		return this.dragPiece != null;
	}
	
	public void addToCapturedGuiPieces(PieceGui guiPiece) {
		capturedGuiPieces.add(guiPiece);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		// draw background
		g.drawImage(imgBackground, 0, 0, null);

		// draw pieces
		for (PieceGui guiPiece : guiPieces) {
			if(guiPiece != null && guiPiece.getAnim(guiPiece.getState()) != null) {
				guiPiece.getAnim(guiPiece.getState()).update(System.currentTimeMillis());
				g.drawImage(guiPiece.getImage(guiPiece.getState()), guiPiece.getX(), guiPiece.getY() - GUI_PIECE_SCALE,
							guiPiece.getAnim(guiPiece.getState()).unit.getModFrmDim(SQUARE_HEIGHT, getScaleAdd).w,
							guiPiece.getAnim(guiPiece.getState()).unit.getModFrmDim(SQUARE_HEIGHT, getScaleAdd).h, null);
			}
		}
		// draw dead pieces on each side of board, white on the left
		int w = -1, b = -1;
		for (PieceGui guiPiece : capturedGuiPieces) {		
			g.drawImage(guiPiece.getAnim(STATE_DEATH).getSpriteAtFrame(guiPiece.getAnim(STATE_DEATH).getSizeOfFrames()-1), 
					(guiPiece.getColour() == COLOR_WHITE) ? 30 : 900, 
					100 + ( ((guiPiece.getColour() == COLOR_WHITE) ? ++w : ++b) * 
							(int)(guiPiece.getAnim(STATE_DEATH).unit.getModFrmDim(SQUARE_HEIGHT, getScaleAdd).h * 0.8)) - GUI_PIECE_SCALE,
					guiPiece.getAnim(STATE_DEATH).unit.getModFrmDim(SQUARE_HEIGHT, getScaleAdd).w,
					guiPiece.getAnim(STATE_DEATH).unit.getModFrmDim(SQUARE_HEIGHT, getScaleAdd).h, null);
		}

		// draw last move, if user is not dragging game piece
		if( !isUserDraggingPiece() && lastMove != null ){
			int highlightSourceX = convertColumnToX(lastMove.getSrc()%8);
			int highlightSourceY = convertRowToY(lastMove.getSrc()/8);
			int highlightTargetX = convertColumnToX(lastMove.getTrg()%8);
			int highlightTargetY = convertRowToY(lastMove.getTrg()/8);

			g.setColor(Color.YELLOW);
			g.drawRoundRect( highlightSourceX, highlightSourceY, SQUARE_WIDTH-12, SQUARE_HEIGHT-12,10,10);
			g.drawRoundRect( highlightTargetX, highlightTargetY, SQUARE_WIDTH-12, SQUARE_HEIGHT-12,10,10);
		}

		// draw valid target locations, if user is dragging a game piece
		if(isUserDraggingPiece()) {
			// does dragPiece have any moveBits set?
			if(dragPiece.getMoveBits().getBits() != 0L) {
				int[] movePositions = BitBoard.getMultiPos(dragPiece.getMoveBits().getBits());
				drawGreenRects(g, movePositions);
			}
			// draw cyan rectangle on square where dragPiece is currently
			int column = convertXToColumn(dragPiece.getX());
			int row = convertYToRow(dragPiece.getY());
			drawRect(g, getPosFromCoords(column, row), Color.CYAN);
		}
		
		if(game.getActivePlayer().isDebugging()) {
			int[] piecePositions = BitBoard.getMultiPos(game.getBitBoard().getColourPieces(game.getPlayerTurn()));
			drawGreenRects(g, piecePositions);
		}
		
		// draw game state label
		lblGameState.setText(getGameStateAsText());
		debugTextLabel.setText(getDebugText());
		repaint();
	}
	
	public void drawGreenRects(Graphics gr, int[] movePositions) {
		for (int pos : movePositions) {
			drawRect(gr, pos, Color.GREEN);
		}		
	}
	
	public void drawRect(Graphics gr, int pos, Color colour) {
		int rectX = convertColumnToX(pos%8);
		int rectY = convertRowToY(pos/8);
		// draw the rectangle
		gr.setColor(colour);
		gr.drawRoundRect( rectX, rectY, SQUARE_WIDTH-12, SQUARE_HEIGHT-12,10,10);		
	}
	
	public void setNewPieceLocation(PieceGui draggedPiece, int targetPos) {
		// if dragPiece hasn't moved outside of start square, snap back to start
		if(draggedPiece != null) {
			if(draggedPiece.getPos() == targetPos)
				draggedPiece.snapToNearestSquare();
			else if(draggedPiece.getMoveBits() != null) {
				Move move = new Move(draggedPiece.getPos(), targetPos);
				if((draggedPiece.getMoveBits().getBits() & 1L<<targetPos) > 0L) {
					game.getActivePlayer().setCurrentMove(move);
					lastMove = game.getActivePlayer().getLastMove();
				} else {
					// if target square wasn't part of valid moves, snap back to start
					draggedPiece.snapToNearestSquare();
				}
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		} else
			return;
	}

	public Game getGame() {
		return game;
	}
	
	public PieceGui getGuiPiece(int pos) {
		return guiPieces[pos];
	}
	
	public void setGuiPiece(int pos, PieceGui guiPiece) {
		guiPieces[pos] = guiPiece;
	}

	public PieceGui[] getGuiPieces() {
		return guiPieces;
	}

	public void setGuiPieces(PieceGui[] guiPieces) {
		this.guiPieces = guiPieces;
	}

	public int getPiecesSize() {
		return piecesSize;
	}

	public void setPiecesSize(int piecesSize) {
		this.piecesSize = piecesSize;
	}
}
