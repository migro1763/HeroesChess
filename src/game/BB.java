package game;

public class BB {
	private long bits;
	private int colour;
	
	public BB(long bits, int colour) {
		this.bits = bits;
		this.colour = colour;
	}
	
	public BB() {
		this.bits = 0L;
		this.colour = 0;
	}
	
	public void setBits(long bits) {
		this.bits |= bits;
	}
	
	public long getBits() {
		return bits;
	}

	public int getColour() {
		return colour;
	}

	public void setColour(int colour) {
		this.colour = colour;
	}
}
