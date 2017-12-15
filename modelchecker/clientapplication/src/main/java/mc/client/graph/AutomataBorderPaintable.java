package mc.client.graph;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.MultiLayerTransformer;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import javafx.scene.text.Text;
import lombok.RequiredArgsConstructor;
import mc.client.ModelView;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class AutomataBorderPaintable implements VisualizationServer.Paintable{

    private final VisualizationViewer<GraphNode, DirectedEdge> vv;
    private final Map<String,Set<GraphNode>> automata;

    @Override
    public void paint(Graphics g_) {
        Graphics2D g = (Graphics2D) g_;

        Layout<GraphNode,DirectedEdge> layout = vv.getGraphLayout();

        //TODO: fix the transform and make it scale more effectively
        MultiLayerTransformer transform = vv
                .getRenderContext()
                .getMultiLayerTransformer();


        automata.forEach((key, value) -> {
            Rectangle2D boundingBox = computeBoundingBox(value, layout, transform);

            double d = 80;
            Shape rect = new RoundRectangle2D.Double(
                    boundingBox.getMinX() - d,
                    boundingBox.getMinY() - d,
                    boundingBox.getWidth() + 2 * d,
                    boundingBox.getHeight() + 2 * d,
                    d, d);
            g.setColor(Color.decode("#808080"));
            g.fill(rect);
            g.setColor(Color.BLACK);
            g.draw(rect);

            g.drawString(key, (int) (rect.getBounds2D().getX()+rect.getBounds2D().getWidth()/2-(0.5*key.length()/2)),
                              (int) rect.getBounds2D().getY()+20);
        });

    }

    private static <V> Rectangle2D computeBoundingBox(Collection<V> vertices, Layout<V, ?> layout,
                                                      MultiLayerTransformer at) {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        for (V vertex : vertices) {
            Point2D location = layout.apply(vertex);
            location = at.transform(location);
            minX = Math.min(minX, location.getX());
            minY = Math.min(minY, location.getY());
            maxX = Math.max(maxX, location.getX());
            maxY = Math.max(maxY, location.getY());
        }
        return new Rectangle2D.Double(minX, minY, maxX-minX, maxY-minY);
    }
    @Override
    public boolean useTransform() {
        return false;
    }
}
