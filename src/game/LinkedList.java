package game;

public class LinkedList {

	Node root;
	private int turn;
	
	public LinkedList() {
		turn = 1;
	}

	public void addNode(Move move) {
		Node newNode = new Node(move, turn);

		if(root == null) {
			root = newNode;
			root.parent = root.child = null;

		} else {
			Node focusNode = root;
			Node parent;

			while(true) {
				parent = focusNode;
				focusNode = parent.child;

				if(focusNode == null) {
					parent.child = newNode;
					parent.child.parent = parent;
					return;
				} else {
					focusNode = focusNode.child;
				}
			}
		}
	}

	public Node findNode(Move move, int turn) {
		Node focusNode = root;
		// if turn parameter is less than zero, ignore turn
		int turnToFind = turn >= 0 ? this.turn : turn;

		while(turnToFind != turn && focusNode.move.equals(move)) {
			focusNode = focusNode.child;

			if(focusNode == null)
				return null;
		}		
		return focusNode;
	}
	
	// if only searching for a move, ignore turn
	public Node findNode(Move move) {
		return findNode(move, -1);
	}

	public class Node {
		private int turn;
		private Move move;

		Node parent, child;

		public Node(Move move, int turn) {
			this.move = move;
			this.turn = turn; 
		}

		public Move getMove() {
			return move;
		}

		public int getTurn() {
			return turn;
		}
		
		public void setTurn(int turn) {
			this.turn = turn;
		}

		@Override
		public String toString() {
			return ("Node = " + turn + ": " + move);
		}
	}
}


