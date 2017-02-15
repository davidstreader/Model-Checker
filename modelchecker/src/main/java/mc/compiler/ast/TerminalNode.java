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

    public boolean equals(Object obj){
        boolean result = super.equals(obj);
        if(!result){
            return false;
        }
        if(obj == this){
            return true;
        }
        if(obj == null){
            return false;
        }
        if(obj instanceof TerminalNode){
            TerminalNode node = (TerminalNode)obj;
            return terminal.equals(node.getTerminal());
        }

        return false;
    }
}
