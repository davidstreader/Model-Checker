package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProcessNode extends ASTNode {

	private String type;
	private String identifier;
	private ASTNode process;
	private List<LocalProcessNode> localProcesses;
    private RelabelNode relabels;
	private HidingNode hiding;
    private VariableSetNode variables;
    private InterruptNode interrupt;

    public ProcessNode(String type, String identifier, ASTNode process, List<LocalProcessNode> localProcesses, Location location){
        super(location);
        this.type = type;
        this.identifier = identifier;
        this.process = process;
        this.localProcesses = localProcesses;
        hiding = null;
        variables = null;
        interrupt = null;
    }

	public ProcessNode(String type, String identifier, ASTNode process, List<LocalProcessNode> localProcesses, HidingNode hiding, Location location){
		super(location);
		this.type = type;
		this.identifier = identifier;
		this.process = process;
		this.localProcesses = localProcesses;
		this.hiding = hiding;
        variables = null;
        interrupt = null;
	}

    public boolean hasRelabels(){
        return relabels != null;
    }

	public boolean hasHiding(){
		return hiding != null;
	}

    public boolean hasVariableSet(){
        return variables != null;
    }
}
