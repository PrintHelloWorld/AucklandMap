package articulation_points;

import java.util.Set;

import model.Node;

public class DepthNode {
	private Node node;
	private int depth;
	private int reachBack;
	
	public DepthNode(Node n){
		this.node = n;
		this.setDepth(Integer.MAX_VALUE);
		this.setReachBack(Integer.MAX_VALUE);
	}
	
	/** Getters and setters */
	public Set<Node> getNeighbours(){
		return (Set<Node>) node.getNeighbourNodes();
	}
	
	public Node getNode() {
		return node;
	}

	public void setNode(Node n) {
		this.node = n;
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
}