package mc.compiler;

import lombok.Getter;

@Getter
public class ImpliesResult extends Result {
  private boolean res = false; // op1 ==> op2
  private OperationResult op1;
  private OperationResult op2;

  public ImpliesResult(OperationResult o1,OperationResult o2) {
    op1= o1;
    op2 = o2;
    res = (! op1.isRes() || op2.isRes());

  }

}
