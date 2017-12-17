package mc.client.ui;

import com.google.common.collect.Multimap;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.picking.PickedState;
import javafx.scene.control.ComboBox;
import lombok.Setter;
import mc.client.graph.DirectedEdge;
import mc.client.graph.GraphNode;

import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.Set;

/**
 * Created by smithjord3 on 12/12/17.
 */
public class DoubleClickHandler implements GraphMouseListener<GraphNode> {
    private long startTime = -1;
    private Multimap<String, GraphNode> processModelVertexes;
    private VisualizationViewer<GraphNode,DirectedEdge> vv;

    public DoubleClickHandler(Multimap<String, GraphNode> processModelVertexes_, VisualizationViewer<GraphNode,DirectedEdge> vv_) {
        processModelVertexes = processModelVertexes_;
        vv = vv_;

    }

    @Override
    public void graphClicked(GraphNode graphNode, MouseEvent me) {

        if(startTime == -1)
            startTime = System.currentTimeMillis();
        else if(System.currentTimeMillis() - startTime <= 500){ // 500 millis is the average time for a double click
            //if double click on a vertex, get the automata name then select all other vertexes

            String automataName = graphNode.getAutomata();
            if(processModelVertexes.containsKey(automataName)) {
                PickedState<GraphNode> pickedVertexState = vv.getPickedVertexState(); // The graph has 'picking' support.
                // So we can just add them to the picked list,
                // and let pickingGraphMousePlugin deal with it
                processModelVertexes.get(automataName).stream()
                        .filter(v -> v != graphNode)
                        .forEach(v -> pickedVertexState.pick(v, true));

            }

            startTime = -1;
        } else
            startTime = -1;

    }

    @Override
    public void graphPressed(GraphNode graphNode, MouseEvent me) {

    }

    @Override
    public void graphReleased(GraphNode graphNode, MouseEvent me) {

    }

    public void updateProcessModelList(Multimap<String, GraphNode> processModels) {
        processModelVertexes = processModels;
    }
}
