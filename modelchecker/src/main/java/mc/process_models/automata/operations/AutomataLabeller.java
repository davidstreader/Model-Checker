package mc.process_models.automata.operations;

import mc.process_models.automata.Automaton;
import mc.process_models.automata.AutomatonEdge;
import mc.process_models.automata.AutomatonNode;

import java.util.List;

/**
 * Created by sheriddavi on 25/01/17.
 */
public class AutomataLabeller {

    public Automaton labelAutomaton(Automaton automaton, String label){
        Automaton labelled = new Automaton(label + ":" + automaton.getId());

        List<AutomatonNode> nodes = automaton.getNodes();
        for(AutomatonNode node : nodes){
            AutomatonNode newNode = labelled.addNode(label + ":" + node.getId());
            for(String key : node.getMetaDataKeys()){
                newNode.addMetaData(key, node.getMetaData(key));
            }
        }

        List<AutomatonEdge> edges = automaton.getEdges();
        for(AutomatonEdge edge : edges){
            AutomatonNode from = labelled.getNode(label + ":" + edge.getFrom().getId());
            AutomatonNode to = labelled.getNode(label + ":" + edge.getTo().getId());
            AutomatonEdge newEdge = labelled.addEdge(label + "." + edge.getLabel(), from, to);

            for(String key : edge.getMetaDataKeys()){
                newEdge.addMetaData(key, edge.getMetaData(key));
            }
        }

        for(String key : automaton.getMetaDataKeys()){
            labelled.addMetaData(key, automaton.getMetaData(key));
        }

        return labelled;
    }

}
