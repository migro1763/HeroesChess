package network;

import game.Move;

public class NetworkProtocol {

	public NetworkProtocol() {
		
	}
	
	public Object parse(String packet) {
		String content;
		String cmd = packet.substring(0, 3); // first 4 chars constitute the command
		content = packet.substring(4, packet.length());
		
		switch(cmd) {
			case "move": return getMove(content);
			case "prom": return getPromo(content);
			case "enpa": return getEnPassant(content);
			case "mess": return getMessage(content);
			default:
		}
		return null;
	}
	
	private Move getMove(String content) {
		Move move = new Move();
		return move;
	}
	
	private char getPromo(String content) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	private Move getEnPassant(String content) {
		Move move = new Move();
		return move;
	}

	private String getMessage(String content) {
		// TODO Auto-generated method stub
		return null;
	}
}
