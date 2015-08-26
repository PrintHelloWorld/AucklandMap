package a_star;

import model.Node;
import model.Segment;

public class FringeNode implements Comparable<FringeNode> {

	private Node node;
	private FringeNode nodeFrom;
	private Segment segmentFrom;
	private double costToHere;
	private double estimatedCost;

	public FringeNode(Node node, FringeNode nodeFrom, Segment seg, double costToHere, double heuristic) {
		this.node = node;
		this.nodeFrom = nodeFrom;
		this.segmentFrom = seg;
		this.costToHere = costToHere;
		this.estimatedCost = costToHere + heuristic;
	}

	@Override
	public int compareTo(FringeNode n) {
		if (estimatedCost < n.getEstimatedCost()) {
			return -1;
		} else if (estimatedCost > n.getEstimatedCost()) {
			return 1;
		} else {
			return 0;
		}
	}

	/** getters and setters */
	public Node getNode() {
		return node;
	}

	public FringeNode getFrom() {
		return nodeFrom;
	}
	
	public Segment getSegmentFrom(){
		return segmentFrom;
	}

	public double getCostToHere() {
		return costToHere;
	}

	public double getEstimatedCost() {
		return estimatedCost;
	}
}
