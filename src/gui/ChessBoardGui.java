package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import listeners.PiecesDragAndDropListener;
import pieces.Piece;
import game.BitBoard;
import game.Game;
import game.Move;
import interfaces.GuiParams;
import interfaces.Vals;

public class ChessBoardGui extends JPanel implements GuiParams, Vals {
	
	private static final long serialVersionUID = 1L;
	
	private static final String bgImgPaths[] = {"/img/gameBg_grasshills.png",
												"/img/gameBg_desert.png",
												"/img/gameBg_swamp.png",
												"/img/gameBg_winter.png"
												};
	private static final String spriteSheetTxtPath = "heroesFrames_sheet2.txt";
	private static final String spriteSheetImgPath = "/img/heroesFrames_sheet2.png";
	
	private static boolean DEBUG = true;
	private static boolean SHOW_MOVES = true;
	private static boolean SHOW_LAST_MOVE = true;
	private static boolean SHOW_CURRENT_SQUARE = true;
	
	private Image imgBackground;
	private JLabel lblGameState;
	private ScrollLabel debugLabel, historyLabel;
	private HtmlLabel debugText, historyText;

	private Game game;
	private HashMap<String, PieceGui> pieceGuisBuffer = new HashMap<String, PieceGui>();
	private PieceGui[] guiPieces = new PieceGui[64];
	private List<PieceGui> capturedGuiPieces = new ArrayList<PieceGui>();
	private PieceGui dragPiece = null, attackPiece = null, deathPiece = null;

	public Move lastMove = null;
	private double getScaleAdd;
	
	private HeroesFrame frame;
	private SpriteSheet ss;
	private Reader reader = new Reader();
	private ArrayList<Unit> data;
    public double scale = 1.0;
    public int speed = 200; // smaller = faster
    public int whichUnit = 0;
    public int whichState = 0;
    
    public ChessBoardGui(Game game) {
    	// set game object as the created chess game parameter
		this.game = game;
		SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	            createAndShowGUI();
	        }
        }); 	
	}
    
    private void createAndShowGUI() {
		// load graphics into buffers/memory
		loadGraphics();	
		// create application frame
		frame = new HeroesFrame(imgBackground.getWidth(null), imgBackground.getHeight(null)+23); // menubar height = 23
		// set up all gui elements
		initGameGui(frame);
		frame.add(this);
		// configure game frame	
		frame.display();	
		
		// setup 64-size array of gui pieces, one for each board square
		setupGuiPieceArray();
		
		// add listeners to enable drag and drop pieces
		PiecesDragAndDropListener listener = new PiecesDragAndDropListener(this, frame);
		// add listeners to JPanel (this)
		this.addMouseListener(listener);
		this.addMouseMotionListener(listener);  	
    }
    
    private void loadGraphics() {
    	// load background image
    	BufferedImageLoader imgLoader = new BufferedImageLoader();
//			this.imgBackground = new ImageIcon(ImageIO.read(this.getClass().getResource(bgImgPath))).getImage();
		this.imgBackground = imgLoader.loadImage(bgImgPaths[new Random().nextInt(4)]);
    	// factor to scale gui pieces to fit board graphics
		getScaleAdd = ((double)GUI_PIECE_SCALE / 12.0) - 1.0;

		// load animation graphics and text file of all sprites
		initAnimationData();
		
		// load piece gui graphics into hashmap buffer
		loadPieceGuisIntoBuffer(); 
    }
    
    private void initGameGui(HeroesFrame frame) {
    	this.setLayout(null);
		int bgWidth = imgBackground.getWidth(null);
		int bgHeight = imgBackground.getHeight(null);
		
		// menu
		JMenuBar menubar = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenu options = new JMenu("Options");
        JMenuItem fileMenuExit = new JMenuItem("Exit HeroesChess");
        JCheckBoxMenuItem  optionsMenuDebug = new JCheckBoxMenuItem ("Debug mode", DEBUG);
        JCheckBoxMenuItem  optionsMenuMoves = new JCheckBoxMenuItem ("Show valid moves", SHOW_MOVES);
        JCheckBoxMenuItem  optionsMenuCurrent = new JCheckBoxMenuItem ("Show current square", SHOW_CURRENT_SQUARE);
        JCheckBoxMenuItem  optionsMenuLast = new JCheckBoxMenuItem ("Show last move", SHOW_LAST_MOVE);
        fileMenuExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
        });
        optionsMenuDebug.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DEBUG = !DEBUG;			
			}
        });
        optionsMenuMoves.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SHOW_MOVES = !SHOW_MOVES;			
			}
        });
        optionsMenuCurrent.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SHOW_CURRENT_SQUARE = !SHOW_CURRENT_SQUARE;			
			}
        });
        optionsMenuLast.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SHOW_LAST_MOVE = !SHOW_LAST_MOVE;			
			}
        });
        file.add(fileMenuExit);
        options.add(optionsMenuDebug);
        options.add(optionsMenuMoves);
        options.add(optionsMenuCurrent);
        options.add(optionsMenuLast);
        
        menubar.add(file);
        menubar.add(options);	
        frame.setJMenuBar(menubar);
        
		// label to display game state
		String labelText = getGameStateAsText();
		lblGameState = new JLabel(labelText);

		lblGameState.setFont(GAME_FONT);
		int posX = (bgWidth - (bgWidth-BOARD_WIDTH>>1)) - (labelText.length()<<3);
		lblGameState.setBounds(posX, 40, 200, 20);
		lblGameState.setForeground(new Color(255, 255, 255, 128));
		lblGameState.setOpaque(false);
		// add JLabel to JPanel (this)
		this.add(lblGameState);
		
		// label to display debug text, via setDebugText(String text)
		debugText = new HtmlLabel();
		debugLabel = new ScrollLabel(getDebugText(), 10, 5, 300, 110);
		this.add(debugLabel);
		
		historyText = new HtmlLabel();
		historyLabel = new ScrollLabel(getHistoryText(), bgWidth-180, bgHeight-500, 150, 480);
		historyLabel.setTextColour(COLOUR_WHITE_50);
		this.add(historyLabel);  	
    }

	// initialize animation graphics data
    private void initAnimationData() {
        BufferedImageLoader loader = new BufferedImageLoader();
        BufferedImage spriteSheet = null;
		// load all units as array list of Unit objects
        System.out.println("Loading spritesheet text: " + 
        		this.getClass().getResource(spriteSheetTxtPath).getFile());
		try {
			data = reader.readData(spriteSheetTxtPath);
		} catch (IOException e) {
			System.out.println("ChessBoardGui: spritesheet text path illegal!");
		}
		spriteSheet = loader.loadImage(spriteSheetImgPath);
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
				Piece piece = new Piece(colour, PIECE_NAME[pGui], 0);
				PieceGui guiPiece = new PieceGui(piece, getAnimStates(piece));
				guiPiece.getAnim(guiPiece.getState()).setSpeed(200);
				guiPiece.getAnim(guiPiece.getState()).play();
				pieceGuisBuffer.put(Game.getLongName(PIECE_NAME[pGui], colour), guiPiece);
			}
		}
	}
	
	public PieceGui getPieceGuiFromBuffer(String key) {
		return pieceGuisBuffer.get(key);
	}
    
	// set up all gui pieces
    public void setupGuiPieceArray() {
		for(int pos = 0; pos < 64; pos++)
			guiPieces[pos] = getNewGuiPieceAtPos(pos);
    }
    
    // load one new guiPiece from buffer at position
    public PieceGui getNewGuiPieceAtPos(int pos) {
    	char type = game.getBitBoard().getArraySquare(pos);
    	int colour;
    	PieceGui guiPiece;
		// determine piece colour
		if(((1L<<pos) & game.getBitBoard().getColourPieces(COLOR_BLACK)) != 0L) {
			colour = COLOR_BLACK;
		} else if(((1L<<pos) & game.getBitBoard().getColourPieces(COLOR_WHITE)) != 0L) {
			colour = COLOR_WHITE;
		} else
			// if square is empty
			colour = -1;
		// set gui piece on square
		if(colour >= 0) {
			// load guiPiece from PieceGui buffer
			guiPiece = new PieceGui(getPieceGuiFromBuffer(Game.getLongName(type, colour)));
			guiPiece.getAnim(STATE_IDLE).setRandomIdlePauseDuration();
			guiPiece.getAnim(STATE_IDLE).resetIdlePause();
			// set square position
			guiPiece.setPos(pos);
		} else {
			// null if square is empty
			guiPiece = null;
		}
		return guiPiece;
    }
    
    public void reloadGuiPieceAtPos(int pos) {
    	guiPieces[pos] = getNewGuiPieceAtPos(pos);
    }
    
	public void updateGuiPieces(Move move, int attackedPiecePos) {
		PieceGui guiPiece = guiPieces[move.getSrc()];
		guiPiece.setPos(move.getTrg());
		guiPieces[move.getSrc()] = null;
		guiPieces[move.getTrg()] = guiPiece;
		if(attackedPiecePos != -1)
			guiPieces[attackedPiecePos] = null;
	}
	
	private String getGameStateAsText() {
		String state = "Unknown";
		switch (game.getGameState()) {
			case COLOR_WHITE: state = "White player: " + game.getPlayer(COLOR_WHITE); break;
			case COLOR_BLACK: state = "Black player: " + game.getPlayer(COLOR_BLACK); break;
			case WHITE_WON: state = "White player " + game.getPlayer(COLOR_WHITE) + " has won!"; break;
			case BLACK_WON: state = "Black player " + game.getPlayer(COLOR_BLACK) + " has won!"; break;
			default:
		}
		if(game.getActivePlayer() != null && game.getActivePlayer().isCheck() && 
				!game.getActivePlayer().isCheckMate())
			state += " in check!";	
		return state;
	}

	public static int convertColumnToX(int column){
		return PIECES_START_X + SQUARE_WIDTH * column;
	}

	public static int convertRowToY(int row){
		return PIECES_START_Y + SQUARE_HEIGHT * row; // OLD: (ROW_8 - row)
	}

	public static int convertXToColumn(int x){
		return (x - BOARD_START_X)/SQUARE_WIDTH;
	}

	public static int convertYToRow(int y){
		return (y - BOARD_START_Y)/SQUARE_HEIGHT;
	}
	
    public static int getPosFromXY(int x, int y) {
    	return convertXToColumn(x) + ( convertYToRow(y) * 8);
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
		Animator anim = null;
		int colour = 0, offsetX = 0, offsetY = 0, typeNbr = 0, state = 0;
		// draw background
		g.drawImage(imgBackground, 0, 0, null);

		// draw pieces
		for (PieceGui guiPiece : guiPieces) {
			if(guiPiece != null && guiPiece.getAnim(guiPiece.getState()) != null) {
				state = guiPiece.getState();
				colour = guiPiece.getColour();
				anim = guiPiece.getAnim(state);
				typeNbr = Game.getTypeNumber(guiPiece.getType(), colour);
				offsetX = UNIT_ANIM_STARTX[state][typeNbr];
				offsetY = UNIT_ANIM_STARTY[state][typeNbr];
				
				anim.update(System.currentTimeMillis());
				g.drawImage(guiPiece.getImage(state), guiPiece.getX()+offsetX, guiPiece.getY()+offsetY - GUI_PIECE_SCALE,
						anim.unit.getModFrmDim(SQUARE_HEIGHT, getScaleAdd).w,
						anim.unit.getModFrmDim(SQUARE_HEIGHT, getScaleAdd).h, null);
			}
		}
		// draw dead pieces on each side of board, white on the left
		int w = -1, b = -1;
		for (PieceGui guiPiece : capturedGuiPieces) {
			anim = guiPiece.getAnim(STATE_DEATH);
			colour = guiPiece.getColour();
			g.drawImage(anim.getSpriteAtFrame(anim.getSizeOfFrames()-1), (colour == COLOR_WHITE) ? 30 : 900, 100 + 
						(((colour == COLOR_WHITE) ? ++w : ++b) * 
							(int)(anim.unit.getModFrmDim(SQUARE_HEIGHT, getScaleAdd).h * 0.8)) - 
							GUI_PIECE_SCALE, anim.unit.getModFrmDim(SQUARE_HEIGHT, getScaleAdd).w,
							anim.unit.getModFrmDim(SQUARE_HEIGHT, getScaleAdd).h, null);
		}
		
		// draw guiPieces in attack, i.e. attack and death animation
		if(deathPiece != null && attackPiece != null) {
			// death animation
			if(!deathPiece.getAnim(STATE_DEATH).isFinishedDeath()) {
				typeNbr = Game.getTypeNumber(deathPiece.getType(), colour);
				offsetX = UNIT_ANIM_STARTX[STATE_DEATH][typeNbr];
				offsetY = UNIT_ANIM_STARTY[STATE_DEATH][typeNbr];
				attackPiece.setState(STATE_ATTACK); // making sure attackPiece has attack animation
				deathPiece.getAnim(STATE_DEATH).update(System.currentTimeMillis());
				g.drawImage(deathPiece.getImage(STATE_DEATH), deathPiece.getX()+offsetX, 
						deathPiece.getY()+offsetY - GUI_PIECE_SCALE,
						deathPiece.getAnim(STATE_DEATH).unit.getModFrmDim(SQUARE_HEIGHT, getScaleAdd).w,
						deathPiece.getAnim(STATE_DEATH).unit.getModFrmDim(SQUARE_HEIGHT, getScaleAdd).h, null);
			} else {
				deathPiece = null;
				attackPiece.setState(STATE_IDLE);
				attackPiece.getAnim(STATE_IDLE).play();
				attackPiece = null;
			}
		}
		
		// draw square rectangles section -----------------------------------------------------------------
		// draw last move as yellow rectangles, if user is not dragging game piece
		if(!isUserDraggingPiece()) {
			if(SHOW_LAST_MOVE) {
				Move lastPlayersMove = null;
				try {
					lastPlayersMove = game.getPlayer(1-game.getPlayerTurn()).getLastMove();
				} catch (Exception e) {}
				if(lastPlayersMove != null) {
					drawRect(g, lastPlayersMove.getSrc(), COLOUR_YELLOW);
					drawRect(g, lastPlayersMove.getTrg(), COLOUR_YELLOW);
				}
			}
		}

		// draw valid target locations, if user is dragging a game piece
		if(isUserDraggingPiece()) {
			// does dragPiece have any moveBits set?
			if(SHOW_MOVES && dragPiece.getMoveBits().getBits() != 0L) {
				int[] movePositions = BitBoard.getMultiPos(dragPiece.getMoveBits().getBits());
				drawGreenRects(g, movePositions);
			}
			// draw cyan rectangle on square where dragPiece is currently
			if(SHOW_CURRENT_SQUARE) {
				int column = convertXToColumn(dragPiece.getX());
				int row = convertYToRow(dragPiece.getY());
				drawRect(g, getPosFromCoords(column, row), COLOUR_CYAN);
			}
		}	
		// draw square rectangles section end -------------------------------------------------------------
		
		// draw game state label
		lblGameState.setText(getGameStateAsText());
		if(DEBUG)	
			debugLabel.setVisible(true);
		else
			debugLabel.setVisible(false);
		debugLabel.setText(getDebugText());
		historyLabel.setText(getHistoryText());
		
		repaint();
	}
	
	public void drawRect(Graphics gr, int pos, Color colour) {
		int rectX = convertColumnToX(BitBoard.getX(pos));
		int rectY = convertRowToY(BitBoard.getY(pos));
		// draw the rectangle
		gr.setColor(colour);
		gr.drawRoundRect( rectX, rectY, SQUARE_WIDTH-12, SQUARE_HEIGHT-12,10,10);		
	}
	
	public void drawGreenRects(Graphics gr, int[] movePositions) {
		for (int pos : movePositions) {
			drawRect(gr, pos, COLOUR_GREEN);
		}		
	}

	public Game getGame() {
		return game;
	}
	
	public String getDebugText() {
		 return debugText.getText();
	}

	public void setDebugText(String debugText) {
		this.debugText.addLine(debugText);
	}
	
	public String getHistoryText() {
		 return historyText.getText();
	}

	public void setHistoryText(String text) {
		historyText.addLine(text);
	}

	public PieceGui getGuiPiece(int pos) {
		return guiPieces[pos];
	}

	public PieceGui[] getGuiPieces() {
		return guiPieces;
	}

	public void playAttackAnim(Move move, PieceGui attackingPiece, PieceGui attackedPiece) {
    	// snap attacker half a square left of square
		if(attackingPiece != null && attackedPiece != null) {
			setDebugText("playing attack anim");
			attackPiece = attackingPiece;
			attackPiece.getAnim(attackPiece.getState()).stop();
			attackPiece.setState(STATE_ATTACK);
			attackPiece.getAnim(STATE_ATTACK).setSpeed(200);
			attackPiece.getAnim(STATE_ATTACK).play();
			deathPiece = attackedPiece;
			deathPiece.snapToNearestSquare(true, false);
			deathPiece.getAnim(deathPiece.getState()).stop();
			deathPiece.setState(STATE_DEATH);
			deathPiece.getAnim(STATE_DEATH).setDead(true);
			deathPiece.getAnim(STATE_DEATH).setSpeed(200);
			deathPiece.getAnim(STATE_DEATH).setFinishedDeath(false);
			deathPiece.getAnim(STATE_DEATH).play();
		}
	}

	public void playKingDeath(int colour) {
		setDebugText("playing attack anim");
		deathPiece = guiPieces[game.getBitBoard().getKingPos(colour)];
		deathPiece.getAnim(deathPiece.getState()).stop();
		deathPiece.setState(STATE_DEATH);
		deathPiece.getAnim(STATE_DEATH).setDead(true);
		deathPiece.getAnim(STATE_DEATH).setSpeed(200);
		deathPiece.getAnim(STATE_DEATH).setFinishedDeath(false);
		deathPiece.getAnim(STATE_DEATH).play();
	}
}
