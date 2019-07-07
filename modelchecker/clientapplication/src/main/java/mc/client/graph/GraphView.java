package mc.client.graph;

import mc.processmodels.MultiProcessModel;
import mc.processmodels.ProcessModelObject;
import mc.processmodels.ProcessModel;
import mc.processmodels.ProcessType;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetEdge;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.*;


public class GraphView extends JComponent {


    private Map<ProcessModelObject, GraphViewNode> nodes  = new TreeMap<>();
    private Map<ProcessModelObject, GraphViewEdge> edges  = new TreeMap<>();
    Random random;

    public GraphView(ProcessModel pm) {

        if (pm instanceof  Petrinet) {
            new GraphView((Petrinet) pm);
        } else  if (pm instanceof  Automaton) {
            new GraphView((Automaton) pm);
        } else if (pm instanceof MultiProcessModel) {
            new GraphView( ((MultiProcessModel) pm).getProcess(ProcessType.PETRINET));
        }
    }
    public GraphView(Automaton a){
        random = new Random(a.hashCode());
         for(AutomatonNode n: a.getNodes()) {
             nodes.put(n,new GraphViewNode(0,0));
         }

         for (AutomatonEdge ed: a.getEdges()){
             edges.put(ed,new GraphViewEdge(nodes.get(ed.getFrom()),nodes.get(ed.getTo())));
         }
    }
    public GraphView(Petrinet pn){
        random = new Random(pn.hashCode());
        for(PetriNetPlace p: pn.getPlaces().values()) {
            nodes.put(p,new GraphViewNode(0,0));
        }
        for(PetriNetTransition tr: pn.getTransitions().values()) {
            nodes.put(tr,new GraphViewNode(0,0));
        }

        for (PetriNetEdge ed: pn.getEdges().values()){
            edges.put(ed,new GraphViewEdge(nodes.get(ed.getFrom()),nodes.get(ed.getTo())));
        }
    }

    public void randomLocations(int size) {
        for(GraphViewNode n: nodes.values()){
            n.setX(size*random.nextDouble());
            n.setY(size*random.nextDouble());
        }
    }

    public void display(Graphics g_) {
        Graphics2D g = (Graphics2D) g_;
        for(ProcessModelObject pt: nodes.keySet()){
            if(pt instanceof  PetriNetTransition) {
                double d = 40;
                Shape rect = new RoundRectangle2D.Double(
                        nodes.get(pt).getX() - d,
                        nodes.get(pt).getY() - d,
                         2 * d,
                         2 * d,
                        d, d);
                g.setColor(Color.decode("#808080"));
                g.fill(rect);
                g.setColor(Color.GREEN);
                g.draw(rect);
            }
        }
    }
}
