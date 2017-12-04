package mc.client.graph;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.layout.ObservableCachingLayout;
import lombok.RequiredArgsConstructor;

import javax.xml.transform.Transformer;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class AutomataPainter implements VisualizationServer.Paintable{

    private final VisualizationViewer vv;
    private final Map<String,Set<GraphNode>> automata;

    @Override
    public void paint(Graphics g_) {
        Graphics2D g = (Graphics2D) g_;

        ObservableCachingLayout layout = (ObservableCachingLayout) vv.getGraphLayout();

        AffineTransform transform = vv
                .getRenderContext()
                .getMultiLayerTransformer()
                .getTransformer(Layer.LAYOUT)
                .getTransform();

        automata.entrySet().forEach(e -> {
            Rectangle2D boundingBox = computeBoundingBox(e.getValue(),layout,transform);

            double d = 40;
            Shape rect = new RoundRectangle2D.Double(
                    boundingBox.getMinX()-d,
                    boundingBox.getMinY()-d,
                    boundingBox.getWidth()+2*d,
                    boundingBox.getHeight()+2*d,
                    d, d);
            g.setColor(Color.decode("#808080"));
            g.fill(rect);
            g.setColor(Color.BLACK);
            g.draw(rect);
            g.drawString(e.getKey(),(int)boundingBox.getCenterX(),(int)boundingBox.getCenterY());
        });

    }

    private static <V> Rectangle2D computeBoundingBox( Iterable<V> vertices, ObservableCachingLayout<V, ?> layout,
                                                       AffineTransform at) {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        for (V vertex : vertices) {
            Point2D location = layout.apply(vertex);
            at.transform(location, location);
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
