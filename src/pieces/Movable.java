package pieces;

public abstract class Movable {
	
	protected int colour, pos, id;
	
	public Movable(int colour, int pos, int id) {
		this.colour = colour;
		this.pos = pos;
		this.id = id;
	}
	
	public int getColour() {
		return colour;
	}

	public void setColour(int colour) {
		this.colour = colour;
	}

	public int getRow() {
		return pos/8;
	}
	
	public int getColumn() {
		return pos%8;
	}
	
	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
