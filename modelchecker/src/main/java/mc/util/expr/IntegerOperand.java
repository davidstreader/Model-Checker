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

    public boolean equals(Object obj){
        if(obj == this){
            return true;
        }
        if(obj == null){
            return false;
        }
        if(obj instanceof IntegerOperand){
            IntegerOperand op = (IntegerOperand)obj;
            return integer == op.getValue();
        }

        return false;
    }
}
