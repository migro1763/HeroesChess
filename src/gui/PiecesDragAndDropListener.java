package gui;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;

import javax.swing.JFrame;

import game.ChessGame;
//import game.Piece;

public class PiecesDragAndDropListener implements MouseListener, MouseMotionListener {

	private List<GuiPiece> guiPieces;
	private HeroesChessGui heroesChessGui;
	
	private int dragOffsetX;
	private int dragOffsetY;
	
	static Point mouseDownCompCoords;
	private JFrame frame;

	public PiecesDragAndDropListener(List<GuiPiece> guiPieces, HeroesChessGui chessGui, JFrame f) {
		this.guiPieces = guiPieces;
		this.heroesChessGui = chessGui;
		frame = f;
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {}

	@Override
	public void mouseEntered(MouseEvent arg0) {}

	@Override
	public void mouseExited(MouseEvent arg0) {}

	@Override
	public void mousePressed(MouseEvent evt) {
		mouseDownCompCoords = evt.getPoint();
		
		if( !this.heroesChessGui.isDraggingGamePiecesEnabled())
			return;
		
//		int x = evt.getPoint().x;
//		int y = evt.getPoint().y;
		int x = mouseDownCompCoords.x;
		int y = mouseDownCompCoords.y;
		
		// find out which piece to move.
		// we check the list from top to bottom
		// (therefore we iterate in reverse order)
		for (int i = this.guiPieces.size()-1; i >= 0; i--) {
			GuiPiece guiPiece = this.guiPieces.get(i);
			if (guiPiece.isCaptured()) continue;

			if(mouseOverPiece(guiPiece, x, y)) {	
//				if( (	this.chessGui.getGameState() == ChessGame.GAME_STATE_WHITE
//						&& guiPiece.getColor() == Piece.COLOR_WHITE
//					) ||
//					(	this.chessGui.getGameState() == ChessGame.GAME_STATE_BLACK
//							&& guiPiece.getColor() == Piece.COLOR_BLACK
//						)
//					) {
				
				// only pick up pieces of same color as current player
				if(guiPiece.getColor() == this.heroesChessGui.getGameState()) {
					// calculate offset, because we do not want the drag piece
					// to jump with it's upper left corner to the current mouse
					// position
					this.dragOffsetX = x - guiPiece.getX();
					this.dragOffsetY = y - guiPiece.getY();
					this.heroesChessGui.setDragPiece(guiPiece);
//					this.chessGui.repaint();
					break;
				}
			}
		}
		
		// move drag piece to the top of the list
		if(this.heroesChessGui.getDragPiece() != null){
			this.guiPieces.remove( this.heroesChessGui.getDragPiece() );
			this.guiPieces.add(this.heroesChessGui.getDragPiece());
		}
	}

	// check whether the mouse is currently over this piece
	private boolean mouseOverPiece(GuiPiece guiPiece, int x, int y) {

		return guiPiece.getX() <= x 
			&& guiPiece.getX()+guiPiece.getWidth() >= x
			&& guiPiece.getY() <= y
			&& guiPiece.getY()+guiPiece.getHeight() >= y;
	}

	@Override
	public void mouseReleased(MouseEvent evt) {
		mouseDownCompCoords = null;
		
		if( this.heroesChessGui.getDragPiece() != null){
			int x = evt.getPoint().x - this.dragOffsetX;
			int y = evt.getPoint().y - this.dragOffsetY;
			
			// set game piece to the new location if possible
			//
			heroesChessGui.setNewPieceLocation(this.heroesChessGui.getDragPiece(), x, y);
			this.heroesChessGui.repaint();
			this.heroesChessGui.setDragPiece(null);
		}
	}

	@Override
	public void mouseDragged(MouseEvent evt) {
        Point currCoords = evt.getLocationOnScreen();
        
		if(this.heroesChessGui.getDragPiece() != null) {
			
			int x = evt.getPoint().x - this.dragOffsetX;
			int y = evt.getPoint().y - this.dragOffsetY;
			
			GuiPiece dragPiece = this.heroesChessGui.getDragPiece();
			dragPiece.setX(x);
			dragPiece.setY(y);
			
			this.heroesChessGui.repaint();
		} else {
	        frame.setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
		}
		
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {}

}
