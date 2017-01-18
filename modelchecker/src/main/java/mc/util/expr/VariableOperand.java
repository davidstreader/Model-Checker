package mc.util.expr;

public class VariableOperand extends Operand {

	// fields
	private String variable;

	public VariableOperand(String variable){
		this.variable = variable;
	}

	public String getValue(){
		return variable;
	}
}
