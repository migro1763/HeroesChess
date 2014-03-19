package gui;

import game.BitBoard;
import game.Speak;
import interfaces.Declarations;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
//import java.util.List;
//import game.BB;


import javax.swing.JFrame;

public class PiecesDragAndDropListener implements MouseListener, MouseMotionListener, Declarations {

	private ChessBoardGui board;
	
	private int dragOffsetX;
	private int dragOffsetY;
	
	static Point mouseDownCompCoords;
	private JFrame frame;

	public PiecesDragAndDropListener(ChessBoardGui board, JFrame f) {
		this.board = board;
		frame = f;
	}

	@Override
	public void mousePressed(MouseEvent evt) {
		mouseDownCompCoords = evt.getPoint();
		
		if(!board.getGame().getActivePlayer().isDragPiecesEnabled())
			return;
		
		int x = mouseDownCompCoords.x;
		int y = mouseDownCompCoords.y;
		
		int mouseOverPos = getMouseOverPos(x, y);
//		Speak.say("mouse is over pos: " + mouseOverPos, true);
		board.getGame().getActivePlayer().setDebugging(false);
		if(mouseOverPos > -1) {
			PieceGui guiPieceInFocus = board.getGuiPiece(mouseOverPos);
			if(guiPieceInFocus != null) {
//				Speak.say("dragPiece: " + guiPieceInFocus, true);
				if(guiPieceInFocus.getColour() == board.getGame().getPlayerTurn()) {
					// calculate offset, because we do not want the drag piece
					// to jump with it's upper left corner to the current mouse position
					dragOffsetX = x - guiPieceInFocus.getX();
					dragOffsetY = y - guiPieceInFocus.getY();
					board.setDragPiece(guiPieceInFocus);
				}
			}
		} 
		// DEBUG: write array of all pieces in console
		else {
//			for (int pos = 0; pos < 64; pos++)
//				Speak.say(pos + ": " + board.getGuiPiece(pos), true);
			board.getGame().getActivePlayer().setDebugging(true);
		}
	}
	
	private int getMouseOverPos(int x, int y) {
		int pos = -1;
		if(x > BOARD_START_X && x < (BOARD_START_X + BOARD_WIDTH) &&
				y > BOARD_START_Y && y < (BOARD_START_Y + BOARD_HEIGHT)) {
			int row = ((x - BOARD_START_X) / SQUARE_WIDTH);
			int column = ((y - BOARD_START_Y) / SQUARE_HEIGHT);
			pos = BitBoard.getPosFromCoords(column, row);
		}
		return pos;
	}

	@Override
	public void mouseReleased(MouseEvent evt) {
		mouseDownCompCoords = null;
		
		if(board.getDragPiece() != null){
			int x = evt.getPoint().x - dragOffsetX;
			int y = evt.getPoint().y - dragOffsetY;
			
			// set game piece to the new location if possible
			board.setNewPieceLocation(board.getDragPiece(), getMouseOverPos(x, y));
			board.repaint();
			board.setDragPiece(null);
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
			
			board.repaint();
		} else {
			// moves the entire game window if not dragging a gui piece
	        frame.setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
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
