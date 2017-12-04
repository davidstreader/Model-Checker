package mc.client;

import edu.uci.ics.jung.algorithms.layout.DAGLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.*;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Bounds;
import lombok.Getter;
import lombok.Setter;
import mc.client.graph.DirectedEdge;
import mc.client.graph.GraphNode;
import mc.compiler.CompilationObject;
import mc.compiler.CompilationObservable;
import mc.compiler.OperationResult;
import mc.process_models.ProcessModel;
import mc.process_models.automata.Automaton;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by bealjaco on 29/11/17.
 */
public class ModelView implements Observer{

    private Graph<GraphNode,DirectedEdge> graph;
    private VisualizationViewer<GraphNode,DirectedEdge> graphView;

    private Set<String> automataToDisplay;

    private CompilationObject compiledResult;

    private static final Font sourceCodePro;

    @Setter
    private Consumer<Collection<String>> listOfAutomataUpdater;
    @Setter
    private BiConsumer<List<OperationResult>,List<OperationResult>> updateLog;



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

        compiledResult = (CompilationObject) arg;
        listOfAutomataUpdater.accept(compiledResult.getProcessMap().keySet());
        updateLog.accept(compiledResult.getOperationResults(),compiledResult.getEquationResults());
    }

    /**
     * A method to update the graph that is displayed
     * @return the graph component that is displayed
     */
    public VisualizationViewer<GraphNode,DirectedEdge> updateGraph(SwingNode s) {
        graph = new DirectedSparseMultigraph<>();
        if(compiledResult == null)
            return new VisualizationViewer<>(new DAGLayout<>(new DirectedSparseGraph<>()));
        compiledResult.getProcessMap().keySet().stream()
                .filter(automataToDisplay::contains)
                .map(compiledResult.getProcessMap()::get)
                .filter(Objects::nonNull)
                .forEach(this::addProcess);


        //apply a layout to the graph
        Layout<GraphNode,DirectedEdge> layout = new FRLayout<>(graph);

        VisualizationViewer<GraphNode,DirectedEdge> vv = new VisualizationViewer<>(layout);

        PluggableGraphMouse gm = new PluggableGraphMouse();
        gm.add(new TranslatingGraphMousePlugin(MouseEvent.BUTTON2_MASK));
        gm.add(new TranslatingGraphMousePlugin(MouseEvent.BUTTON3_MASK));
        gm.add(new ScalingGraphMousePlugin(new CrossoverScalingControl(),0,1.1f,0.9f));
        gm.add(new PickingGraphMousePlugin<>());
        vv.setGraphMouse(gm);

        vv.getRenderContext().setVertexLabelTransformer(GraphNode::getNodeId);

        if(sourceCodePro!=null) {
            vv.getRenderContext().setEdgeFontTransformer(e -> sourceCodePro);
            vv.getRenderContext().setVertexFontTransformer(e -> sourceCodePro);
        }

        vv.getRenderContext().setEdgeLabelTransformer(DirectedEdge::getLabel);
        vv.getRenderContext().setVertexFillPaintTransformer(node -> {
            if(node.getNodeTermination().equals("START"))
                return Color.GREEN;
            if (node.getNodeTermination().equals("STOP"))
                return Color.CYAN;
            if(node.getNodeTermination().equals("ERROR"))
                return Color.RED;
            return Color.LIGHT_GRAY;
        });

        Bounds b = s.getBoundsInParent();
        vv.setPreferredSize(new Dimension((int)b.getWidth(),(int)b.getHeight()));
        return vv;
    }

    private void addProcess(ProcessModel p){
        switch (p.getProcessType()){
            case AUTOMATA:
                addAutomata((Automaton)p);
                break;
            case PETRINET:
                //TODO
                break;
        }
    }
    /**
     * Add an individual automata to the graph
     * @param automata the automata object
     */
    private void addAutomata(Automaton automata){
        //make a new "parent" object for the children to be parents of
        Map<String,GraphNode> nodeMap = new HashMap<>();

        //add all the nodes to the graph
        automata.getNodes().forEach(n -> {
            String nodeTermination = "";
            if(n.getId().equals(automata.getRootId()))
                nodeTermination = "START";
            if(n.hasMetaData("isTerminal"))
                nodeTermination = (String) n.getMetaData("isTerminal");

            GraphNode node = new GraphNode(automata.getId(),n.getId(),nodeTermination);
            nodeMap.put(n.getId(),node);
            graph.addVertex(node);
        });

        //add the edges to the graph
        automata.getEdges().forEach(e -> {
            GraphNode to   = nodeMap.get(e.getTo().getId());
            GraphNode from = nodeMap.get(e.getFrom().getId());
            graph.addEdge(new DirectedEdge(e.getLabel(),UUID.randomUUID().toString()),from,to);
        });

    }


    public void addDisplayedAutomata(String modelLabel) {
        assert compiledResult.getProcessMap().containsKey(modelLabel);
        automataToDisplay.add(modelLabel);
    }

    public void clearDisplayed() {
        automataToDisplay.clear();
    }

    public void addAllAutomata() {
        automataToDisplay.addAll(getProcessMap().keySet());
    }

    public Map<String, ProcessModel> getProcessMap() {
        return  compiledResult.getProcessMap();
    }

    @Getter
    private static ModelView instance = new ModelView();

    /**
     * Enforcing Singleton
     */
    private ModelView(){
        CompilationObservable.getInstance().addObserver(this);
        automataToDisplay = new HashSet<>();
    }

    //register font
    static {
        Font source;
        try {
            source = Font.createFont(Font.TRUETYPE_FONT,
                    ModelView.class.getResourceAsStream("/clientres/SourceCodePro-Bold.ttf"))
                    .deriveFont(15f);
        } catch (FontFormatException | IOException e) {
            source = null;
        }
        sourceCodePro = source;
    }
}
