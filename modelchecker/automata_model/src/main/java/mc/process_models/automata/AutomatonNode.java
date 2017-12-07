package mc.process_models.automata;

import lombok.Getter;
import lombok.Setter;
import mc.compiler.Guard;
import mc.compiler.ast.ASTNode;
import mc.process_models.ProcessModelObject;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by sheriddavi on 24/01/17.
 */
public class AutomatonNode extends ProcessModelObject {

    // fields

    private Map<String, AutomatonEdge> incomingEdges;
    private Map<String, AutomatonEdge> outgoingEdges;

    @Getter
    @Setter
    private String label;

    @Getter
    @Setter
    private boolean startNode;

    @Getter
    @Setter
    private String terminal;

    @Getter
    @Setter
    private int colour;

    @Getter
    @Setter
    private Set<String> references;

    @Getter
    @Setter
    private int labelNumber;

    @Getter
    @Setter
    private Guard guard;

    @Getter
    @Setter
    private Map<String, Object> variables = new HashMap<String, Object>();

    public AutomatonNode(String id){
        super(id,"node");
        this.label = null;
        incomingEdges = new HashMap<>();
        outgoingEdges = new HashMap<>();
    }



    public void copyProperties(AutomatonNode fromThisNode) {
        this.terminal = fromThisNode.getTerminal();
        this.startNode = fromThisNode.isStartNode();
        this.colour = fromThisNode.getColour();
        this.references = fromThisNode.getReferences();
        this.labelNumber = fromThisNode.getLabelNumber();
        this.guard = fromThisNode.getGuard();
        this.variables = fromThisNode.getVariables();
    }

    public void copyPropertiesFromASTNode(ASTNode fromThisNode) {
        if(fromThisNode.getModelVariables() != null)
            this.variables = fromThisNode.getModelVariables();


        this.guard = (Guard)fromThisNode.getGuard();
    }

    /**
     *  Creates an intersection between two nodes,
     * @param withThisNode The second node to intersect with
     * @return A node that is the combined result of the intersection
     */

    public AutomatonNode createIntersection(AutomatonNode withThisNode) {
        AutomatonNode newNode = new AutomatonNode("");

        if(this.terminal != null && withThisNode.getTerminal() != null)
            if(this.terminal.equals(withThisNode.getTerminal()))
                newNode.setTerminal(this.terminal);

        if(this.startNode == withThisNode.isStartNode())
            newNode.setStartNode(this.startNode);

        if(this.colour == withThisNode.getColour())
            newNode.setColour(this.colour);

        if(this.references != null && withThisNode.getReferences() != null)
            if(this.references.equals(withThisNode.getReferences()))
                newNode.setReferences(this.references);

        if(this.labelNumber == withThisNode.getLabelNumber())
            newNode.setLabelNumber(this.labelNumber);

        if(this.guard != null && withThisNode.getGuard() != null)
            if(this.guard.equals(withThisNode.getGuard()))
                newNode.setGuard(this.guard);

        return newNode;
    }

    public boolean isTerminal() {
        return terminal != null && terminal.length() > 0;
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
