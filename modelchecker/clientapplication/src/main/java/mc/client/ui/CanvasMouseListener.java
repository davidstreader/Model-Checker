package mc.client.ui;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.picking.PickedState;
import mc.Constant;
import mc.client.graph.DirectedEdge;
import mc.client.graph.GraphNode;
import mc.client.graph.NodeStates;
import mc.processmodels.MappingNdMarking;
import mc.processmodels.ProcessModelObject;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.conversion.TokenRule;
import mc.processmodels.conversion.TokenRulePureFunctions;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.event.InputEvent;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by smithjord3 on 12/12/17.
 */
public class CanvasMouseListener implements MouseListener {
    private Map<String, MappingNdMarking> mappings; // ProcessId->TokenMapping
    private Multimap<String, GraphNode> processModelVertexes; //Place,tran,node -> Graph
    private VisualizationViewer<GraphNode, DirectedEdge> vv;
    String automataIDofNet = null;
    private Map<GraphNode, NodeStates> currentlyColored = new HashMap<>();
    //private Map<String, Multiset<PetriNetPlace>> currentMarkingsSeen = new TreeMap<>();


    private String markingToString(Set<PetriNetPlace> m) {
        StringBuilder sb = new StringBuilder();
        m.stream().forEach(x -> sb.append(x.getId() + ", "));
        return sb.toString();
    }

    public CanvasMouseListener(Multimap<String, GraphNode> processModelVertexes_, VisualizationViewer<GraphNode, DirectedEdge> vv_,
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
     * Split on SHIFT held down
     * We need to store old Color and reinstate but only for Place
     * as Transition has color changed by MotionListener
     * <p>
     * int onmask = InputEvent.SHIFT_DOWN_MASK
     * *    int offmask = CTRL_DOWN_MASK;
     * *    if ((event.getModifiersEx() &amp; (onmask | offmask)) == onmask) {
     * *        ...
     * *    }
     */
    @Override
    public void mouseClicked(MouseEvent e) {

        GraphNode currentNodeClicked = getVertex(e.getPoint());
        DirectedEdge currentDirectedEdge = getEdge(e.getPoint());
        if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == 0) {
            //System.out.println("not SHIFT mouseClick");
            if (currentNodeClicked != null) {
                String pid = currentNodeClicked.getProcessModelId();
                if (processModelVertexes.containsKey(pid)) {
                    PickedState<GraphNode> pickedVertexState = vv.getPickedVertexState(); // The graph has 'picking' support.
                    // So we can just add them to the picked list,
                    // and let pickingGraphMousePlugin deal with it
                    processModelVertexes.get(pid).stream()
                        .filter(v -> v != currentNodeClicked)
                        .forEach(v -> pickedVertexState.pick(v, true));
                }
            } else if (currentDirectedEdge != null) {
                //System.out.println("Clicked on Edge "+currentDirectedEdge.getLabel());
            }
        } else {
            //System.out.println("SHIFT mouseClick");
            //System.out.println(mappings.keySet().stream().map(x -> x + "-- " + mappings.get(x).toString()).collect(Collectors.joining(", ")));
            //System.out.println("pMV " + processModelVertexes.keySet().stream().map(x -> x + processModelVertexes.get(x).stream().map(y -> y.getNodeId()).collect(Collectors.joining(", "))).collect(Collectors.joining("\n ")));
            MappingNdMarking thisMapping;
            if (currentNodeClicked != null) {
                String processId = currentNodeClicked.getProcessModelId();
                automataIDofNet = processId.substring(0, processId.indexOf('(')) + "(automata)";
                //System.out.println("CLICKED " + currentNodeClicked.toString());
                ProcessModelObject clk = currentNodeClicked.getRepresentedFeature();
                if (!(clk instanceof PetriNetTransition)) {
                    //System.out.println("Clicked on "+ currentNodeClicked.getNodeId());
                    // if (!currentlyColored.containsKey(currentNodeClicked)) { // If we've clicked on a new node
                  /*  for (GraphNode currentColoredNode : currentlyColored.keySet()) {
                        currentColoredNode.setNodeColor(currentlyColored.get(currentColoredNode)); // Reset the previous to unselected state
                        //System.out.println("ReColor to " + currentColoredNode.toString());
                    } */
                    currentlyColored.clear();  // contains Places and Nodes
                    //  }
                    currentlyColored.put(currentNodeClicked, currentNodeClicked.getNodeColor());// Store the node, and the state
                    currentNodeClicked.setNodeColor(NodeStates.SELECT); // Before setting it with the selected state
                    //System.out.println("currentlyColored - " + currentNodeClicked.getNodeId() + " " + currentNodeClicked.getNodeColor());

                }
                //System.out.println("Id " + pid);
                //System.out.println("\n clk " + clk.getId());
                //System.out.println(thisMapping.toString());
                if (mappings != null && mappings.containsKey(processId)) {
                    thisMapping = mappings.get(processId);
                    if (!(clk instanceof PetriNetTransition)) {
                        if (thisMapping != null) {
                            //If this node/place has a mapping associated with it select those also.
                            Collection<GraphNode> vertexes = vv.getGraphLayout().getGraph().getVertices();

                            if (clk instanceof AutomatonNode) {
                                //Node
                                Map<AutomatonNode, Multiset<PetriNetPlace>> mapping = thisMapping.getNodeToMarking();
                                //System.out.println("A->P " + MappingNdMarking.n2m2String(mapping));
                                for (PetriNetPlace place : mapping.get(clk)) {
                                    // N^2, might be a problem for larger displays
                                    //System.out.println("place " + place.getId());
                                    for (GraphNode g : vertexes) {
                                        //System.out.println("Vertex " +g.getRepresentedFeature().getId());
                                        if (place.getId() == g.getRepresentedFeature().getId()) {
                                            //System.out.println("A " + g.getNodeId());
                                            currentlyColored.put(g, g.getNodeColor());
                                            g.setNodeColor(NodeStates.SELECT);
                                            //System.out.println("currentlyColored A " + g.getNodeId() + " " + g.getNodeColor());
                                            break;
                                        }
                                    }
                                }
                            } else if (clk instanceof PetriNetPlace) {
                                //Place
                                Map<Multiset<PetriNetPlace>, AutomatonNode> mapping = thisMapping.getMarkingToNode();
                                String out =
                                    mapping.keySet().stream().map(x -> "{" + x.stream().map(y -> y.getId()).
                                        collect(Collectors.joining(", ")) + "} ->" +
                                        mapping.get(x).getId()).collect(Collectors.joining("\n"));
                                //System.out.println("P->A \n" + out);
                                for (Multiset<PetriNetPlace> marking : mapping.keySet()) {
                                    Set<String> markingId = marking.stream().map(x -> x.getId()).collect(Collectors.toSet());
                                    //System.out.println("marking " + marking.stream().map(x -> x.getId()).collect(Collectors.joining(", ")));
                                    if (markingId.contains(clk.getId())) {
                                        //System.out.println("found " + clk.getId());
                                        for (GraphNode g : vertexes) {
                                            if (mapping.get(marking) == g.getRepresentedFeature()) {
                                                currentlyColored.put(g, g.getNodeColor());
                                                //System.out.println("currentlyColored P " + g.getNodeId() + " " + g.getNodeColor());
                                                g.setNodeColor(NodeStates.TEMP2);
                                                break;
                                            }
                                        }
                                    }
                                }

                                colorOwenedNodes((PetriNetPlace) clk, processId);
                            }
                        } //else would be for null mappings

                    } else {
                        // PetriNetTransition
                        PetriNetTransition pntClicked = ((PetriNetTransition) clk);
                        if (!pntClicked.getLabel().equals(Constant.DEADLOCK)) {
                            //System.out.println("In Net " + pid + " From node " + pntClicked.getId() + " " + pntClicked.getLabel());
                            Multiset<PetriNetPlace> cm = CurrentMarkingsSeen.currentMarkingsSeen.get(processId);
                            Multiset<PetriNetPlace> newMarking;
                          List<Multiset<PetriNetPlace>> newMarkings;
                            if (TokenRule.isSatisfied(cm, pntClicked)) {
                                newMarkings = TokenRulePureFunctions.newMarking(cm, pntClicked);
                                newMarking = newMarkings.get(0);
                                CurrentMarkingsSeen.currentMarkingsSeen.put(processId, newMarking);

                            //System.out.println(" ?mapping? "+thisMapping.n2m2String()) ;
                                if (thisMapping.contains(newMarking)) {
                             //System.out.println("contains "+newMarking.stream().map(n->n.getId()+", ").collect(Collectors.joining()));

                                    //System.out.println("aid "+aid);
                                    //System.out.println("pMV "+processModelVertexes.keySet());
                                    for (GraphNode autG : processModelVertexes.get(automataIDofNet)) {
                                        //System.out.println(autG.toString());
                                        if (autG.getNodeId().equals(thisMapping.get(newMarking).getId())) {
                                            autG.setNodeColor(NodeStates.ERROR);
                                            //System.out.println("Transition aut " + autG.getNodeId());
                                        } else {
                                          //System.out.println("not NewMarking ");
                                            autG.setNodeColor(autG.getOriginalColor());
                                        }

                                    }
                                    ;
                                }
                            }
                        }
                        refreshtransitionColor();
                        //System.out.println("CLICKED " + CurrentMarkingsSeen.myString() + "\n");
                        //System.out.println("current " + currentMarkingToString(pid));
                    }
                }

            } else { // IF we've clicked, but it isnt on a node
              /*  for (GraphNode currentColoredNode : currentlyColored.keySet()) {
                    if (currentColoredNode.getRepresentedFeature() instanceof PetriNetTransition) continue;
                    currentColoredNode.setNodeColor(currentlyColored.get(currentColoredNode));

                } */
                if (automataIDofNet != null) {
                    //System.out.println("aid = "+ automataIDofNet);
                    for (GraphNode autG : processModelVertexes.get(automataIDofNet)) {

                        //System.out.println(autG.getNodeId());
                        autG.setNodeColor(autG.getOriginalColor());
                    }
                }
                // Reset the previous to unselected Place
                currentlyColored.clear();
                CurrentMarkingsSeen.setCurrentMarkingsSeen(CurrentMarkingsSeen.getRootMarkings());
                refreshtransitionColor();
                removeColorOwenedNodes("");

            }
            // needs to be here for Transitions
            //System.out.println("DoubleClickHandler  mappings");
            //System.out.println(mappings.keySet().stream().map(x->"\n"+x+"\n-- "+mappings.get(x).toString()).collect(Collectors.joining(", ")));
        }
        //System.out.println("pingo");
    }

    public void refreshtransitionColor() {
        for (GraphNode gn : processModelVertexes.values()) {
            if (gn.getRepresentedFeature() instanceof PetriNetTransition) {
                if (((PetriNetTransition) gn.getRepresentedFeature())
                    .getLabel().equals(Constant.DEADLOCK))
                    gn.setNodeColor(NodeStates.NOMINAL);
                else {
                    if (TokenRule.isSatisfied(CurrentMarkingsSeen.currentMarkingsSeen.get(gn.getProcessModelId()),
                        ((PetriNetTransition) gn.getRepresentedFeature()))) {
                        gn.setNodeColor(NodeStates.SELECT);
                    } else {
                        gn.setNodeColor(gn.getOriginalColor());
                    }
                }
            }

        }

    }

    /*
       This marks a "track"  all places with same ownership" and stores the old colors when needed
     */
    private void colorOwenedNodes(PetriNetPlace pl, String pid) {
        removeColorOwenedNodes(pid);
        Set<String> plOwners = pl.getOwners();
        for (GraphNode gn : processModelVertexes.values()) {
            if (gn.getRepresentedFeature() instanceof PetriNetPlace) {
                if (gn.getProcessModelId().equals(pid)) {
                    PetriNetPlace p = ((PetriNetPlace) gn.getRepresentedFeature());

                    Set<String> owners = p.getOwners();
                    if (owners.equals(plOwners)) {
                        if (p.isStart() || p.isSTOP()) {
                            gn.setNodeColor(NodeStates.TEMP);
                        } else {
                            if (pl == p)
                                gn.setNodeColor(NodeStates.TEMP2);
                            else
                                gn.setNodeColor(NodeStates.TEMP);
                        }
                    }
                }
            }
        }
    }

    /*
       remove "track" color and reinstate saved color
     */
    private void removeColorOwenedNodes(String pid) {
        for (GraphNode gn : processModelVertexes.values()) {
            if (gn.getRepresentedFeature() instanceof PetriNetPlace) {
                if (pid.equals("") || gn.getProcessModelId().equals(pid)) {
                    PetriNetPlace p = ((PetriNetPlace) gn.getRepresentedFeature());

                    gn.setNodeColor(gn.getOriginalColor());


                }
            }
        }
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
     * Component as in Button checkbox not area on canvas
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
        GraphNode v = GraphNode.dummy();
        if (pickSupport != null) {
            v = pickSupport.getVertex(layout, point.getX(), point.getY());
        }
        //null //System.out.println("getNode "+v.getLabel()+ " "+v.getProcessModelId());
        return v;
    }
    private DirectedEdge getEdge(Point2D point) {
        // adjust for scale and offset in the VisualizationViewer
        //vv.getRenderContext().getBasicTransformer().inverseViewTransform(point);
        GraphElementAccessor<GraphNode, DirectedEdge> pickSupport = vv.getPickSupport();
        Layout<GraphNode, DirectedEdge> layout = vv.getGraphLayout();
        DirectedEdge edge = DirectedEdge.dummy();
        if (pickSupport != null) {
            edge = pickSupport.getEdge(layout, point.getX(), point.getY());
        }

        return edge;
    }
}
