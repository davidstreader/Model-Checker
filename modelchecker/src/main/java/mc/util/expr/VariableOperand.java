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

    public boolean equals(Object obj){
        if(obj == this){
            return true;
        }
        if(obj == null){
            return false;
        }
        if(obj instanceof VariableOperand){
            VariableOperand op = (VariableOperand)obj;
            return variable.equals(op.getValue());
        }

        return false;
    }
}
