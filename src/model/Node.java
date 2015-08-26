package model;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import util.Location;
import Map.Mapper;

/**
 * Node represents an intersection in the road graph. It stores its ID and its
 * location, as well as all the segments that it connects to. It knows how to
 * draw itself, and has an informative toString method.
 * 
 * @author Tony Butler-Yeoman
 */
public class Node {

	public final int nodeID;
	public final Location location;
	public final Collection<Segment> segments;

	public final Collection<Node> neighbourNodes;
	public final Collection<Segment> exitNeighbours;
	public final Collection<Segment> enterNeighbours;
	
	//articulation point
	private int depth = Integer.MAX_VALUE;

	public Node(int nodeID, double lat, double lon) {
		this.nodeID = nodeID;
		this.location = Location.newFromLatLon(lat, lon);
		this.segments = new HashSet<Segment>();
		this.exitNeighbours = new HashSet<Segment>();
		this.enterNeighbours = new HashSet<Segment>();
		this.neighbourNodes = new HashSet<Node>();
	}

	public void draw(Graphics g, Dimension area, Location origin, double scale) {
		Point p = location.asPoint(origin, scale);

		// for efficiency, don't render nodes that are off-screen.
		if (p.x < 0 || p.x > area.width || p.y < 0 || p.y > area.height)
			return;

		int size = (int) (Mapper.NODE_GRADIENT * Math.log(scale) + Mapper.NODE_INTERCEPT);
		g.fillRect(p.x - size / 2, p.y - size / 2, size, size);
	}

	public String toString() {
		Set<String> edges = new HashSet<String>();
		for (Segment s : segments) {
			if (!edges.contains(s.road.name))
				edges.add(s.road.name);
		}

		String str = "ID: " + nodeID + "  loc: " + location + "\nroads: ";
		for (String e : edges) {
			str += e + ", ";
		}
		return str.substring(0, str.length() - 2);
	}
	
	public void addNeighbourNode(Node n){
		neighbourNodes.add(n);
	}

	public void addEnterSegment(Segment s) {
		enterNeighbours.add(s);
	}

	public void addExitSegment(Segment s) {
		exitNeighbours.add(s);
	}
	
	public void addSegment(Segment seg) {
		segments.add(seg);
	}

	public Collection<Segment> getEnterNeighbours() {
		return enterNeighbours;
	}

	public Collection<Segment> getExitNeighbours() {
		return exitNeighbours;
	}

	public Collection<Node> getNeighbourNodes() {
		return neighbourNodes;
	}

	public double distanceTo(Node goal) {
		if(goal == null)
			return Double.MAX_VALUE;
		else if(this == goal)
			return 0;
		else return this.location.distance(goal.location);
	}
	
	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}
}