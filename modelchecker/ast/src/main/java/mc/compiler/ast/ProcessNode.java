package mc.compiler.ast;

import lombok.ToString;
import mc.util.Location;

import java.util.List;
@ToString
public class ProcessNode extends ASTNode {

	// fields
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

	public String getType(){
		return type;
	}

	public void setType(String type){
		this.type = type;
	}

	public String getIdentifier(){
		return identifier;
	}

	public void setIdentifier(String identifier){
		this.identifier = identifier;
	}

	public ASTNode getProcess(){
		return process;
	}

	public void setProcess(ASTNode process){
		this.process = process;
	}

	public List<LocalProcessNode> getLocalProcesses(){
		return localProcesses;
	}

	public void setLocalProcesses(List<LocalProcessNode> localProcesses){
		this.localProcesses = localProcesses;
	}

    public RelabelNode getRelabels(){
        return relabels;
    }

    public void setRelabels(RelabelNode relabels){
        this.relabels = relabels;
    }

    public boolean hasRelabels(){
        return relabels != null;
    }

	public HidingNode getHiding(){
		return hiding;
	}

    public void setHiding(HidingNode hiding){
        this.hiding = hiding;
    }

	public boolean hasHiding(){
		return hiding != null;
	}

    public VariableSetNode getVariables(){
        return variables;
    }

    public void setVariables(VariableSetNode variables){
        this.variables = variables;
    }

    public boolean hasVariableSet(){
        return variables != null;
    }

    public InterruptNode getInterrupt(){
        return interrupt;
    }

    public void setInterrupt(InterruptNode interrupt){
        this.interrupt = interrupt;
    }

    public boolean hasInterrupt(){
        return interrupt != null;
    }

    public boolean equals(Object obj){
        boolean result = super.equals(obj);
        if(!result){
            return false;
        }
        if(obj == this){
            return true;
        }
        if(obj instanceof ProcessNode){
            ProcessNode node = (ProcessNode)obj;
            if(!type.equals(node.getType())){
                return false;
            }
            if(!identifier.equals(node.getIdentifier())){
                return false;
            }
            if(!process.equals(node.getProcess())){
                return false;
            }
            if(!localProcesses.equals(node.getLocalProcesses())){
                return false;
            }
            if(hasHiding() && !hiding.equals(node.getHiding())){
                return false;
            }
            if(hasVariableSet() && !variables.equals(node.getVariables())){
                return false;
            }
            return !hasInterrupt() || interrupt.equals(node.getInterrupt());
        }

        return false;
    }
}
