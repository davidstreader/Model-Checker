package mc.client.ui;

import com.google.common.collect.Multiset;
import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import mc.client.graph.DirectedEdge;
import mc.client.graph.GraphNode;
import mc.client.graph.NodeStates;
import mc.client.graph.NodeType;
import mc.processmodels.conversion.TokenRule;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Map;

public class CanvasMouseMotionListener implements MouseMotionListener {
  private VisualizationViewer<GraphNode, DirectedEdge> vv;
 // private Map<String, Multiset<PetriNetPlace>> currentMarkingsSeen;

  public CanvasMouseMotionListener(VisualizationViewer<GraphNode, DirectedEdge> vv_) {
    vv = vv_;


  }

  public void mouseMoved(MouseEvent e) {
/*
    GraphNode currentNode;
    currentNode = getVertex(e.getPoint());
    if (currentNode == null ) {
      return;
    } else if (!currentNode.getType().equals(NodeType.PETRINET_TRANSITION)) {
      return;
    }
    //  only PetriNetTransitions  reset all to Nominal

    //System.out.println(" mListener " + CurrentMarkingsSeen.currentMarkingsSeen.keySet());
    String pid = currentNode.getProcessModelId();
    //System.out.println(" mListener " +pid) ;
        if (TokenRule.isSatisfied(CurrentMarkingsSeen.currentMarkingsSeen.get(pid),
          ((PetriNetTransition) currentNode.getRepresentedFeature()))) {
          currentNode.setNodeColor(NodeStates.SELECT);
        } else {
          currentNode.setNodeColor(NodeStates.STOPSTART5);
        }

*/
  }

  public void mouseDragged(MouseEvent e) {

  }

  /**
   * Transform the point to the coordinate system in the
   * VisualizationViewer, then use either PickSuuport
   * (if available) or Layout to find a Vertex
   *
   * @return The vertex that was clicked on
   */
  private GraphNode getVertex(Point2D point) {
    // adjust for scale and offset in the VisualizationViewer
    //vv.getRenderContext().getBasicTransformer().inverseViewTransform(point);
    GraphElementAccessor<GraphNode, DirectedEdge> pickSupport = vv.getPickSupport();
    Layout<GraphNode, DirectedEdge> layout = vv.getGraphLayout();
    GraphNode v = GraphNode.dummy();
    if (pickSupport != null) {
      v = pickSupport.getVertex(layout, point.getX(), point.getY());
    }
    return v;
  }
}
