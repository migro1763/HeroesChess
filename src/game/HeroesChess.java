package game;

import gui.HeroesFrame;
import gui.TitleScreen;
import interfaces.Vals;

import java.io.IOException;

import players.Computer;
import players.Human;
import players.Network;
import players.Player;

public class HeroesChess implements Vals {
	
	String[] iPlayerHandlerOptions = new String[] {"Swing GUI", "AI Player", "Network Player"};
	
	public HeroesChess() {
		
		HeroesFrame frame = new HeroesFrame();
		TitleScreen titleScreen = new TitleScreen(frame);
		Thread titleThread = new Thread(titleScreen);
		titleThread.start();

		Game game = new Game();
		
		int numberOfPlayers = -1, playerColour = -1, networkSelect = -1;
		int choice;
		String gameIdOnServer = null;
		String gamePassword = null, name = "name";
		Player playerWhite = null, playerBlack = null, networkPlayer = null;
		
		while (gameIdOnServer == null) {
			do {
				choice = titleScreen.getChoice();
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {}
			} while (choice != 0 && choice != 4);
			
			// if quit, exit game
			if(choice == 4)
				System.exit(0);
			
			// DEBUG
			Speak.say("exit, choice from titlescreen: " + choice, true);
			Speak.say("numberOfPlayers: " + titleScreen.getPlayerSelect(), true);
			
			numberOfPlayers = titleScreen.getPlayerSelect() < 3 ? titleScreen.getPlayerSelect() : -1;
										
			/** 
			 * if numberOfPlayers = -1, that means cancel button was pressed.
			 * we jump to end of while, with gameIdOnServer still = null,
			 * repeating loop until numberOfPlayers is selected.
			 */	
			if(numberOfPlayers == -1)
				continue;						
										
			// DEBUG
			Speak.say("player colour: " + COLOUR_NAME[titleScreen.getColourSelect()-1], true);
			
			playerColour = titleScreen.getColourSelect() < 3 ? // player colour from title screen: 1 = white, 2 = black
					titleScreen.getColourSelect()-1 : -1; // convert colour to: 0 = white, 1 = black
					
			// same as when numberOfPlayers = -1
			if(playerColour == -1)
				continue;
			
			if(numberOfPlayers == 2) {
				// DEBUG
				Speak.say("network option: " + (titleScreen.getNetworkSelect() == 1 ? "Host" : "Guest"), true);
				
				networkSelect = titleScreen.getNetworkSelect() < 3 ? // network: 0 = host, 1 = guest
						titleScreen.getNetworkSelect()-1 : -1;
						
				// same as when numberOfPlayers = -1
				if(networkSelect == -1)
					continue;
				
				gamePassword = titleScreen.getText();
				
				if(networkSelect == 1) { // if client
					gameIdOnServer = Network.getCurrentChannelId();
					if(gameIdOnServer == " ")
						gameIdOnServer = null;
				} else {
					// if host, do nothing (leave gameIdOnServer = null)
				}
				
				try {
					networkPlayer = getPlayerHandler(PLAYER_OPTION_NETWORK, game, gameIdOnServer, gamePassword, name);
					gameIdOnServer = ((Network)networkPlayer).getGameIdOnServer();
				} catch (IOException e) {
					e.printStackTrace();
				}
					
			} else
				// if selected one player, exit game since AI not ready
				System.exit(0);
		}
		
		// DEBUG
		Speak.say("exit while-loop, gameIdOnServer: " + gameIdOnServer, true);
		
		// send interrupt signal to title screen thread, disposing all its frames
		titleThread.interrupt();
		
		// DEBUG
		Speak.say("playerColour: " + playerColour + ", whereas COLOR_WHITE = " + COLOR_WHITE, true);
		
		if(playerColour == COLOR_WHITE) {
			try {
				playerWhite = getPlayerHandler(PLAYER_OPTION_HUMAN, game, gameIdOnServer, gamePassword, name);
				playerBlack = networkPlayer;
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				playerBlack = getPlayerHandler(PLAYER_OPTION_HUMAN, game, gameIdOnServer, gamePassword, name);
				playerWhite = networkPlayer;
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}

		// then we attach the clients/players to the game
		game.setPlayer(COLOR_WHITE, playerWhite);
		game.setPlayer(COLOR_BLACK, playerBlack);

		// in the end we start the game
		new Thread(game).start();
	}
	
	private Player getPlayerHandler(int playerHandlerOption,
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