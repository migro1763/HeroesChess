package gui;

import java.util.ArrayList;

public class Unit {
	public String unit;
	public String state;
	public int frames = 0;
	public ArrayList<Frm> frmDims = new ArrayList<Frm>();

	public Unit(String unit, String state) {
		this.unit = unit;
		this.state = state;
	}

	@Override
	public String toString() {
		return (unit + "_" + state);
	}

	public void addFrmDim(int x, int y, int w, int h) {
		frmDims.add(new Frm(x, y, w, h));
	}

	public Frm getFrm(int frameNbr) {
		return frmDims.get(frameNbr);
	}
	
	// returns frame 0 scaled to height of game square size
	public Frm getModFrmDim(int height, double scaleAdd) {	
		Frm modFrm = frmDims.get(0).clone();
		double aspect = ((double)height / (double)modFrm.h);
		if(aspect < 0.8)
			aspect = 0.8;
		aspect += scaleAdd;
		modFrm.w = (int)(aspect * modFrm.w);
		modFrm.h = (int)(aspect * modFrm.h);
		return modFrm;
	}

}