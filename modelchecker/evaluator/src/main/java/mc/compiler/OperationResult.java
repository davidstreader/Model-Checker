package mc.compiler;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import mc.compiler.ast.ASTNode;
import mc.compiler.ast.OperationNode;
import mc.exceptions.CompilationException;
import mc.util.Location;

/**
 * For holding the output of operations in the operation block. Eg
 * operation {
 * P1 ~ P2.
 * P3 # P4.
 * }
 */
@Getter
public class OperationResult extends Result {

  private OperationNode op;
  //private OperationProcess process1;
  //private OperationProcess process2;
  //private String operation;
  private String result;
  private boolean res;
  private String extra;
  private List<String> failures;

  public OperationResult(//ASTNode process1, ASTNode process2, String ident1, String ident2,
                         //String operation,
                         List<String> failedOps,
                         boolean result, String extra,OperationNode op)
    throws CompilationException {
    System.out.println("OperationResult "+ failures+" "+ op.myString()+ " "+result);
    //this.process1 = new OperationProcess(ident1, process1.getLocation());
    //this.process2 = new OperationProcess(ident2, process2.getLocation());
    //this.operation = (negated ? "!" : "") + operation;
    this.result = result + "";
    res = result;
    this.extra = extra;
    this.failures = failedOps;
    this.op = op;

  }

  @AllArgsConstructor
  @Getter
  public static class OperationProcess {
    private String ident;
    private Location location;
  }
}
