package Map;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import articulation_points.ArtPointSearch;
import util.GUI;
import util.Location;
import util.Trie;
import model.Graph;
import model.Node;
import model.Road;
import model.Segment;

/**
 * This is the main class for the mapping program. It extends the GUI abstract
 * class and implements all the methods necessary, as well as having a main
 * function.
 * 
 * @author Tony Butler-Yeoman
 */
public class Mapper extends GUI {
	public static final Color NODE_COLOUR = new Color(77, 113, 255);
	public static final Color SEGMENT_COLOUR = new Color(130, 130, 130);
	public static final Color ONEWAY_SEGMENT_COLOUR = Color.RED;
	public static final Color HIGHLIGHT_COLOUR = new Color(255, 219, 77);
	public static final Color ART_POINTS_COLOUR = Color.MAGENTA;
	public static final Color ART_POINTS_COMPONENT_COLOUR = Color.PINK;
	public static final Color ASTAR_ROUTE = Color.GREEN;

	// these two constants define the size of the node squares at different zoom
	// levels; the equation used is node size = NODE_INTERCEPT + NODE_GRADIENT *
	// log(scale)
	public static final int NODE_INTERCEPT = 1;
	public static final double NODE_GRADIENT = 0.8;

	// defines how much you move per button press, and is dependent on scale.
	public static final double MOVE_AMOUNT = 100;
	// defines how much you zoom in/out per button press, and the maximum and
	// minimum zoom levels.
	public static final double ZOOM_FACTOR = 1.3;
	public static final double MIN_ZOOM = 1, MAX_ZOOM = 200;

	// how far away from a node you can click before it isn't counted.
	public static final double MAX_CLICKED_DISTANCE = 0.15;

	// these two define the 'view' of the program, ie. where you're looking and
	// how zoomed in you are.
	private Location origin;
	private double scale;

	// our data structures.
	private Graph graph;
	private Trie trie;

	// next click will initialise start/end node
	private boolean selectStart;
	private boolean selectEnd;

	@Override
	protected void redraw(Graphics g) {
		if (graph != null)
			graph.draw(g, getDrawingAreaDimension(), origin, scale);
	}

	@Override
	protected void onClick(MouseEvent e) {
		Location clicked = Location.newFromPoint(e.getPoint(), origin, scale);
		// find the closest node.
		double bestDist = Double.MAX_VALUE;
		Node closest = null;

		for (Node node : graph.getNodes().values()) {
			double distance = clicked.distance(node.location);
			if (distance < bestDist) {
				bestDist = distance;
				closest = node;
			}
		}
		if (clicked.distance(closest.location) < MAX_CLICKED_DISTANCE) {
			if (selectStart) {
				graph.setStartNode(closest);
				if (graph.getStartNode() != null) {
					getTextOutputArea().setText(
							"Start: \n" + closest.toString());
					selectStart = false;
					selectEnd = false;
				}
			}
			// select and set end node of A*
			else if (selectEnd) {
				graph.setEndNode(closest);
				if (graph.getEndNode() != null) {
					getTextOutputArea().setText("End: \n" + closest.toString());
					selectStart = false;
					selectEnd = false;
				}
			}
			// if it's close enough, highlight it and show some information.
			else {
				graph.setHighlight(closest);
				ArtPointSearch  artPointSearch = new ArtPointSearch(graph.getHiglight());
				graph.setCurrentArtPoints(artPointSearch.getArticulations());
				getTextOutputArea().setText(closest.toString());
			}
		}
	}

	@Override
	protected void onSearch() {
		if (trie == null)
			return;

		// get the search query and run it through the trie.
		String query = getSearchBox().getText();
		Collection<Road> selected = trie.get(query);

		// figure out if any of our selected roads exactly matches the search
		// query. if so, as per the specification, we should only highlight
		// exact matches. there may be (and are) many exact matches, however, so
		// we have to do this carefully.
		boolean exactMatch = false;
		for (Road road : selected)
			if (road.name.equals(query))
				exactMatch = true;

		// make a set of all the roads that match exactly, and make this our new
		// selected set.
		if (exactMatch) {
			Collection<Road> exactMatches = new HashSet<>();
			for (Road road : selected)
				if (road.name.equals(query))
					exactMatches.add(road);
			selected = exactMatches;
		}

		// set the highlighted roads.
		graph.setHighlight(selected);

		// now build the string for display. we filter out duplicates by putting
		// it through a set first, and then combine it.
		Collection<String> names = new HashSet<>();
		for (Road road : selected)
			names.add(road.name);
		String str = "";
		for (String name : names)
			str += name + "; ";

		if (str.length() != 0)
			str = str.substring(0, str.length() - 2);
		getTextOutputArea().setText(str);
	}

	@Override
	protected void onMove(Move m) {
		if (m == GUI.Move.NORTH) {
			origin = origin.moveBy(0, MOVE_AMOUNT / scale);
		} else if (m == GUI.Move.SOUTH) {
			origin = origin.moveBy(0, -MOVE_AMOUNT / scale);
		} else if (m == GUI.Move.EAST) {
			origin = origin.moveBy(MOVE_AMOUNT / scale, 0);
		} else if (m == GUI.Move.WEST) {
			origin = origin.moveBy(-MOVE_AMOUNT / scale, 0);
		} else if (m == GUI.Move.ZOOM_IN) {
			if (scale < MAX_ZOOM) {
				// yes, this does allow you to go slightly over/under the
				// max/min scale, but it means that we always zoom exactly to
				// the centre.
				scaleOrigin(true);
				scale *= ZOOM_FACTOR;
			}
		} else if (m == GUI.Move.ZOOM_OUT) {
			if (scale > MIN_ZOOM) {
				scaleOrigin(false);
				scale /= ZOOM_FACTOR;
			}
		}
	}

	@Override
	protected void onLoad(File nodes, File roads, File segments, File polygons) {
		graph = new Graph(nodes, roads, segments, polygons);
		trie = new Trie(graph.getRoads().values());
		origin = new Location(-250, 250); // close enough
		scale = 1;
	}

	/**
	 * This method does the nasty logic of making sure we always zoom into/out
	 * of the centre of the screen. It assumes that scale has just been updated
	 * to be either scale * ZOOM_FACTOR (zooming in) or scale / ZOOM_FACTOR
	 * (zooming out). The passed boolean should correspond to this, ie. be true
	 * if the scale was just increased.
	 */
	private void scaleOrigin(boolean zoomIn) {
		Dimension area = getDrawingAreaDimension();
		double zoom = zoomIn ? 1 / ZOOM_FACTOR : ZOOM_FACTOR;

		int dx = (int) ((area.width - (area.width * zoom)) / 2);
		int dy = (int) ((area.height - (area.height * zoom)) / 2);

		origin = Location.newFromPoint(new Point(dx, dy), origin, scale);
	}

	/** find route using AStar */
	private void findPath() {
		getTextOutputArea().setText("Searching for best Route: \n");
		graph.pathSearch();
		if (graph.getDistancePath() != null) {
			Map<String, Double> nameToLength = new HashMap<String, Double>();
			for (Segment s : graph.getDistancePath()) {
				if(!nameToLength.containsKey(s.road.name)){
					nameToLength.put(s.road.name, s.length);
				}
				nameToLength.put(s.road.name, nameToLength.get(s.road.name) + s.length);
			}
			Double totalLength = 0.0;
			for(Entry<String, Double> entry : nameToLength.entrySet()){
				getTextOutputArea().append(entry.getKey() + "\n" + "Length: " + entry.getValue() + "\n");
				totalLength += entry.getValue();
			}
			getTextOutputArea().append("Total Length: " + totalLength);
		}
	}

	@Override
	protected void onFindPath() {
		if (graph.getStartNode() != null && graph.getEndNode() != null)
			findPath();
	}

	/** set display of art points to true */
	@Override
	protected void displayArtPoints() {
		graph.displayArtPoints(true);
	}

	/** set display of art points to false */
	@Override
	protected void removeArtPoints() {
		graph.displayArtPoints(false);
	}

	/** set next click to select start node */
	@Override
	protected void selectStartNode() {
		selectStart = true;
		getTextOutputArea().setText("Select starting destination");
	}

	/** set next click to be end node */
	@Override
	protected void selectEndNode() {
		selectEnd = true;
		getTextOutputArea().setText("Select goal destination");
	}

	public static void main(String[] args) {
		new Mapper();
	}
}