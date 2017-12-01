package mc.client;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.layout.orthogonal.mxOrthogonalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import lombok.Getter;
import lombok.SneakyThrows;
import mc.compiler.CompilationObject;
import mc.compiler.CompilationObservable;
import mc.process_models.ProcessModel;
import mc.process_models.automata.Automaton;

import java.util.*;
import java.util.List;


/**
 * Created by bealjaco on 29/11/17.
 */
public class ModelView implements Observer{
    private mxGraphComponent graphComponent;

    private Set<String> displayedAutomata;

    private CompilationObject compiledResult;

    private Map<String,Object> nodeMap = new HashMap<>();
    private List<String> rootNodes = new ArrayList<>();


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


        compiledResult = (CompilationObject) arg;
    }

    public mxGraphComponent updateGraph() {
        mxGraph graph = new mxGraph();
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
        layout(graph);
        graphComponent.setGraph(graph);

        return graphComponent;
    }

    private void addAutomata(Automaton automata,mxGraph graph){
        Object parent = graph.getDefaultParent();
        nodeMap = new HashMap<>();
        automata.getNodes().forEach(n -> {
            mxCell gNode = (mxCell) graph.insertVertex(parent,n.getId(),n.getId(),100, 100, 20, 20);
            nodeMap.put(n.getId(),gNode);
            if (n.hasMetaData("startNode"))
                rootNodes.add(n.getId());
        });
        automata.getEdges().forEach(e -> {
            Object to = nodeMap.get(e.getTo().getId());
            Object from = nodeMap.get(e.getFrom().getId());
            graph.insertEdge(parent,e.getId(),e.getLabel(),from,to);
        });
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
        graph.setCellsEditable(false);
        graph.setAllowDanglingEdges(false);
        graph.setAllowNegativeCoordinates(false);
        graph.setAllowLoops(false);
        graph.setCellsDeletable(false);
        graph.setCellsCloneable(false);
        graph.setCellsDisconnectable(false);
        graph.setDropEnabled(false);
        graph.setSplitEnabled(false);
        graph.setCellsBendable(false);
        graph.setCellsResizable(false);
        graph.setEnabled(false);
        graphComponent.setConnectable(false);
        graphComponent.setPanning(true);


        mxGraphLayout layout;
        try {
            layout = new mxHierarchicalLayout(graph);

            layout.execute(graph.getDefaultParent());
        } catch(Throwable ignored) {
            layout = new mxOrthogonalLayout(graph);
            layout.execute(graph.getDefaultParent());
        }
    }




    private static ModelView instance;
    /**
     * Enforcing Singleton
     */
    //TODO REMOVE THIS!!!
    @SneakyThrows
    private ModelView(){
        CompilationObservable.getInstance().addObserver(this);
        graphComponent = new mxGraphComponent(new mxGraph());
        displayedAutomata = new LinkedHashSet<>();
    }

    public static ModelView getInstance(){
        if(instance == null)
            instance = new ModelView();
        return instance;
    }

}
