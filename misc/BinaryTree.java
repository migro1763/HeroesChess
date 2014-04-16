package game;

import gui.PieceGui;

public class BinaryTree {

	Node root;

	public void addNode(Object object) {
		Node newNode = new Node(object);

		if(root == null) {
			root = newNode;

		} else {
			Node focusNode = root;
			Node parent;

			while(true) {
				parent = focusNode;

				if(object.getId() < focusNode.id) {
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
	
	public Node findNode(Object object) {
		return findNode(object.getId());
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
		private Object object;

		Node leftChild, rightChild;

		public Node(Object object) {
			this.id = object.getId();
			this.object = object;
		}

		public Object getObject() {
			return object;
		}

		@Override
		public String toString() {
			return ("Node id: " + id + " piece: " + object);
		}
	}
}

