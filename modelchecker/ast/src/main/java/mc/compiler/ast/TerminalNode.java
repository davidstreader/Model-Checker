package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

@Data
@EqualsAndHashCode(callSuper = true)
public class TerminalNode extends ASTNode {

	private final String terminal;

	public TerminalNode(String terminal, Location location){
		super(location);
		this.terminal = terminal;
	}
}
