package articulation_points;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import model.Node;

public class ArtPointSearch {

	boolean iterative = true;

	private Set<Node> articulationPoints;
	private List<DepthNode> unvisitedNodes;
	private Map<Node, DepthNode> depthNodeMap;

	public ArtPointSearch(Map<Integer, Node> nodeMap) {
		this.articulationPoints = new HashSet<Node>();
		this.unvisitedNodes = new ArrayList<DepthNode>();
		this.depthNodeMap = new HashMap<Node, DepthNode>();

		// construct the data structures for every node
		for (Node n : nodeMap.values()) {
			DepthNode artPoint = new DepthNode(n);
			// set each node with its own fresh art point
			depthNodeMap.put(n, artPoint);
			// list of unvisited nodes
			unvisitedNodes.add(artPoint);
			if (iterative)
				n.setDepth(Integer.MAX_VALUE);
		}
		// find all art points
		search();
	}

	public ArtPointSearch(Node node) {
		this.articulationPoints = new HashSet<Node>();
		this.unvisitedNodes = new ArrayList<DepthNode>();
		this.depthNodeMap = new HashMap<Node, DepthNode>();
		Set<Node> graphComponent = new HashSet<Node>();
		traverse(node, graphComponent);
		for (Node n : graphComponent) {
			DepthNode artPoint = new DepthNode(n);
			depthNodeMap.put(n, artPoint);
			unvisitedNodes.add(artPoint);
			if (iterative)
				n.setDepth(Integer.MAX_VALUE);
		}
		// find components art points
		search();
	}

	public void traverse(Node n, Set<Node> visited) {
		visited.add(n);
		for (Node neighbour : n.getNeighbourNodes()) {
			if (!visited.contains(neighbour)) {
				traverse(neighbour, visited);
			}
		}
	}

	private void search() {
		// while there are unvisited nodes in the list
		while (!unvisitedNodes.isEmpty()) {
			// start at first node in list of unvisited
			DepthNode start = unvisitedNodes.remove(0);
			int rootChildren = 0;
			// recurse subtree of root
			for (DepthNode neighbour : neighboursOf(start)) {
				// if there is still unvisited nodes after first traversal or
				// rootChildren > 1 then root is art.pt
				if (neighbour.getDepth() == Integer.MAX_VALUE) {
					if (iterative) {
						iterArtPts(neighbour.getNode(), start.getNode());
					} else {
						recArtPts(neighbour, 1, start);
					}
					rootChildren++;
				}
			}
			if (rootChildren > 1) {
				// if the root has more than one child add it as art
				articulationPoints.add(start.getNode());
			}
		}
	}

	private int recArtPts(DepthNode node, int depth, DepthNode fromNode) {
		unvisitedNodes.remove(node); // record that this node was visited
		node.setDepth(depth);
		node.setReachBack(depth);
		// recurse neighbours excluding where the node it came from
		Set<DepthNode> neighbours = neighboursOf(node);

		// remove fromNode from neighbours so it can't go back up the graph.
		neighbours.remove(fromNode);

		for (DepthNode neigh : neighbours) {
			// if it has already been visited
			if (neigh.getDepth() < Integer.MAX_VALUE) {
				node.setReachBack(Math.min(neigh.getDepth(),
						node.getReachBack()));
				// else isn't visited
			} else {
				int childReach = recArtPts(neigh, depth + 1, node);
				node.setReachBack(Math.min(childReach, node.getReachBack()));
				/**
				 * once visited if child has no reach higher than current node
				 * it is articulation point
				 */
				if (childReach >= node.getDepth()) {
					articulationPoints.add(node.getNode());
				}
			}
		}
		return node.getReachBack();
	}

	private void iterArtPts(Node firstNode, Node root) {
		// initialise stack and push first node
		Stack<IterativeNode> stack = new Stack<IterativeNode>();
		stack.push(new IterativeNode(firstNode, 1, new IterativeNode(root, 0,
				null)));
		while (!stack.isEmpty()) {
			// peek at the top element, get its node and mark it as visited
			IterativeNode elem = stack.peek();
			Node node = elem.getNode();
			unvisitedNodes.remove(depthNodeMap.get(node));
			// if this node has not been visited
			if (elem.getChildren() == null) {
				// save the nodes depth and reachback
				node.setDepth(elem.getDepth());
				elem.setReachBack(elem.getDepth());
				// create the nodes children as a new linkedList
				elem.setChildren(new LinkedList<Node>());
				for (Node neigh : node.getNeighbourNodes()) {
					if (neigh != elem.getParent().getNode()) {
						elem.addChild(neigh);
					}
				}
			}
			// if the element has been visited and has children
			else if (!elem.getChildren().isEmpty()) {
				// poll child
				Node child = elem.getChildren().poll();
				// if the child has been visited, update its reachback
				if (child.getDepth() < Integer.MAX_VALUE) {
					elem.setReachBack(Math.min(elem.getReachBack(),
							child.getDepth()));
				}
				// otherwise put it on the stack with depth one greater than its
				// parent
				else {
					stack.push(new IterativeNode(child, node.getDepth() + 1,
							elem));
				}
			}
			// if the node has been visited but has ZERO children
			else {
				/**
				 * if this isnt the first node and this node has a reachback
				 * less than its parents depth and this nodes parent is not the
				 * root then this nodes parent is an articulation point
				 */
				if (node != firstNode) {
					if (elem.getReachBack() >= elem.getParent().getDepth()
							&& elem.getParent() != null) {
						articulationPoints.add(elem.getParent().getNode());
					}
					elem.getParent().setReachBack(
							Math.min(elem.getParent().getReachBack(),
									elem.getReachBack()));
				}
				// remove the element from the stack
				stack.pop();
			}
		}
	}

	private Set<DepthNode> neighboursOf(DepthNode n) {
		// get all neighbour nodes current node
		Set<Node> neighbourNodes = n.getNeighbours();
		// create a set of depth nodes
		Set<DepthNode> neighbourDepthNodes = new HashSet<DepthNode>();
		// Iterate set of neighbour nodes getting the depth nodes
		for (Node node : neighbourNodes) {
			neighbourDepthNodes.add(depthNodeMap.get(node));
		}
		// return a set of neighbour depth nodes
		return neighbourDepthNodes;
	}

	public Set<Node> getArticulations() {
		return articulationPoints;
	}
}
