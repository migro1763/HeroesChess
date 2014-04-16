package gui;

import game.BB;
import interfaces.GuiParams;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;

import players.Player;

public class PiecesDragAndDropListener implements MouseListener, MouseMotionListener, GuiParams {

	private ChessBoardGui board;
	
	private int dragOffsetX;
	private int dragOffsetY;
	
	static Point mouseDownCompCoords;
	private JFrame frame;

	public PiecesDragAndDropListener(ChessBoardGui board, JFrame f) {
		this.board = board;
		frame = f; // for moving the game frame window with the mouse
	}
	
	private int getMouseOverPos(int x, int y) {
		int pos = -1;
		if(x > BOARD_START_X && x < (BOARD_START_X + BOARD_WIDTH) &&
				y > BOARD_START_Y && y < (BOARD_START_Y + BOARD_HEIGHT)) {
			pos = ChessBoardGui.getPosFromCoords(ChessBoardGui.convertXToColumn(x), 
												ChessBoardGui.convertYToRow(y));
		}
		return (pos <= 63) ? pos : 63; // return max value 63
	}

	@Override
	public void mousePressed(MouseEvent evt) {
		mouseDownCompCoords = evt.getPoint();
		Player activePlayer = board.getGame().getActivePlayer();
		int playerTurn = board.getGame().getPlayerTurn();
		
		if(!activePlayer.isDragPiecesEnabled())
			return;
		
		int x = mouseDownCompCoords.x;
		int y = mouseDownCompCoords.y;
		
		int mouseOverPos = getMouseOverPos(x, y);
		if(mouseOverPos >= 0) {
			PieceGui guiPieceInFocus = board.getGuiPiece(mouseOverPos);
			if(guiPieceInFocus != null) {
				if(guiPieceInFocus.getColour() == playerTurn) {
					// calculate offset, because we do not want the drag piece
					// to jump with it's upper left corner to the current mouse position
					dragOffsetX = x - guiPieceInFocus.getX();
					dragOffsetY = y - guiPieceInFocus.getY();
					guiPieceInFocus.setState(STATE_WALK);
					guiPieceInFocus.getAnim(STATE_WALK).setIdlePause(0);
					guiPieceInFocus.getAnim(STATE_WALK).setSpeed(150);
					guiPieceInFocus.getAnim(STATE_WALK).play();
					
					BB dragPieceMoveBits = board.getGame().getMoveGen().possibleMoves(
							playerTurn, guiPieceInFocus.getPos(), board.getGame().getPawnHistory());

					guiPieceInFocus.setMoveBits(dragPieceMoveBits);
					board.setDragPiece(guiPieceInFocus);
				}
			} 
		}
	}

	@Override
	public void mouseDragged(MouseEvent evt) {
        Point currCoords = evt.getLocationOnScreen();
        
		if(board.getDragPiece() != null) {			
			int x = evt.getPoint().x - dragOffsetX;
			int y = evt.getPoint().y - dragOffsetY;
			
			PieceGui dragPiece = board.getDragPiece();
			dragPiece.setX(x);
			dragPiece.setY(y);
		} else {
			// moves the entire game window if not dragging a gui piece. y - 23 (menubar height)
	        frame.setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y - 23); 
		}
	}
	
	@Override
	public void mouseReleased(MouseEvent evt) {
		mouseDownCompCoords = null;
//		Game.threadPause(20);
		if(board.getDragPiece() != null){
			// set game piece to the new location if possible
			PieceGui dragPiece = board.getDragPiece();
			if(dragPiece != null) {
				int pos = ChessBoardGui.getPosFromXY(evt.getPoint().x, evt.getPoint().y);
				board.getGame().setNewPieceLocation(dragPiece, pos);
		    	dragPiece.setState(STATE_IDLE);
		    	dragPiece.getAnim(STATE_IDLE).resetIdlePause();
		    	dragPiece.getAnim(STATE_IDLE).play();
			}
			board.setDragPiece(null);
		}
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {}
	
	@Override
	public void mouseClicked(MouseEvent arg0) {}

	@Override
	public void mouseEntered(MouseEvent arg0) {}

	@Override
	public void mouseExited(MouseEvent arg0) {}

}
