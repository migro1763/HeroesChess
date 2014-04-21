package gui;

class Frm implements Cloneable {
	public int x = 0;
	public int y = 0;
	public int w = 0;
	public int h = 0;

	public Frm(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	public Frm() {
		this(0, 0, 0, 0);
	}
	
	@Override
	public Frm clone() {
		return new Frm(x, y, w, h);
	}
}