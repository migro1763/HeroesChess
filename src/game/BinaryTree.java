package game;

import gui.PieceGui;

public class BinaryTree {

	Node root;

	public void addNode(PieceGui guiPiece) {
		Node newNode = new Node(guiPiece);

		if(root == null) {
			root = newNode;

		} else {
			Node focusNode = root;
			Node parent;

			while(true) {
				parent = focusNode;

				if(guiPiece.getId() < focusNode.id) {
					focusNode = focusNode.leftChild;

					if(focusNode == null) {
						parent.leftChild = newNode;
						return;
					}
				} else {
					focusNode = focusNode.rightChild;

					if(focusNode == null) {
						parent.rightChild = newNode;
						return;
					}
				}
			}
		}
	}

	public Node findNode(int id) {
		Node focusNode = root;

		while(focusNode.id != id) {

			if(id < focusNode.id) {
				focusNode = focusNode.leftChild;

			} else {
				focusNode = focusNode.rightChild;
			}

			if(focusNode == null)
				return null;
		}
		return focusNode;
	}
	
	public Node findNode(PieceGui piece) {
		return findNode(piece.getId());
	}

	public void inOrderTraverseTree(Node focusNode) {

		if(focusNode != null) {
			inOrderTraverseTree(focusNode.leftChild);

			System.out.println(focusNode);

			inOrderTraverseTree(focusNode.rightChild);
		}
	}

	public class Node {
		int id;
		private PieceGui guiPiece;

		Node leftChild, rightChild;

		public Node(PieceGui guiPiece) {
			this.id = guiPiece.getId();
			this.guiPiece = guiPiece;
		}

		public PieceGui getGuiPiece() {
			return guiPiece;
		}

		@Override
		public String toString() {
			return ("Node id: " + id + " piece: " + this.getGuiPiece());
		}
	}
}

