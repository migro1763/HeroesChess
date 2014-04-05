package game;
import interfaces.Vals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import aiAlgorithms.SimpleAiPlayerHandler;
import gui.ChessBoardGui;
import pieces.Piece;
import players.Computer;
import players.Human;
import players.Player;
//import interfaces.IPlayerHandler;
//import net.XmlRpcPlayerHandler;

public class HeroesChess implements Vals {
	
	public static Runtime runtime = Runtime.getRuntime();
//	public static long startMem = runtime.freeMemory();

	public static void main(String[] args) throws IOException {

		// ask for player handlers to be used for this game
		//
//		String[] iPlayerHandlerOptions = new String[] {
//			"Swing GUI", "AI Player"
//		}; //, "Network Player"
//		int whitePlayerOption = Speak.ask("What should be the white player?", iPlayerHandlerOptions);
//		int blackPlayerOption = Speak.ask("What should be the black player?", iPlayerHandlerOptions);
		int whitePlayerOption = 0;
		int blackPlayerOption = 0;

		// in case of network players, ask for details
		String gameIdOnServer = null;
		String gamePassword = null;
//		if(whitePlayerOption == PLAYER_OPTION_NETWORK) {
//			gameIdOnServer = Speak.ask("Game ID on server:");
//			gamePassword = Speak.ask("Password for game:");
//		}
//		
//		if(blackPlayerOption == PLAYER_OPTION_NETWORK) {
//			gamePassword = Speak.ask("Password for new game:");
//		}

		// create the game logic
		
		Game game = new Game();

		// assign white and black player
		Player playerWhite = getPlayerHandler(whitePlayerOption, game, gameIdOnServer, gamePassword, "Mikkel");
		Player playerBlack = getPlayerHandler(blackPlayerOption, game, gameIdOnServer, gamePassword, "Ask");
		
		// if white and black are the same (e.g. both Swing or both Console)
		// then do not create a new player instance, but reuse the already
		// created ones. Otherwise we would create two Swing frames or
		// mess up the console interface, etc.
//		if (whitePlayerOption == blackPlayerOption) {
//			playerBlack = playerWhite;
//		} else {
//			playerBlack = getPlayerHandler(blackPlayerOption, game, gameIdOnServer, gamePassword, "Black player");			
//		}

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
			//case PLAYER_OPTION_NETWORK: return new XmlRpcPlayerHandler(gameIdOnServer,gamePassword);
			case PLAYER_OPTION_HUMAN: return new Human(name);
			default: throw new IllegalArgumentException("Invalid player option:" + playerHandlerOption);
		}
	}
}