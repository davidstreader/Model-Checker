package mc.client;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.layout.orthogonal.mxOrthogonalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxGraph;
import lombok.Getter;
import lombok.SneakyThrows;
import mc.compiler.CompilationObject;
import mc.compiler.CompilationObservable;
import mc.compiler.Compiler;
import mc.process_models.automata.Automaton;
import mc.process_models.automata.AutomatonNode;
import mc.util.expr.Expression;
import mc.webserver.Context;
import mc.webserver.FakeContext;
import mc.webserver.NativesManager;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * Created by bealjaco on 29/11/17.
 */
public class ModelView implements Observer{
    @Getter
    private mxGraphComponent graphComponent;

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

        //reset state
        nodeMap.clear();
        rootNodes.clear();
        mxGraph graph = new mxGraph();



        CompilationObject compiled = (CompilationObject) arg;
        graph.getModel().beginUpdate();
        try {
            compiled.getProcessMap().values().forEach(process -> {
                switch (process.getProcessType()) {
                    case AUTOMATA:
                        addAutomata((Automaton) process, graph);
                        break;
                    case PETRINET:
                        //TODO: Petrinet display
                        break;
                }
            });
        } finally {
            graph.getModel().endUpdate();
        }
        layout(graph);
        graphComponent.setGraph(graph);
    }

    private void addAutomata(Automaton automata,mxGraph graph){

//        mxCell parent = (mxCell) graph.addCell(new mxCell(automata.getId()));
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
        Compiler comp = new Compiler();
        //hacky test
        update(null, comp.compile("automata { A = b -> c -> STOP." +
                "                             B = d -> c -> STOP." +
                "                             C = A || B.}", new Context(), Expression.mkCtx(),new LinkedBlockingQueue<>()));

    }

    public static ModelView getInstance(){
        if(instance == null)
            instance = new ModelView();
        return instance;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Hello World");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(ModelView.getInstance().graphComponent);
        frame.setSize(400,320);
        frame.setVisible(true);
    }
}
