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

    private void addAutomata(Automaton automata,mxGraph graph){
        mxCell parent = (mxCell) graph.insertVertex(graph.getDefaultParent(),automata.getId(),automata.getId(),0,0,100,100,"group");
        cellList.add(parent);
        nodeMap = new HashMap<>();
        automata.getNodes().forEach(n -> {
            mxCell gNode = (mxCell) graph.insertVertex(parent,n.getId(),n.getId(),100, 100, 40, 40, "vertex");
            nodeMap.put(n.getId(),gNode);
            if (n.isStartNode()) {
                rootNodes.add(n.getId());
                gNode.setStyle("vertex;start");
            }
            if(n.isTerminal()) {
                if(n.getTerminal().equals("STOP"))
                    gNode.setStyle("vertex;stop");
                if(n.getTerminal().equals("ERROR"))
                    gNode.setStyle("vertex;error");
            }
        });

        automata.getEdges().forEach(e -> {
            Object to = nodeMap.get(e.getTo().getId());
            Object from = nodeMap.get(e.getFrom().getId());
            graph.insertEdge(parent,e.getId(),e.getLabel(),from,to);
        });
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
        Hashtable<String, Object> nodeStyle = new Hashtable<>();
        nodeStyle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
        nodeStyle.put(mxConstants.STYLE_FILLCOLOR, "#ffffff");
        nodeStyle.put(mxConstants.STYLE_STROKECOLOR, "#000000");
        nodeStyle.put(mxConstants.STYLE_FONTCOLOR, "#000000");
        ss.putCellStyle("vertex", nodeStyle);


        Hashtable<String,Object> groupStyle = new Hashtable<>();
        groupStyle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
        groupStyle.put(mxConstants.STYLE_FILLCOLOR, "#666666");
        groupStyle.put(mxConstants.STYLE_FONTCOLOR, "#ffffff");
//        groupStyle.put(mxConstants.STYLE_LABEL_POSITION,mxConstants.ALIGN_TOP);
        groupStyle.put(mxConstants.STYLE_GLASS,1);
        ss.putCellStyle("group", groupStyle);

        /**
         * Styles for specific node types
         */
        Hashtable<String,Object> beginningStyle = new Hashtable<>();
        beginningStyle.put(mxConstants.STYLE_FILLCOLOR, "#00ff00");
        ss.putCellStyle("start", beginningStyle);
        Hashtable<String,Object> stopStyle = new Hashtable<>();
        stopStyle.put(mxConstants.STYLE_FILLCOLOR, "#0000ff");
        ss.putCellStyle("stop", stopStyle);
        Hashtable<String,Object> errorStyle = new Hashtable<>();
        errorStyle.put(mxConstants.STYLE_FILLCOLOR, "#ff0000");
        ss.putCellStyle("error", errorStyle);
    }


    @Getter
    private static ModelView instance = new ModelView();
    /**
     * Enforcing Singleton
     */
    //TODO REMOVE THIS!!!
    @SneakyThrows
    private ModelView(){
        CompilationObservable.getInstance().addObserver(this);
        graphComponent = new mxGraphComponent(new mxGraph());

        displayedAutomata = new LinkedHashSet<>();

        graphComponent.setConnectable(false);
        graphComponent.setPanning(true);

        //Compiler comp = new Compiler();

        //hacky test
       // update(null, comp.compile("automata { A = b -> c -> STOP." +
         //       "                             B = d -> c -> STOP." +
         //       "                             C = A || B.}", new Context(), Expression.mkCtx(),new LinkedBlockingQueue<>()));
    }
}
