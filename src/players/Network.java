package players;

import game.Game;
import game.Move;
import game.Speak;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;

public class Network extends Player {

    /** equivalent to channelId. */
	private String gameIdOnServer;
	/** password for the channel on the server. */
	private String gamePassword = null;
	/** last received message string from server */
	private String lastMoveStrReceivedFromNetwork = null;
	/** last message sent to server */
	private String lastMoveStrSentToNetwork = null;
	/** connection to server */
	private XmlRpcClient xmlRpcClient;
	/** URL of the XML-RPC services */
//	private static final String XML_RPC_HOST_URL = "http://hchessgs.appspot.com/xml";
	private static final String XML_RPC_HOST_URL = "http://localhost:8080/";

	/**
	 * create new network game or join existing one.
	 * @param aGameIdOnServer - game id to join. If null then a new game is created
	 * @param aGamePassword - password for joining the online game or password for
	 *                        the new game to be created
	 */
	public Network(String aGameIdOnServer, String aGamePassword, String name) {
		super(name);
		setDragPiecesEnabled(false);
		
		// set up connection to server
		try{
			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setServerURL(new URL(XML_RPC_HOST_URL));
			xmlRpcClient = new XmlRpcClient();
			xmlRpcClient.setTransportFactory(new XmlRpcCommonsTransportFactory(xmlRpcClient));
			xmlRpcClient.setConfig(config);
		} catch(MalformedURLException mue) {
			throw new IllegalStateException("Invalid XML-RPC-Server URL:" + XML_RPC_HOST_URL);
		}
		
		this.gamePassword = aGamePassword;
		createOrJoinGame(aGameIdOnServer);
	}
	
	public void createOrJoinGame(String aGameIdOnServer) {
		// do we need to create a new game or join existing?
		if (aGameIdOnServer == null) {
			// create new game
			Speak.tell("Network.createOrJoinGame: HOST >>> Creating new game", true);
			this.gameIdOnServer = createGame(gamePassword);
		} else {
			Speak.tell("Network.createOrJoinGame: GUEST >>> Joining game: " + aGameIdOnServer, true);
			// verify received parameters
			if (isGameValid(aGameIdOnServer, gamePassword)) {
				this.gameIdOnServer = aGameIdOnServer;
			} else {
				throw new IllegalStateException("GameId: " + aGameIdOnServer
						+ " and/or password: >" + gamePassword+"< are invalid");
			}
		}		
	}
	
	/**
	 * Network player always has dragpieces disabled
	 */
	@Override
	public void setDragPiecesEnabled(boolean state) {
		this.dragPiecesEnabled = false;
	}

	/** Loop until we receive a new move.
	 *
	 * We could also just return null when we receive no move
	 * or receive the one we sent, but as the game logic
	 * would ask again in 100 ms, I decided to block
	 * the call and implement a waiting time of 3000 ms.
	 * This greatly reduces network traffic, while still
	 * not slowing down the game too much.
	 */
	@Override
	public Move getMove() {
		Move receivedMove = null;
		String lastMoveFromServerStr = null;

		while(receivedMove == null) {
			// wait 3 seconds before asking server for new message = less network traffic
			Game.threadPause(3000);
			// ask server if there are new messages
			lastMoveFromServerStr = "" + getLastMove();
	    	// instead of returning an empty string we return null
	    	if (lastMoveFromServerStr != null && (lastMoveFromServerStr.trim().length() == 0 || lastMoveFromServerStr.equals("null")))
	    		lastMoveFromServerStr = null;
	    	
			// if no messages returned, return null
			if(lastMoveFromServerStr == null ) {
				Speak.tell("Network.getMove: No moves received", true);
				return null;
			
			/** 
			 * if we receive the move that we have just sent, we do not want
			 * to return it to the game logic.
			 */
			} else if (lastMoveStrSentToNetwork != null
					&& lastMoveStrSentToNetwork.equals(lastMoveFromServerStr)) {	
				Speak.say("Network.getMove: Received move is the one we sent: " + lastMoveFromServerStr, true);
				break;
			} else {
				Speak.tell("Network.getMove: Last move received from server: " + lastMoveFromServerStr, true);
				if(lastMoveFromServerStr != null)
					receivedMove = Move.makeMoveFromString(lastMoveFromServerStr);
			}
		}
		
		// set last received move
		this.lastMoveStrReceivedFromNetwork = lastMoveFromServerStr;
		currentMove = null;
		return receivedMove;
	}

	private Move parsePacket(String lastMoveFromServerStr) {
		if(lastMoveFromServerStr == null)
			return null;
		int serverStrLen = lastMoveFromServerStr.length();
		Move move = null;
		String type = lastMoveFromServerStr.substring(0, 4);
		String content = lastMoveFromServerStr.substring(4, serverStrLen);
		switch (type) {
			case "MOVE":	move = Move.makeMoveFromString(content);
							break;
			case "PROM":	move = Move.makeMoveFromString(content);
							setPromotedTo(Integer.parseInt(content.substring(4, 5)));
							break;
			case "MESS":	Speak.say("Chat message: " + content, true);
							break;
			default:		break;
		}
		Speak.say("Network.parsePacket: Move returning from parsePacket: " + move, true);
		return move;
	}
	
	/**	Set currentMove to the move parameter.
	 * Sends this move to server as last move.
	 *
	 * @param currentMove
	 */
	@Override
	public void setCurrentMove(Move currentMove) {
		String moveStr = "" + currentMove;
		Speak.tell("Network.setCurrentMove: moveStr: " + moveStr, true);
		Speak.tell("Network.setCurrentMove: lastMoveStrReceivedFromNetwork: " + lastMoveStrReceivedFromNetwork, true);
		if (!moveStr.equals(lastMoveStrReceivedFromNetwork)) {
			// send our move to server
			sendMove(moveStr, 0);
			lastMoveStrSentToNetwork = moveStr;
		} else {
			Speak.tell("!!! ------> aaaaah! move equals last move received from server!", true);
		}
		this.currentMove = currentMove;
	}
	
	/** Set currentMove for pawn promotion.
	 * Param promoType int) represents type of piece player promoted pawn to.
	 * Sends this to server as combination of move and promotion type.
	 * 
	 * @param currentMove
	 * @param promoType
	 */
	public void setCurrentMove(Move currentMove, int promoType) {
		Speak.tell("Network.setCurrentMove: pawn promotion, type = " + promoType, true);
		String moveStr = "" + currentMove + "" + promoType;
		if (!moveStr.equals(lastMoveStrReceivedFromNetwork)) {
			// send our move to server
			sendMove(moveStr, 1);
			lastMoveStrSentToNetwork = moveStr;
		} else {
			// the executed move is the one we have received from
			// the network, so no need to send it again to the server
		}
		this.currentMove = currentMove;
	}
	
	/** Send chat message to remote opponent
	 * 
	 * @param message
	 */
	public void chat(String message) {
		if(message != null)
			sendMove(message, 2);
	}

	/** Get the last move that is available on the server
	 * 
	 * @param aGameIdOnServer - server game id
	 * @param aGamePassword - password for online game
	 * @return the last move as string
	 */
	@Override
	public Move getLastMove() {
	    Object[] params = new Object[]{getGameIdOnServer()};
	    String message = null;
	    try {
	    	message = (String) xmlRpcClient.execute("getLastMessage", params);
	    	// instead of returning an empty string we return null
	    	if (message != null && message.trim().length() == 0) 
	    		message = null;
		} catch (XmlRpcException e) {
			throw new IllegalStateException(e);
		}	    
	    if(message != null) {
	    	Speak.tell("Network.getLastMove: Got last move from server: " + message, true);
	    	return parsePacket(message);
	    } else
	    	return null;
	}

	/**
	 * Send the move message to the server.
	 * @param aGameIdOnServer - server game id
	 * @param aGamePassword - password for online game
	 * @param message - the move as a string to be sent
	 * @return id that the server assigned to the new move message
	 */
	public String sendMove(String message, int messageType) {
		Speak.say("Network.sendMove()", true);
		String type = "Move";
		switch (messageType) {
			case 0:		message = "MOVE" + message;
						break;
			case 1:		message = "PROM" + message;
						type = "Promotion";
						break;
			case 2: 	message = "MESS" + message;
						type = "Message";
						break;
			default:	return null;
		}	
		Speak.tell("Network.sendMove: Sending " + type + ": " + message, true);
	    Object[] params = new Object[]{getGameIdOnServer(), getGamePassword(), message};
	    String result = null;
	    try {
			result = (String) xmlRpcClient.execute("sendMessage", params);
		} catch (XmlRpcException e) {
			throw new IllegalStateException(e);
		}
	    Speak.say("returned result:" + result, true);
		return result;
	}
	
	public static String getCurrentChannelId() {
		Speak.say("Network.getCurrentChannelId()", true);
		XmlRpcClient client;
		Speak.say("Network.getCurrentChannelId: Getting current channel id...", true);
		Object[] params = new Object[]{};
		String result = null;
		try{
			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setServerURL(new URL(XML_RPC_HOST_URL));
			client = new XmlRpcClient();
			client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
			client.setConfig(config);
		} catch(MalformedURLException mue) {
			throw new IllegalStateException("Invalid XML-RPC-Server URL:" + XML_RPC_HOST_URL);
		}
	    try {
			result = (String) client.execute("getLastChannel", params);
		} catch (XmlRpcException e) {
			throw new IllegalStateException(e);
		}
		return result;
	}

	/**
	 * checks the validity of the provided game id and password
	 * @param aGameIdOnServer - server game id
	 * @param aGamePassword - game password
	 * @return true if parameters are valid
	 */
	private boolean isGameValid(String aGameIdOnServer, String aGamePassword) {
		Speak.say("Network.isGameValid()", true);
		Speak.say("Network.isGameValid: Sending validation request for game and password", true);
	    Object[] params = new Object[]{aGameIdOnServer, aGamePassword};
	    String result = null;
	    try {
			result = (String) xmlRpcClient.execute("isValid", params);
		} catch (XmlRpcException e) {
			throw new IllegalStateException(e);
		}
		return Boolean.parseBoolean(result);
	}

	/**
	 * Create a new game with the specified password
	 * @param aGamePassword - password for the new game
	 * @return the game id that the game server assigned to the new game
	 */
	private String createGame(String aGamePassword) {
		Speak.say("Network.createGame()", true);
	    Object[] params = new Object[]{aGamePassword};
	    String result;
	    Speak.say("Network.createGame: Sending createChannel request", true);
	    try {
	    	result = (String) xmlRpcClient.execute("createChannel", params);
		} catch (XmlRpcException e) {
			throw new IllegalStateException(e);
		}
	    Speak.say("Network.createGame: Executed method createChannel. gameID: " + result, true);
		return result;
	}

	public String getGameIdOnServer(){
		return gameIdOnServer;
	}

	public String getGamePassword() {
		return gamePassword;
	}
	
	@Override
	public String toString() {
		return "NET: " + name;
	}

	@Override
	public void moveSuccessfullyExecuted(Move move) {
		// TODO Auto-generated method stub		
	}
}

