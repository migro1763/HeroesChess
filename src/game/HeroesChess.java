package game;

import gui.HeroesFrame;
import gui.OptionsDiag;
import interfaces.Vals;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;

import players.Computer;
import players.Human;
import players.Network;
import players.Player;

public class HeroesChess extends JPanel implements Vals {
	
	private static final long serialVersionUID = 1L;
	
	String[] iPlayerHandlerOptions = new String[] {"Swing GUI", "AI Player", "Network Player"};
	
	public HeroesChess() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		OptionsDiag diag = new OptionsDiag("What should be the white player?", iPlayerHandlerOptions, 
				0, screenSize.width/2 - 200, screenSize.height/2 - 200);
		int whitePlayerOption = diag.getChoice();
		int bOption = 0;
		String[] blackOptions = new String[iPlayerHandlerOptions.length-1];
		for (int i = 0; i < iPlayerHandlerOptions.length; i++)
			if(i != whitePlayerOption)
				blackOptions[bOption++] = iPlayerHandlerOptions[i];
		diag = new OptionsDiag("What should be the black player?", blackOptions, 
				0, screenSize.width/2 - 200, screenSize.height/2 - 200);
		int blackPlayerOption = diag.getChoice();
		diag.dispose();

		// in case of network players, ask for details
		String gameIdOnServer = null;
		String gamePassword = null;
		String name = "";
		if(whitePlayerOption == PLAYER_OPTION_NETWORK) {
			gameIdOnServer = Speak.ask("Game ID on server: ");
			gamePassword = Speak.ask("Password for game: ");
		} 
		if(blackPlayerOption == 1) { // if black player = network by blackOptions numbers
			OptionsDiag diag3 = new OptionsDiag("Password for new game:", "Enter new game password",
					screenSize.width/2, screenSize.height/2);
			gamePassword = diag3.getInputText();
			blackPlayerOption = PLAYER_OPTION_NETWORK; // convert to iPlayerHandlerOptions numbers
		}

		// create the game logic		
		Game game = new Game();

		// assign white and black player
		Player playerWhite = null, playerBlack = null;
		try {
			playerWhite = getPlayerHandler(whitePlayerOption, game, gameIdOnServer, gamePassword, "white");
		} catch (IOException e) {}
		
		// if white and black are the same (e.g. both Swing)
		// then do not create a new player instance, but reuse the already
		// created ones. Otherwise we would create two Swing frames.
		if (whitePlayerOption == blackPlayerOption) {
			playerBlack = playerWhite;
		} else {
			try {
				playerBlack = getPlayerHandler(blackPlayerOption, game, gameIdOnServer, gamePassword, "black");
			} catch (IOException e) {}			
		}

		// then we attach the clients/players to the game
		game.setPlayer(0, playerWhite);
		game.setPlayer(1, playerBlack);

		// in the end we start the game
		new Thread(game).start();		
	}
	
	private static Player getPlayerHandler(int playerHandlerOption,
			Game game, String gameIdOnServer, String gamePassword, String name) throws IOException {
		switch (playerHandlerOption) {
			case PLAYER_OPTION_AI: return new Computer();
			case PLAYER_OPTION_NETWORK: return new Network(gameIdOnServer, gamePassword, name);
			case PLAYER_OPTION_HUMAN: return new Human(name);
			default: throw new IllegalArgumentException("Invalid player option:" + playerHandlerOption);
		}
	}
	
	public static void main(String[] args) throws IOException {
		new HeroesChess();
	}
}