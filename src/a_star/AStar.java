package a_star;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import model.Node;
import model.Segment;

public class AStar {

	// ordered by an estimated total cost to goal
	private PriorityQueue<FringeNode> fringe;

	// set of visited nodes
	private Set<Node> visited;

	// Returned path
	private List<Segment> path;

	// Distance from/to nodes
	private Node start;
	private Node goal;

	public AStar(Node start, Node goal) {
		this.start = start;
		this.goal = goal;
		fringe = new PriorityQueue<FringeNode>();
		visited = new HashSet<Node>();
	}

	public List<Segment> search() {
		// add start to fringe
		fringe.offer((new FringeNode(getStartNode(), null, null, 0,
				estimateFromToEnd(getStartNode()))));
		while (!fringe.isEmpty()) {
			// poll highest priority node from fringe
			FringeNode currentFringeNode = fringe.poll();
			// if node has been visited, do not check again
			if (!(visited.contains(currentFringeNode.getNode()))) {
				visited.add(currentFringeNode.getNode());
			}
			// goal node has been found.
			if (currentFringeNode.getNode().equals(getEndNode())) {
				generatePath(currentFringeNode);
				return getPath();
			}
			for (Segment s : currentFringeNode.getNode().getExitNeighbours()) {
				// read the node at the end of the segment, if it has
				// not been visited add it to the priority queue
				Node neighbour = s.getEnd();
				if (!(visited.contains(neighbour)) && !(fringe.contains(neighbour))) {
					fringe.offer((new FringeNode(neighbour, currentFringeNode, s,
							costToHere(currentFringeNode, s), estimateFromToEnd(neighbour))));
				}
			}
		}
		return null;
	}

	private double estimateFromToEnd(Node from) {
		return from.distanceTo(getEndNode());
	}

	private void generatePath(FringeNode n) {
		path = new ArrayList<Segment>();
		while (n.getFrom() != null) {
			Segment s = n.getSegmentFrom();
			path.add(s);
			n = n.getFrom();
		}
		setPath(path);
	}

	private double costToHere(FringeNode node, Segment s) {
		double totalCostToHere = node.getCostToHere();
		double actualLength = s.getLength();
		return totalCostToHere + actualLength;
	}

	/** getters and setters */
	public Node getStartNode() {
		return start;
	}

	public Node getEndNode() {
		return goal;
	}

	public List<Segment> getPath() {
		return path;
	}

	public void setPath(List<Segment> path) {
		this.path = path;
	}
}
