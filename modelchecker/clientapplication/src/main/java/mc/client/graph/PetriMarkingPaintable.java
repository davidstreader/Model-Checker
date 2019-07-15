package mc.client.graph;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.MultiLayerTransformer;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import lombok.RequiredArgsConstructor;
import mc.processmodels.petrinet.components.PetriNetPlace;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PetriMarkingPaintable implements VisualizationServer.Paintable {
/*
  The CrossoverScalingControl  controls the scaling of the automata  not connected to the Token SIZE
 */

    private final VisualizationViewer<GraphNode, DirectedEdge> vv;
    private final Multimap<String,GraphNode> automata;
    private final Map<String, Multiset<PetriNetPlace>> currentMarking;
    @Override
    public void paint(Graphics g_) {
      Graphics2D g = (Graphics2D) g_;

      Layout<GraphNode,DirectedEdge> layout = vv.getGraphLayout();

      //TODO: fix the transform and make it scale more effectively
      MultiLayerTransformer transform = vv
        .getRenderContext()
        .getMultiLayerTransformer();

      Set<String> mk =
        currentMarking.values().stream().flatMap(x->x.stream()).map(y->y.getId()).collect(Collectors.toSet());

      automata.asMap().forEach((key, value) -> {
     //   Rectangle2D boundingBox = computeBoundingBox(value, layout, transform);

        for (GraphNode vertex : value) {
          //System.out.println("vertex Id " +vertex.getRepresentedFeature().getId());

          double diam = vertex.getType().getNodeShape().getBounds().getHeight()/3;
             // small enough  to be acceptable when scaled
          double rad = diam/2;
          if (vertex.getRepresentedFeature() instanceof PetriNetPlace &&

            mk.contains(((PetriNetPlace) vertex.getRepresentedFeature()).getId() )) {
            Point2D location = layout.apply(vertex);
            if (location != null) {
              location = transform.transform(location);
              Shape token = new Ellipse2D.Double(location.getX()-rad, location.getY()-rad, diam, diam);
              g.setColor(Color.BLACK);
              g.fill(token);
            } else {
              //System.out.println("null");
            }
          }
        }

      //  g.drawString(key, (int) (rect.getBounds2D().getX()+rect.getBounds2D().getWidth()/2-(key.length()/2)),
      //    (int) rect.getBounds2D().getY()+20);
      });

    }


    @Override
    public boolean useTransform() {
      return false;
    }
  }


