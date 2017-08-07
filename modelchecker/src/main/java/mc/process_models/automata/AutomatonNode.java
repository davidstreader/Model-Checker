package mc.process_models.automata;

import mc.process_models.ProcessModelObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by sheriddavi on 24/01/17.
 */
public class AutomatonNode extends ProcessModelObject {

    // fields
    private String label;
    private Map<String, AutomatonEdge> incomingEdges;
    private Map<String, AutomatonEdge> outgoingEdges;

    public AutomatonNode(String id){
        super(id,"node");
        this.label = null;
        incomingEdges = new HashMap<>();
        outgoingEdges = new HashMap<>();
    }

    public String getLabel(){
        return label;
    }

    public void setLabel(String label){
        this.label = label;
    }

    public List<AutomatonEdge> getIncomingEdges(){
        return new ArrayList<>(incomingEdges.values());
    }

    public boolean addIncomingEdge(AutomatonEdge edge){
        if(!incomingEdges.containsKey(edge.getId())){
            incomingEdges.put(edge.getId(), edge);
            return true;
        }

        return false;
    }

    public boolean removeIncomingEdge(AutomatonEdge edge){
        if(incomingEdges.containsKey(edge.getId())){
            incomingEdges.remove(edge.getId());
            return true;
        }

        return false;
    }

    public List<AutomatonEdge> getOutgoingEdges(){
        return new ArrayList<>(outgoingEdges.values());
    }

    public boolean addOutgoingEdge(AutomatonEdge edge){
        if(!outgoingEdges.containsKey(edge.getId())){
            outgoingEdges.put(edge.getId(), edge);
            return true;
        }

        return false;
    }

    public boolean removeOutgoingEdge(AutomatonEdge edge){
        if(outgoingEdges.containsKey(edge.getId())){
            outgoingEdges.remove(edge.getId());
            return true;
        }

        return false;
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        List<AutomatonEdge> incoming = getIncomingEdges();

        builder.append("node{\n");
        builder.append("\tid:").append(getId()).append("\n");
        builder.append("\tincoming:{");
        for(int i = 0; i < incoming.size(); i++){
            builder.append(incoming.get(i).getId());
            if(i < incoming.size() - 1){
                builder.append(", ");
            }
        }
        builder.append("}\n");

        builder.append("\toutgoing:{");
        List<AutomatonEdge> outgoing = getOutgoingEdges();
        for(int i = 0; i < outgoing.size(); i++){
            builder.append(outgoing.get(i).getId());
            if(i < outgoing.size() - 1){
                builder.append(", ");
            }
        }
        builder.append("}\n}");

        return builder.toString();
    }
}
