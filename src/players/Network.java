package players;

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
		//
		if (aGameIdOnServer == null) {
			// create new game
			System.out.println("Creating new game");
			this.gameIdOnServer = createGame(gamePassword);
		} else {
			System.out.println("Joining game: " + aGameIdOnServer);
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

	@Override
	public Move getMove() {
		Move receivedMove = null;
		String lastMoveFromServerStr = null;

		/** loop until we receive a new move.
		 *
		 * We could also just return null when we receive no move
		 * or receive the one we sent, but as the game logic
		 * would ask again in 100 ms, I decided to block
		 * the call and implement a waiting time of 3000 ms.
		 * This greatly reduces network traffic, while still
		 * not slowing down the game too much.
		 */
		while(receivedMove == null) {
			// ask server if there are new messages
			lastMoveFromServerStr = "" + getLastMove();
	    	// instead of returning an empty string we return null
	    	if (lastMoveFromServerStr != null && lastMoveFromServerStr.trim().length() == 0) 
	    		lastMoveFromServerStr = null;
			// if no messages returned, return null
			if (lastMoveFromServerStr == null) {
				System.out.println("No moves received");
			
			/** 
			 * if we receive the move that we have just sent, we do not want
			 * to return it to the game logic.
			 */
			} else if (lastMoveStrSentToNetwork != null
					&& lastMoveStrSentToNetwork.equals(lastMoveFromServerStr)) {
				System.out.println("Received move is the one we sent");
//				receivedMove = parsePacket(lastMoveFromServerStr);
				return null;
			} else {
				receivedMove = parsePacket(lastMoveFromServerStr);
			}
			try {Thread.sleep(3000);} catch (InterruptedException e) {}
		}

		// set last received move
		this.lastMoveStrReceivedFromNetwork = lastMoveFromServerStr;
		currentMove = null;
		return receivedMove;
	}

	private Move parsePacket(String lastMoveFromServerStr) {
		Speak.say("inside parsePacket: " + lastMoveFromServerStr, true);
		Move move = null;
		String type = lastMoveFromServerStr.substring(0, 4);
		String content = lastMoveFromServerStr.substring(4, 
								lastMoveFromServerStr.length());
		switch (type) {
			case "MOVE":	move = Move.makeMoveFromString(content);
							break;
			case "PROM":	break; // pawn promotion choice
			case "MESS":	Speak.say("Chat message: " + content, true);
							break;
			default:		break;
		}
		return move;
	}
	
	@Override
	public void setCurrentMove(Move currentMove) {
		String moveStr = "" + currentMove;
		if (!moveStr.equals(lastMoveStrReceivedFromNetwork)) {
			// send our move to server
			sendMove(moveStr);
			lastMoveStrSentToNetwork = moveStr;
		} else {
			// the executed move is the one we have received from
			// the network, so no need to send it again to the server
		}
		this.currentMove = currentMove;
	}

	/**
	 * get the last move that is available on the server
	 *
	 * @param aGameIdOnServer - server game id
	 * @param aGamePassword - password for online game
	 * @return the last move as string
	 */
	@Override
	public Move getLastMove() {
//		System.out.println("get last move from server");
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
	    	System.out.println("got last move from server:" + message);
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
	public String sendMove(String message) {
		message = "MOVE" + message;
		System.out.println("sending move:" + message);
	    Object[] params = new Object[]{getGameIdOnServer(), getGamePassword(), message};
	    String result = null;
	    try {
			result = (String) xmlRpcClient.execute("sendMessage", params);
		} catch (XmlRpcException e) {
			throw new IllegalStateException(e);
		}
	    System.out.println("returned result:" + result);
		return result;
	}
	
	public static String getCurrentChannelId() {
		XmlRpcClient client;
		System.out.println("getting current channel id...");
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
		System.out.println("sending validation request for game and password");
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
	    Object[] params = new Object[]{aGamePassword};
	    String result;
	    System.out.println("sending createChannel request");
	    try {
	    	result = (String) xmlRpcClient.execute("createChannel", params);
		} catch (XmlRpcException e) {
			throw new IllegalStateException(e);
		}
	    System.out.println("executed method createChannel. gameID: " + result);
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
		return "Network: " + name;
	}

	@Override
	public void moveSuccessfullyExecuted(Move move) {
		// TODO Auto-generated method stub
		
	}
}

