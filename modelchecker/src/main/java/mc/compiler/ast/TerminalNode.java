package mc.compiler.ast;

import mc.util.Location;

public class TerminalNode extends ASTNode {

	// fields
	private String terminal;

	public TerminalNode(String terminal, Location location){
		super(location);
		this.terminal = terminal;
	}

	public String getTerminal(){
		return terminal;
	}
}
