package game;

import gui.GuiPiece;

public class BinaryTree {

	Node root;

	public void addNode(GuiPiece piece) {
		Node newNode = new Node(piece);

		if(root == null) {
			root = newNode;

		} else {
			Node focusNode = root;
			Node parent;

			while(true) {
				parent = focusNode;

				if(piece.getPiece().getId() < focusNode.id) {
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
	
	public Node findNode(GuiPiece piece) {
		return findNode(piece.getPiece().getId());
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
		private GuiPiece guiPiece;

		Node leftChild, rightChild;

		public Node(GuiPiece guiPiece) {
			this.id = guiPiece.getPiece().getId();
			this.guiPiece = guiPiece;
		}

		public GuiPiece getGuiPiece() {
			return guiPiece;
		}

		@Override
		public String toString() {
			return ("Node id: " + id + " piece: " + this.getGuiPiece());
		}
	}
}

