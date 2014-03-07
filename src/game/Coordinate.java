package game;

public class Coordinate {
	
	private int row;
	private int column;
	
	public Coordinate(int row, int column) {
		this.setRow(row);
		this.setColumn(column);
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}
	
	public boolean equals(Coordinate coordinate) {
		return coordinate.getRow() == row && coordinate.getColumn() == column;
	}
	
	@Override
	public String toString() {
		return Piece.getRowString(column) + Piece.getColumnString(row);
	}
}
