package model;

import java.awt.Graphics;
import java.awt.Point;

import util.Location;

/**
 * A Segment is the most interesting class making up our graph, and represents
 * an edge between two Nodes. It knows the Road it belongs to as well as the
 * Nodes it joins, and contains a series of Locations that make up the length of
 * the Segment and can be used to render it.
 * 
 * @author Tony Butler-Yeoman
 */
public class Segment {

	public final Road road;
	public final Node start, end;
	public final double length;
	public final Location[] points;

	public Segment(Graph graph, int roadID, double length, int node1ID,
			int node2ID, double[] coords) {

		this.road = graph.getRoads().get(roadID);
		this.start = graph.getNodes().get(node1ID);
		this.end = graph.getNodes().get(node2ID);
		this.length = length;

		points = new Location[coords.length / 2];
		for (int i = 0; i < points.length; i++) {
			points[i] = Location
					.newFromLatLon(coords[2 * i], coords[2 * i + 1]);
		}

		this.road.addSegment(this);
		this.start.addSegment(this);
		this.end.addSegment(this);
		this.start.addNeighbourNode(end);
		this.end.addNeighbourNode(start);
		this.start.exitNeighbours.add(this);
		this.end.enterNeighbours.add(this);

		if (road.oneway == 0) {
			Segment reverse = this.reverseWay();
			this.end.addExitSegment(reverse);
			this.start.addEnterSegment(reverse);
		}
	}

	public Segment(Road road, double length, Node end, Node start,
			Location[] points) {
		this.road = road;
		this.start = end;
		this.end = start;
		this.length = length;
		this.points = points;
	}

	public Segment reverseWay() {
		Segment seg = new Segment(road, length, end, start, points);
		return seg;
	}

	public void draw(Graphics g, Location origin, double scale) {
		for (int i = 1; i < points.length; i++) {
			Point p = points[i - 1].asPoint(origin, scale);
			Point q = points[i].asPoint(origin, scale);
			g.drawLine(p.x, p.y, q.x, q.y);
		}
	}
	
	@Override
	public String toString() {
		return "Segment : roadID=" + road.roadID + ", length="
				+ length + ", startNode=" + start + ", endNode=" + end;
	}

	public Node getStart() {
		return start;
	}

	public Node getEnd() {
		return end;
	}

	public double getLength() {
		return length;
	}
}