package mc.compiler.ast;

import mc.util.Location;

/**
 * Created by sheriddavi on 1/02/17.
 */
public class ProcessRootNode extends ASTNode {

    // fields
    private ASTNode process;
    private String label;
    private RelabelNode relabels;
    private HidingNode hiding;

    public ProcessRootNode(ASTNode process, String label, RelabelNode relabels, HidingNode hiding, Location location){
        super(location);
        this.process = process;
        this.label = label;
        this.relabels = relabels;
        this.hiding = hiding;
    }

    public ASTNode getProcess(){
        return process;
    }

    public String getLabel(){
        return label;
    }

    public boolean hasLabel(){
        return label != null;
    }

    public RelabelNode getRelabels(){
        return relabels;
    }

    public boolean hasRelabels(){
        return relabels != null;
    }

    public HidingNode getHiding(){
        return hiding;
    }

    public boolean hasHiding(){
        return hiding != null;
    }

    public boolean equals(Object obj){
        if(obj == this){
            return true;
        }
        if(obj == null){
            return false;
        }
        if(obj instanceof ProcessRootNode){
            ProcessRootNode node = (ProcessRootNode)obj;
            if(!process.equals(node.getProcess())){
                return false;
            }
            if(hasLabel() != node.hasLabel()){
                return false;
            }
            if(hasLabel() && node.hasLabel() && !label.equals(node.getLabel())){
                return false;
            }
            if(hasRelabel() != node.hasRelabel()){
                return false;
            }
            if(hasRelabel() && node.hasRelabel() && !relabels.equals(node.getRelabels())){
                return false;
            }
            if(hasHiding() != node.hasHiding()){
                return false;
            }
            if(hasHiding() && node.hasHiding() && !hiding.equals(node.getHiding())){
                return false;
            }

            return true;
        }

        return false;
    }

}
