package game;

public class Square {

	private Coordinate coordinate;
	private int color;
	private Piece piece;
	
	public Square(Coordinate coordinate) {
		this.setCoordinate(coordinate);
		// row + column gives odd = black, even = white
		this.setColor(((coordinate.getRow() + coordinate.getColumn()) % 2 == 0) ? 
											Piece.COLOR_BLACK : Piece.COLOR_WHITE);
		this.setPiece(null);
	}

	public Coordinate getCoordinate() {
		return coordinate;
	}

	public void setCoordinate(Coordinate coordinate) {
		this.coordinate = coordinate;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public Piece getPiece() {
		return piece;
	}

	public void setPiece(Piece piece) {
		this.piece = piece;
	}
}
