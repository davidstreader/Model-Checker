package mc.client.ui;

import com.google.common.collect.Multimap;
import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.picking.PickedState;
import javafx.scene.control.ComboBox;
import lombok.Setter;
import mc.client.graph.DirectedEdge;
import mc.client.graph.GraphNode;
import mc.client.graph.NodeStates;
import mc.client.graph.NodeType;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.util.Map;
import java.util.Set;

/**
 * Created by smithjord3 on 12/12/17.
 */
public class DoubleClickHandler implements MouseListener {
    private Multimap<String, GraphNode> processModelVertexes;
    private VisualizationViewer<GraphNode,DirectedEdge> vv;
    private NodeStates previousState = null;
    private GraphNode previousModified = null;


    /**
     * Transform the point to the coordinate system in the
     * VisualizationViewer, then use either PickSuuport
     * (if available) or Layout to find a Vertex
     * @param point
     * @return
     */
    private GraphNode getVertex(Point2D point) {
        // adjust for scale and offset in the VisualizationViewer
        Point2D p = point;
        //vv.getRenderContext().getBasicTransformer().inverseViewTransform(point);
        GraphElementAccessor<GraphNode, DirectedEdge> pickSupport = vv.getPickSupport();
        Layout<GraphNode, DirectedEdge> layout = vv.getGraphLayout();
        GraphNode v = null;
        if(pickSupport != null) {
            v = pickSupport.getVertex(layout, p.getX(), p.getY());
        }
        return v;
    }

    public DoubleClickHandler(Multimap<String, GraphNode> processModelVertexes_, VisualizationViewer<GraphNode,DirectedEdge> vv_) {
        processModelVertexes = processModelVertexes_;
        vv = vv_;

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
        GraphNode currentNodeClicked = getVertex(e.getPoint());
        if (currentNodeClicked != null) {



            if(previousModified != currentNodeClicked) { // If we've clicked on a new node
                if(previousModified != null) {
                    previousModified.setNodeTermination(previousState); // Reset the previous to unselected state
                }

                previousModified = currentNodeClicked; // Store the node
                previousState = currentNodeClicked.getNodeTermination(); // And the state
                currentNodeClicked.setNodeTermination(NodeStates.SELECT); // Before setting it with the selected state
            }


            String processName = currentNodeClicked.getAutomata();
            if (processModelVertexes.containsKey(processName)) {
                PickedState<GraphNode> pickedVertexState = vv.getPickedVertexState(); // The graph has 'picking' support.
                // So we can just add them to the picked list,
                // and let pickingGraphMousePlugin deal with it
                processModelVertexes.get(processName).stream()
                        .filter(v -> v != currentNodeClicked)
                        .forEach(v -> pickedVertexState.pick(v, true));

            }

        } else { // IF we've clicked, but it isnt on a node
            if(previousModified != null) {  // And the previous node isnt null
                previousModified.setNodeTermination(previousState); // Reset the state
                previousModified = null;
                previousState = null;
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
     */
    @Override
    public void mouseExited(MouseEvent e){

    }
}
