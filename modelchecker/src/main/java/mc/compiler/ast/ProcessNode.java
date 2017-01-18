package mc.compiler.ast;

import java.util.List;

import mc.util.Location;

public class ProcessNode extends ASTNode {

	// fields
	private String type;
	private String identifier;
	private ASTNode process;
	private List<LocalProcessNode> localProcesses;

	public ProcessNode(String type, String identifier, ASTNode process, List<LocalProcessNode> localProcesses, Location location){
		super(location);
		this.type = type;
		this.identifier = identifier;
		this.process = process;
		this.localProcesses = localProcesses;
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
}
