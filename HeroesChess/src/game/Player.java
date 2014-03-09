package game;

import javax.swing.JPanel;

public abstract class Player extends JPanel {

	private static final long serialVersionUID = 4140234403329894661L;
	private boolean isCheck;
	
	public Move getMove() {
		return null;
	}
	
    public void moveSuccessfullyExecuted(Move move) {}

	public boolean isCheck() {
		return isCheck;
	}

	public void setCheck(boolean isCheck) {
		this.isCheck = isCheck;
	};
}
