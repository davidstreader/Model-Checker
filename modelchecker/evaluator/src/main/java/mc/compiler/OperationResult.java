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
  private String result;
  private boolean res;
  private String extra;
  private List<String> failures;

  public OperationResult(List<String> failedOps,
                         boolean result, String extra,OperationNode op)
    throws CompilationException {
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
