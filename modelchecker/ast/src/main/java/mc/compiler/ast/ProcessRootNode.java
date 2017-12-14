package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProcessRootNode extends ASTNode {

    private ASTNode process;
    private String label;
    private RelabelNode relabelSet;
    private HidingNode hiding;

    public ProcessRootNode(ASTNode process, String label, RelabelNode relabels, HidingNode hiding, Location location){
        super(location);
        this.process = process;
        this.label = label;
        this.relabelSet = relabels;
        this.hiding = hiding;
    }

    public boolean hasLabel(){
        return label != null;
    }

    public boolean hasRelabelSet(){
        return relabelSet != null;
    }

    public boolean hasHiding(){
        return hiding != null;
    }
}
