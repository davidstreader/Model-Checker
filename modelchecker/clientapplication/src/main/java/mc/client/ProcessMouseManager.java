package mc.client;

import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.util.MouseManager;

import java.awt.event.MouseEvent;

public class ProcessMouseManager implements MouseManager {

    /**
     * The view this manager operates upon.
     */
    protected View view;


    /**
     * The graph to modify according to the view actions.
     */
    protected GraphicGraph graph;

    private MultiGraph workingCanvasArea;

    @Override
    public void init(GraphicGraph graphicGraph, View view) {
        this.graph = graphicGraph;
        this.view = view;

    }

    public void setGraph(MultiGraph mg){
        workingCanvasArea = mg;
    }

    @Override
    public void release() {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        ModelView.getInstance().determineIfNodeClicked(e.getX(), e.getY());
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        //System.out.println("Mouse release");
        //ModelView.getInstance().determineIfNodeReleasedOn(e.getX(), e.getY());
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        //MouseEnter event triggered from robot usage in ModelView not ideal but functional
        //i.e graph detects mouse enter after robot forces a click at the last position of the added shape
       ModelView.getInstance().dropNode(e.getX(), e.getY());
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }
}
