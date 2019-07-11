package mc.client.ui;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.picking.PickedState;
import mc.client.graph.DirectedEdge;
import mc.client.graph.GraphNode;
import mc.client.graph.NodeStates;
import mc.processmodels.MappingNdMarking;
import mc.processmodels.ProcessModelObject;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by smithjord3 on 12/12/17.
 */
public class DoubleClickHandler implements MouseListener {
    private Map<String, MappingNdMarking> mappings;
    private Multimap<String, GraphNode> processModelVertexes;
    private VisualizationViewer<GraphNode, DirectedEdge> vv;

    private Map<GraphNode, NodeStates> currentlyColored = new HashMap<>();


    public DoubleClickHandler(Multimap<String, GraphNode> processModelVertexes_, VisualizationViewer<GraphNode, DirectedEdge> vv_,
                              Map<String, MappingNdMarking> nodeAndMarkingMappings) {
        processModelVertexes = processModelVertexes_;
        vv = vv_;
        mappings = nodeAndMarkingMappings;
   }

    public void updateProcessModelList(Multimap<String, GraphNode> processModels) {
        processModelVertexes = processModels;
    }

    /**
     * Invoked when the mouse button has been clicked (pressed
     * and released) on a component.
     */
    @Override
    public void mouseClicked(MouseEvent e) {
      System.out.println("mouseClick");
      System.out.println(mappings.keySet().stream().map(x->x+"-- "+mappings.get(x).toString()).collect(Collectors.joining(", ")));

      GraphNode currentNodeClicked = getVertex(e.getPoint());
        if (currentNodeClicked != null) {
            //System.out.println("Clicked on "+ currentNodeClicked.getNodeId());
            if (!currentlyColored.containsKey(currentNodeClicked)) { // If we've clicked on a new node
                for (GraphNode currentColoredNode : currentlyColored.keySet())
                    currentColoredNode.setNodeColor(currentlyColored.get(currentColoredNode)); // Reset the previous to unselected state

                currentlyColored.clear();

                currentlyColored.put(currentNodeClicked, currentNodeClicked.getNodeColor());// Store the node, and the state
                currentNodeClicked.setNodeColor(NodeStates.SELECT); // Before setting it with the selected state

                //If this node/place has a mapping associated with it select those also.
                MappingNdMarking thisMapping = mappings.get(currentNodeClicked.getProcessModelId());
                ProcessModelObject clk = currentNodeClicked.getRepresentedFeature();
              System.out.println("Id "+currentNodeClicked.getProcessModelId());
                System.out.println( "\n clk "+ clk.getId());
                System.out.println( thisMapping.toString());

                if (!(clk instanceof PetriNetTransition) &&
                       mappings.containsKey(currentNodeClicked.getProcessModelId())) {

                    if (thisMapping != null) {
                        Collection<GraphNode> vertexes = vv.getGraphLayout().getGraph().getVertices();
                        if (clk instanceof AutomatonNode) {
                            Map<AutomatonNode, Multiset<PetriNetPlace>> mapping = thisMapping.getNodeToMarking();
                      System.out.println("A->P "+ MappingNdMarking.n2m2String(mapping));
                            for (PetriNetPlace place : mapping.get(clk)) {
                                // N^2, might be a problem for larger displays
                      System.out.println("place "+place.getId());
                                for (GraphNode g : vertexes) {
                                  //System.out.println("Vertex " +g.getRepresentedFeature().getId());
                                  if (place.getId() == g.getRepresentedFeature().getId()) {
                                    System.out.println("A " + g.getNodeId());
                                    currentlyColored.put(g, g.getNodeColor());
                                    g.setNodeColor(NodeStates.SELECT);
                                    break;
                                  }
                                }
                            }
                        } else if (clk instanceof PetriNetPlace) {
                            Map<Multiset<PetriNetPlace>, AutomatonNode> mapping = thisMapping.getMarkingToNode();
                            String out =
                              mapping.keySet().stream().map(x->"{"+ x.stream().map(y->y.getId()).
                                collect(Collectors.joining(", ")  )+"} ->" +
                                mapping.get(x).getId()).collect(Collectors.joining("\n"));
                        System.out.println("P->A \n"+ out);
                            for (Multiset<PetriNetPlace> marking : mapping.keySet()) {
                    Set<String> markingId = marking.stream().map(x->x.getId()).collect(Collectors.toSet());
                    System.out.println("marking "+ marking.stream().map(x->x.getId()).collect(Collectors.joining(", ")));
                                if (markingId.contains(clk.getId())) {
                                  System.out.println("found " + clk.getId());
                                  for (GraphNode g : vertexes) {
                                    if (mapping.get(marking) == g.getRepresentedFeature()) {
                                      System.out.println("g " + g.getNodeId());
                                      currentlyColored.put(g, g.getNodeColor());
                                      g.setNodeColor(NodeStates.SELECT);
                                      break;
                                    }
                                  }
                                }
                            }
                        }
                    }

                }

            }


            String processName = currentNodeClicked.getProcessModelId();
            if (processModelVertexes.containsKey(processName)) {
                PickedState<GraphNode> pickedVertexState = vv.getPickedVertexState(); // The graph has 'picking' support.
                // So we can just add them to the picked list,
                // and let pickingGraphMousePlugin deal with it
                processModelVertexes.get(processName).stream()
                        .filter(v -> v != currentNodeClicked)
                        .forEach(v -> pickedVertexState.pick(v, true));

            }

        } else { // IF we've clicked, but it isnt on a node
            for (GraphNode currentColoredNode : currentlyColored.keySet())
                currentColoredNode.setNodeColor(currentlyColored.get(currentColoredNode)); // Reset the previous to unselected state

            currentlyColored.clear();
        }

        System.out.println("DoubleClickHandler");
        System.out.println(mappings.keySet().stream().map(x->x+"-- "+mappings.get(x).toString()).collect(Collectors.joining(", ")));

    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     */
    @Override
    public void mousePressed(MouseEvent e) {
    }

    /**
     * Invoked when a mouse button has been released on a component.
     */
    @Override
    public void mouseReleased(MouseEvent e) {
    }

    /**
     * Invoked when the mouse enters a component.
     */
    @Override
    public void mouseEntered(MouseEvent e) {
    }

    /**
     * Invoked when the mouse exits a component.
     */
    @Override
    public void mouseExited(MouseEvent e) {
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
        GraphNode v = null;
        if (pickSupport != null) {
            v = pickSupport.getVertex(layout, point.getX(), point.getY());
        }
        return v;
    }
}
