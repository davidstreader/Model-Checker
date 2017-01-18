package mc.util.expr;

public class IntegerOperand extends Operand {

	// fields
	private int integer;

	public IntegerOperand(int integer){
		this.integer = integer;
	}

	public int getValue(){
		return integer;
	}
}
