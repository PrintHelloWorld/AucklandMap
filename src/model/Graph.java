package model;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import a_star.AStar;
import articulation_points.ArtPointSearch;
import util.Location;
import util.Parser;
import Map.Mapper;

/**
 * This represents the data structure storing all the roads, nodes, and
 * segments, as well as some information on which nodes and segments should be
 * highlighted.
 * 
 * @author Tony Butler-Yeoman
 */
public class Graph {
	// map node IDs to Nodes.
	private Map<Integer, Node> nodes = new HashMap<>();
	// map road IDs to Roads.
	private Map<Integer, Road> roads;
	// just some collection of Segments.
	Collection<Segment> segments;
	Node highlightedNode;
	Collection<Road> highlightedRoads = new HashSet<>();

	// selected aStar route
	private List<Segment> selectedDistancePath = new ArrayList<Segment>();
	// art points for all nodes
	private Set<Node> artPoints;
	// art points for current node;
	private Set<Node> currentArtPoints;
	private Node start;
	private Node goal;
	private boolean displayArtPoints;

	public Graph(File nodes, File roads, File segments, File polygons) {
		this.setNodes(Parser.parseNodes(nodes, this));
		this.setRoads(Parser.parseRoads(roads, this));
		this.segments = Parser.parseSegments(segments, this);
		ArtPointSearch artPtSearch = new ArtPointSearch(getNodes());
		artPoints = artPtSearch.getArticulations();
	}

	public void draw(Graphics g, Dimension screen, Location origin, double scale) {
		// a compatibility wart on swing is that it has to give out Graphics
		// objects, but Graphics2D objects are nicer to work with. Luckily
		// they're a subclass, and swing always gives them out anyway, so we can
		// just do this.
		Graphics2D g2 = (Graphics2D) g;

		// draw all the segments.
		for (Segment s : segments) {
			if (s.road.oneway != 0) {
				g2.setColor(Mapper.ONEWAY_SEGMENT_COLOUR);
				s.draw(g2, origin, scale);
			} else {
				g2.setColor(Mapper.SEGMENT_COLOUR);
				s.draw(g2, origin, scale);
			}
		}

		// draw the segments of all highlighted roads.
		g2.setColor(Mapper.HIGHLIGHT_COLOUR);
		g2.setStroke(new BasicStroke(3));
		for (Road road : highlightedRoads) {
			for (Segment seg : road.components) {
				seg.draw(g2, origin, scale);
			}
		}

		// draw all the nodes and articulation nodes
		for (Node n : getNodes().values())
			if (artPoints.contains(n) && displayArtPoints) {
				g2.setColor(Mapper.ART_POINTS_COLOUR);
				n.draw(g2, screen, origin, scale);
			} else {
				g2.setColor(Mapper.NODE_COLOUR);
				n.draw(g2, screen, origin, scale);
			}

		// draw the highlighted node, if it exists.
		if (highlightedNode != null) {
			g2.setColor(Mapper.HIGHLIGHT_COLOUR);
			highlightedNode.draw(g2, screen, origin, scale);
			if(displayArtPoints && currentArtPoints != null){
				for(Node n : currentArtPoints){
					g2.setColor(Mapper.ART_POINTS_COMPONENT_COLOUR);
					n.draw(g2, screen, origin, scale);
				}
			}
		}

		// highlight start node of AStar
		if (start != null) {
			g2.setColor(Mapper.ASTAR_ROUTE);
			start.draw(g2, screen, origin, scale);
		}

		// highlight end node of AStar
		if (goal != null) {
			g2.setColor(Mapper.ASTAR_ROUTE);
			goal.draw(g2, screen, origin, scale);
		}

		// draw path of AStar
		if (selectedDistancePath != null) {
			for (Segment seg : selectedDistancePath) {
				g2.setColor(Mapper.ASTAR_ROUTE);
				seg.draw(g2, origin, scale);
			}
		}
	}

	/** A STAR */
	public void pathSearch() {
		AStar aStar = new AStar(getStartNode(), getEndNode());
		setDistancePath(aStar.search());
	}

	public void setHighlight(Node node) {
		this.highlightedNode = node;
	}
	
	public Node getHiglight(){
		return highlightedNode;
	}

	public void setHighlight(Collection<Road> roads) {
		this.highlightedRoads = roads;
	}

	public Map<Integer, Node> getNodes() {
		return nodes;
	}

	public void setNodes(Map<Integer, Node> nodes) {
		this.nodes = nodes;
	}

	public Map<Integer, Road> getRoads() {
		return roads;
	}

	public void setRoads(Map<Integer, Road> roads) {
		this.roads = roads;
	}

	public List<Segment> getDistancePath() {
		return selectedDistancePath;
	}

	public void setDistancePath(List<Segment> path) {
		this.selectedDistancePath = path;
	}

	public Node getStartNode() {
		return start;
	}

	public void setStartNode(Node start) {
		this.start = start;
	}

	public Node getEndNode() {
		return goal;
	}

	public void setEndNode(Node goal) {
		this.goal = goal;
	}
	
	public void setCurrentArtPoints(Set<Node> artPoints){
		this.currentArtPoints = artPoints;
	}

	public void setArtPoints(Set<Node> artPoints) {
		this.artPoints = artPoints;
	}

	public void displayArtPoints(boolean b) {
		displayArtPoints = b;
	}
}

// code for COMP261 assignments