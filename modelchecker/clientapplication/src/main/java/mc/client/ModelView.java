package mc.client;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.layout.orthogonal.mxOrthogonalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;

import com.mxgraph.util.mxConstants;

import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;
import lombok.Getter;
import lombok.SneakyThrows;
import mc.compiler.CompilationObject;
import mc.compiler.CompilationObservable;
import mc.process_models.ProcessModel;
import mc.process_models.automata.Automaton;
import mc.util.expr.Expression;
import mc.webserver.Context;

import java.util.*;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import mc.compiler.Compiler;

/**
 * Created by bealjaco on 29/11/17.
 */
public class ModelView implements Observer{
    private mxGraphComponent graphComponent;

    private Set<String> displayedAutomata;

    private CompilationObject compiledResult;

    private Map<String,Object> nodeMap = new HashMap<>();
    private List<String> rootNodes = new ArrayList<>();
    private List<mxCell> cellList = new ArrayList<>();


    /**
     * This method is called whenever the observed object is changed. An
     * application calls an <tt>Observable</tt> object's
     * <code>notifyObservers</code> method to have all the object's
     * observers notified of the change.
     *
     * @param o   the observable object.
     * @param arg an argument passed to the <code>notifyObservers</code>
     */
    @Override
    @SuppressWarnings("unchecked")
    public void update(Observable o, Object arg) {
        if(!(arg instanceof CompilationObject))
            throw new IllegalArgumentException("arg object was not of type compilationObject");
        //reset state
        nodeMap.clear();
        rootNodes.clear();
        cellList.clear();


        compiledResult = (CompilationObject) arg;
    }

    /**
     * A method to update the graph that is displayed
     * @return the graph component that is displayed
     */
    public mxGraphComponent updateGraph() {
        mxGraph graph = new mxGraph();

        setupStyles(graph);

        graph.getModel().beginUpdate();
        try {

            displayedAutomata.forEach(processLabel -> {
                Map<String, ProcessModel> automataMap = this.getProcessMap();
                if(automataMap.containsKey(processLabel) && !nodeMap.containsKey(processLabel)) {
                    switch (automataMap.get(processLabel).getProcessType()) {
                        case AUTOMATA:
                            addAutomata((Automaton) automataMap.get(processLabel), graph);
                            break;
                        case PETRINET:
                            //TODO: Petrinet display
                            break;
                    }
                }
            });
        } finally {
            graph.getModel().endUpdate();
        }
        cellList.add((mxCell) graph.getDefaultParent());
        layout(graph);
        graphComponent.setGraph(graph);

        return graphComponent;
    }

    /**
     * Add an individual automata to the graph
     * @param automata the automata object
     * @param graph    the graph the automata is added to
     */
    private void addAutomata(Automaton automata,mxGraph graph){
        //make a new "parent" object for the children to be parents of
        mxCell parent = (mxCell) graph.insertVertex(graph.getDefaultParent(),automata.getId(),automata.getId(),0,0,100,100,"group");
        cellList.add(parent);
        nodeMap = new HashMap<>();

        //add all the nodes to the graph
        automata.getNodes().forEach(n -> {
            mxCell gNode = (mxCell) graph.insertVertex(parent,n.getId(),n.getId(),100, 100, 40, 40, "vertex");
            nodeMap.put(n.getId(),gNode);
            //apply specific stylings
            if (n.hasMetaData("startNode")) {
                rootNodes.add(n.getId());
                gNode.setStyle("vertex;start");
            }
            if(n.hasMetaData("isTerminal")) {
                if(Objects.equals(n.getMetaData("isTerminal"),"STOP"))
                    gNode.setStyle("vertex;stop");
                if(Objects.equals(n.getMetaData("isTerminal"),"ERROR"))
                    gNode.setStyle("vertex;error");
            }
        });

        //add the edges to the graph
        automata.getEdges().forEach(e -> {
            Object to = nodeMap.get(e.getTo().getId());
            Object from = nodeMap.get(e.getFrom().getId());
            graph.insertEdge(parent,e.getId(),e.getLabel(),from,to);
        });

        //officially "group" the cells
        graph.groupCells(parent,2,nodeMap.values().toArray());
    }


    public void addDisplayedAutomata(String modelLabel) {
        displayedAutomata.add(modelLabel);
    }

    public void clearDisplayed() {
        displayedAutomata.clear();
    }

    public void addAllAutomata() {
            displayedAutomata.addAll(this.getProcessMap().keySet());
    }

    public Map<String, ProcessModel> getProcessMap() {
        return  compiledResult.getProcessMap();
    }



    private void layout(mxGraph graph){
        graph.setAllowDanglingEdges(false);
        graph.setAllowLoops(false);
        graph.setAllowNegativeCoordinates(false);
        graph.setCellsBendable(false);
        graph.setCellsCloneable(false);
        graph.setCellsDeletable(false);
        graph.setCellsDisconnectable(false);
        graph.setCellsEditable(false);
        graph.setCellsResizable(false);
//        graph.setDropEnabled(false);
        graph.setEnabled(false);
        graph.setSplitEnabled(false);


        mxGraphLayout layout;
        try {
            layout = new mxHierarchicalLayout(graph);
            cellList.forEach(layout::execute);
        } catch(Throwable ignored) {
            layout = new mxOrthogonalLayout(graph);
            layout.execute(graph.getDefaultParent());
        }
    }

    private void setupStyles(mxGraph graph) {
        mxStylesheet ss = graph.getStylesheet();
        HashMap<String, Object> nodeStyle = new HashMap<>();
        nodeStyle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
        nodeStyle.put(mxConstants.STYLE_FILLCOLOR, "#ffffff");
        nodeStyle.put(mxConstants.STYLE_STROKECOLOR, "#000000");
        nodeStyle.put(mxConstants.STYLE_FONTCOLOR, "#000000");
        ss.putCellStyle("vertex", nodeStyle);


        HashMap<String,Object> groupStyle = new HashMap<>();
        groupStyle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
        groupStyle.put(mxConstants.STYLE_FILLCOLOR, "#666666");
        groupStyle.put(mxConstants.STYLE_FONTCOLOR, "#ffffff");
//        groupStyle.put(mxConstants.STYLE_LABEL_POSITION,mxConstants.ALIGN_TOP);
        groupStyle.put(mxConstants.STYLE_GLASS,1);
        ss.putCellStyle("group", groupStyle);

        /**
         * Styles for specific node types
         */
        HashMap<String,Object> beginningStyle = new HashMap<>();
        beginningStyle.put(mxConstants.STYLE_FILLCOLOR, "#00ff00");
        ss.putCellStyle("start", beginningStyle);
        HashMap<String,Object> stopStyle = new HashMap<>();
        stopStyle.put(mxConstants.STYLE_FILLCOLOR, "#0000ff");
        ss.putCellStyle("stop", stopStyle);
        HashMap<String,Object> errorStyle = new HashMap<>();
        errorStyle.put(mxConstants.STYLE_FILLCOLOR, "#ff0000");
        ss.putCellStyle("error", errorStyle);

        /**
         * Edge styles
         */
        Map<String,Object> edgeStyle = graph.getStylesheet().getDefaultEdgeStyle();
        edgeStyle.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_LOOP);
    }


    @Getter
    private static ModelView instance = new ModelView();

    /**
     * Enforcing Singleton
     */
    private ModelView(){
        CompilationObservable.getInstance().addObserver(this);
        graphComponent = new mxGraphComponent(new mxGraph());
        graphComponent.setConnectable(false);
        graphComponent.setPanning(true);

        displayedAutomata = new LinkedHashSet<>();
    }
}
