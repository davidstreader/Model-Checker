package mc.client.graph;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.MultiLayerTransformer;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import lombok.RequiredArgsConstructor;
import mc.client.ui.CurrentMarkingsSeen;
import mc.processmodels.petrinet.components.PetriNetPlace;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PetriMarkingPaintable implements VisualizationServer.Paintable {
/*
  The CrossoverScalingControl  controls the scaling of the automata  not connected to the Token SIZE
 */

    private final VisualizationViewer<GraphNode, DirectedEdge> vv;
    private final Multimap<String,GraphNode> automata;
    //private final Map<String, Multiset<PetriNetPlace>> currentMarking;
    @Override
    public void paint(Graphics g_) {
      Graphics2D g = (Graphics2D) g_;
      //System.out.println("PetriMarkingPaintable paint "+CurrentMarkingsSeen.myString() );
      Layout<GraphNode,DirectedEdge> layout = vv.getGraphLayout();

      //TODO: fix the transform and make it scale more effectively
      MultiLayerTransformer transform = vv
        .getRenderContext()
        .getMultiLayerTransformer();
      Map<String,Set<String>> pnidToSetPlaceId = new TreeMap<>();
      for (String pid: CurrentMarkingsSeen.currentMarkingsSeen.keySet()) {
        Set<String> mk = CurrentMarkingsSeen.getIds(pid);
        pnidToSetPlaceId.put(pid,mk);
        //System.out.println(pid+"->"+mk);
      }

        automata.asMap().forEach((key, value) -> {
          //   Rectangle2D boundingBox = computeBoundingBox(value, layout, transform);

          for (GraphNode vertex : value) {
            //System.out.println("vertex Id " +vertex.getRepresentedFeature().getId());

            double diam = vertex.getType().getNodeShape().getBounds().getHeight() / 3;
            // small enough  to be acceptable when scaled
            double rad = diam / 2;
            if (vertex.getRepresentedFeature() instanceof PetriNetPlace &&
              pnidToSetPlaceId.get( vertex.getProcessModelId())
                .contains(((PetriNetPlace) vertex.getRepresentedFeature()).getId())) {
              //System.out.println("found "+vertex.getProcessModelId()+" "+((PetriNetPlace) vertex.getRepresentedFeature()).getId());
              Point2D location = layout.apply(vertex);
              if (location != null) {
                location = transform.transform(location);
                Shape token = new Ellipse2D.Double(location.getX() - rad, location.getY() - rad, diam, diam);
                g.setColor(Color.BLACK);
                g.fill(token);
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


