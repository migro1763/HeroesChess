package pieces;

public abstract class Movable {
	
	protected int colour, pos;
	
	public Movable(int colour, int pos) {
		this.colour = colour;
		this.pos = pos;
	}
	
	public int getColour() {
		return colour;
	}

	public void setColour(int colour) {
		this.colour = colour;
	}

	public int getRow() {
		return (pos/8);
	}
	
	public int getColumn() {
		return (pos%8);
	}
	
	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}
}
