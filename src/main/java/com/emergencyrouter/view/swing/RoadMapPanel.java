package com.emergencyrouter.view.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.swing.JPanel;

import com.emergencyrouter.interfaces.Location;
import com.emergencyrouter.model.Edge;
import com.emergencyrouter.model.Graph;
import com.emergencyrouter.model.Node;
import com.emergencyrouter.model.Route;

/**
 * Draws the road graph and current route using Java2D.
 *
 * <p>Use this panel inside Swing views when users need a visual understanding
 * of nodes, roads, closures, and the active emergency route.</p>
 *
 * <p>Impact on the system: this file turns graph and route data into a visual
 * road map. It does not change routing decisions; it only displays nodes,
 * normal roads, closed roads, and the currently selected route in a readable
 * way for the Swing UI.</p>
 *
 * <p>Main methods in this file:</p>
 * <ul>
 *     <li>{@link #setGraph(Graph)} chooses the graph to draw.</li>
 *     <li>{@link #setRoute(Route)} chooses the highlighted route.</li>
 *     <li>{@link #paintComponent(Graphics)} redraws the full map.</li>
 *     <li>{@code drawRoads(...)}, {@code drawRoute(...)}, and
 *     {@code drawNodes(...)} split the rendering work into focused steps.</li>
 *     <li>{@code toPoint(...)} scales latitude/longitude values to panel
 *     coordinates.</li>
 * </ul>
 */
public final class RoadMapPanel extends JPanel {
    private static final int PADDING = 50;
    private static final int NODE_RADIUS = 9;
    private static final Color BACKGROUND = new Color(248, 250, 252);
    private static final Color OPEN_ROAD = new Color(100, 116, 139);
    private static final Color CLOSED_ROAD = new Color(220, 38, 38);
    private static final Color ROUTE_LINE = new Color(22, 163, 74);
    private static final Color NODE_FILL = new Color(37, 99, 235);

    private Graph graph;
    private Route route;

    /**
     * Creates the map panel with stable default size and background.
     */
    public RoadMapPanel() {
        setPreferredSize(new Dimension(980, 620));
        setBackground(BACKGROUND);
    }

    /**
     * Sets the graph that should be drawn and schedules a repaint.
     *
     * @param graph graph to display
     */
    public void setGraph(Graph graph) {
        this.graph = graph;
        repaint();
    }

    /**
     * Sets the route that should be highlighted and schedules a repaint.
     *
     * @param route route to highlight
     */
    public void setRoute(Route route) {
        this.route = route;
        repaint();
    }

    /**
     * Draws the graph, route, and labels whenever Swing repaints the panel.
     *
     * @param graphics graphics context provided by Swing
     */
    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D graphics2D = (Graphics2D) graphics.create();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (graph == null || graph.getNodes().isEmpty()) {
            drawEmptyState(graphics2D);
            graphics2D.dispose();
            return;
        }

        Bounds bounds = Bounds.from(graph.getNodes());
        drawRoads(graphics2D, bounds);
        drawRoute(graphics2D, bounds);
        drawNodes(graphics2D, bounds);

        graphics2D.dispose();
    }

    private void drawEmptyState(Graphics2D graphics2D) {
        graphics2D.setColor(new Color(71, 85, 105));
        graphics2D.drawString("No road graph loaded.", 24, 32);
    }

    private void drawRoads(Graphics2D graphics2D, Bounds bounds) {
        for (Edge edge : graph.getEdges()) {
            Point sourcePoint = toPoint(edge.getSource(), bounds);
            Point destinationPoint = toPoint(edge.getDestination(), bounds);

            if (edge.isClosed()) {
                graphics2D.setColor(CLOSED_ROAD);
                graphics2D.setStroke(new BasicStroke(
                        1.8f,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND,
                        1.0f,
                        new float[] {8.0f, 6.0f},
                        0.0f
                ));
            } else {
                graphics2D.setColor(OPEN_ROAD);
                graphics2D.setStroke(new BasicStroke(1.6f));
            }

            graphics2D.drawLine(sourcePoint.x, sourcePoint.y, destinationPoint.x, destinationPoint.y);
        }
    }

    private void drawRoute(Graphics2D graphics2D, Bounds bounds) {
        if (route == null || route.getPath().size() < 2) {
            return;
        }

        List<Node> routeNodes = route.getPath().stream()
                .filter(Node.class::isInstance)
                .map(Node.class::cast)
                .toList();

        if (routeNodes.size() < 2) {
            return;
        }

        Stroke previousStroke = graphics2D.getStroke();
        graphics2D.setColor(ROUTE_LINE);
        graphics2D.setStroke(new BasicStroke(4.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        for (int index = 0; index < routeNodes.size() - 1; index++) {
            Point sourcePoint = toPoint(routeNodes.get(index), bounds);
            Point destinationPoint = toPoint(routeNodes.get(index + 1), bounds);
            graphics2D.drawLine(sourcePoint.x, sourcePoint.y, destinationPoint.x, destinationPoint.y);
        }

        graphics2D.setStroke(previousStroke);
    }

    private void drawNodes(Graphics2D graphics2D, Bounds bounds) {
        for (Node node : graph.getNodes()) {
            Point point = toPoint(node, bounds);
            graphics2D.setColor(NODE_FILL);
            graphics2D.fillOval(
                    point.x - NODE_RADIUS,
                    point.y - NODE_RADIUS,
                    NODE_RADIUS * 2,
                    NODE_RADIUS * 2
            );

            graphics2D.setColor(Color.WHITE);
            graphics2D.drawOval(
                    point.x - NODE_RADIUS,
                    point.y - NODE_RADIUS,
                    NODE_RADIUS * 2,
                    NODE_RADIUS * 2
            );

            drawLabel(graphics2D, node.getId(), point);
        }
    }

    private void drawLabel(Graphics2D graphics2D, String label, Point point) {
        FontMetrics metrics = graphics2D.getFontMetrics();
        int labelWidth = metrics.stringWidth(label);
        int x = point.x - labelWidth / 2;
        int y = point.y - NODE_RADIUS - 6;

        graphics2D.setColor(new Color(15, 23, 42));
        graphics2D.drawString(label, x, y);
    }

    private Point toPoint(Location location, Bounds bounds) {
        int width = Math.max(getWidth(), getPreferredSize().width);
        int height = Math.max(getHeight(), getPreferredSize().height);
        double longitudeRange = Math.max(bounds.maxLongitude() - bounds.minLongitude(), 0.000001);
        double latitudeRange = Math.max(bounds.maxLatitude() - bounds.minLatitude(), 0.000001);
        double xRatio = (location.getLongitude() - bounds.minLongitude()) / longitudeRange;
        double yRatio = (bounds.maxLatitude() - location.getLatitude()) / latitudeRange;

        int x = PADDING + (int) Math.round(xRatio * Math.max(width - PADDING * 2, 1));
        int y = PADDING + (int) Math.round(yRatio * Math.max(height - PADDING * 2, 1));
        return new Point(x, y);
    }

    private record Bounds(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude) {
        private static Bounds from(Collection<Node> nodes) {
            Objects.requireNonNull(nodes, "nodes must not be null");
            List<Node> nodeList = new ArrayList<>(nodes);

            double minLatitude = nodeList.stream().mapToDouble(Node::getLatitude).min().orElse(0);
            double maxLatitude = nodeList.stream().mapToDouble(Node::getLatitude).max().orElse(0);
            double minLongitude = nodeList.stream().mapToDouble(Node::getLongitude).min().orElse(0);
            double maxLongitude = nodeList.stream().mapToDouble(Node::getLongitude).max().orElse(0);

            return new Bounds(minLatitude, maxLatitude, minLongitude, maxLongitude);
        }
    }
}
