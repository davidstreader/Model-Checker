package mc.compiler;

import mc.compiler.ast.OperationNode;
import mc.exceptions.CompilationException;
import mc.util.Location;

import java.util.List;

/**
 * For holding the output of operations in the operation block. Eg
 * operation {
 * P1 ~ P2.
 * P3 # P4.
 * }
 */
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
    //System.out.println("OperationResult "+ failures+" "+ op.myString()+ " "+result);
    //this.process1 = new OperationProcess(ident1, process1.getLocation());
    //this.process2 = new OperationProcess(ident2, process2.getLocation());
    //this.operation = (negated ? "!" : "") + operation;
    this.result = result + "";
    res = result;
    this.extra = extra;
    this.failures = failedOps;
    this.op = op;

  }

  public OperationNode getOp() {
    return this.op;
  }

  public String getResult() {
    return this.result;
  }

  public boolean isRes() {
    return this.res;
  }

  public String getExtra() {
    return this.extra;
  }

  public List<String> getFailures() {
    return this.failures;
  }

  public static class OperationProcess {
    private String ident;
    private Location location;

    public OperationProcess(String ident, Location location) {
      this.ident = ident;
      this.location = location;
    }

    public String getIdent() {
      return this.ident;
    }

    public Location getLocation() {
      return this.location;
    }
  }

  public String myString() {
    return "OperationResult "+result+" "+res+" extra "+extra+" failures "+failures;
  }
}
