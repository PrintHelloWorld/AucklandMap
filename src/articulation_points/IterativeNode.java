package articulation_points;

import java.util.Queue;

import model.Node;

public class IterativeNode {
	private Node node;
	private IterativeNode parent;
	private int depth;
	private int reachBack;
	private Queue<Node> children;

	public IterativeNode(Node node, int depth, IterativeNode parent) {
		this.setNode(node);
		this.setDepth(depth);
		this.setParent(parent);
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public int getReachBack() {
		return reachBack;
	}

	public void setReachBack(int reachBack) {
		this.reachBack = reachBack;
	}

	public Queue<Node> getChildren() {
		return children;
	}

	public void setChildren(Queue<Node> children) {
		this.children = children;
	}

	public void addChild(Node n) {
		children.add(n);
	}

	public IterativeNode getParent() {
		return parent;
	}

	public void setParent(IterativeNode parent) {
		this.parent = parent;
	}
}
