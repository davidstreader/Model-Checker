package mc.client.graph;

import com.google.common.collect.Multimap;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.MultiLayerTransformer;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import lombok.RequiredArgsConstructor;
import mc.Constant;
import mc.compiler.CompilationObject;
import mc.processmodels.MultiProcessModel;
import mc.processmodels.ProcessModel;
import mc.processmodels.ProcessModelObject;
import mc.processmodels.ProcessType;
import mc.processmodels.automata.Automaton;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.*;
import java.util.stream.Collectors;


public class AutomataBorderPaintable implements VisualizationServer.Paintable {

    private final VisualizationViewer<GraphNode, DirectedEdge> vv;
    private final Multimap<String, GraphNode> automata;
    private final CompilationObject compiledResult;

    private static AutomataBorderPaintable single;

    //  final Color  CONCURRENT = (new Color(15,15 ,15, 50));
    //  final Color  SEQUENTIAL=  (new Color(50,0 ,0, 50));


    public AutomataBorderPaintable(VisualizationViewer<GraphNode, DirectedEdge> vv_,
                                   Multimap<String, GraphNode> automata_,
                                   CompilationObject compiledResult_) {
        vv = vv_;
        automata = automata_;
        compiledResult = compiledResult_;

    }

    public static AutomataBorderPaintable getAutomataBorderPaintable
        (VisualizationViewer<GraphNode, DirectedEdge> vv_,
         Multimap<String, GraphNode> automata_,
         CompilationObject compiledResult_) {
        return single;
    }

    public void paint(Graphics g_) {
        Graphics2D g = (Graphics2D) g_;

        Layout<GraphNode, DirectedEdge> layout = vv.getGraphLayout();

        //TODO: fix the transform and make it scale more effectively
        MultiLayerTransformer transform = vv
            .getRenderContext()
            .getMultiLayerTransformer();

        if (automata == null) {
            System.out.println("AutomataBorderPaintable  automata = null ");
            return;
        }
        for (String s : automata.keySet()) {
            if (s == null) {
                System.out.println("AutomataBorderPaintable  key = null");
                return;
            } else if (automata.get(s) == null) {
                System.out.println("AutomataBorderPaintable  key = " + s + " value =null");
                return;
            }
          /*   System.out.println(s+"->"+
                automata.get(s).stream().map(x->x.getNodeId()).collect(Collectors.joining(", ")));
         */
        }
        // automata.asMap().forEach((key, value) -> {
        // BEWARE can throw null pointer exception
        for (String key : automata.keySet()) {
            Collection<GraphNode> value = automata.get(key);
            Iterator<GraphNode> i = value.iterator();
            ProcessModel pmo;
            //Color fillColor = Color.decode("#808080");
            Color fillColor = NodeStates.CONCURRENT.getColorNodes(); // CONCURRENT;
            if (i.hasNext()) {
                GraphNode gn = i.next();
                //System.out.println("gn " + gn.toString());
                if (compiledResult != null &&
                    compiledResult.getProcessMap() != null &&
                    compiledResult.getProcessMap().containsKey(gn.getProcessModelId())
                ) {
                    pmo = compiledResult.getProcessMap().get(gn.getProcessModelId());
                    if (pmo.isSequential()) {
                        fillColor = NodeStates.SEQUENTIAL.getColorNodes();
                    }

                }
            }

            Rectangle2D boundingBox = computeBoundingBox(value, layout, transform);

            double d = 30;
            Shape rect = new RoundRectangle2D.Double(
                boundingBox.getMinX() - d,
                boundingBox.getMinY() - (d + 20),
                boundingBox.getWidth() + 2 * d,
                boundingBox.getHeight() + (2 * d) + 20,
                d, d);

            g.setColor(fillColor);
            g.fill(rect);
            //System.out.println("boarder color  = " + fillColor.toString());
            g.setColor(Color.BLACK);
            g.draw(rect);


            g.drawString(key, (int) (rect.getBounds2D().getX() + rect.getBounds2D().getWidth() / 2 - (key.length() / 2)),
                (int) rect.getBounds2D().getY() + 20);
        }
        ;

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
        return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }

    @Override
    public boolean useTransform() {
        return false;
    }
}
