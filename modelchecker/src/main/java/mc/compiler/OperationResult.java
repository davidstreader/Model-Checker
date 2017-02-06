package mc.compiler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mc.compiler.ast.ASTNode;
import mc.compiler.ast.FunctionNode;
import mc.compiler.ast.IdentifierNode;
import mc.util.Location;

@Getter
public class OperationResult {
  private OperationProcess process1;
  private OperationProcess process2;
  private String operation;
  private String result;

  public OperationResult(ASTNode process1, ASTNode process2, String operation, boolean result) {
    this.process1 = new OperationProcess(getIdent(process1), true, process1.getLocation());
    this.process2 = new OperationProcess(getIdent(process2), true, process2.getLocation());
    this.operation = getOpSymbol(operation);
    this.result = result+"";
  }

  public OperationResult(ASTNode process1, ASTNode process2, String operation, boolean firstFound, boolean secondFound) {
    this.process1 = new OperationProcess(getIdent(process1), firstFound, process1.getLocation());
    this.process2 = new OperationProcess(getIdent(process2), secondFound, process2.getLocation());
    this.operation = getOpSymbol(operation);
    this.result = "notfound";
  }

  private String getOpSymbol(String op) {
    switch (op) {
      case "bisimulation": return "~";
      case "traceequivilant": return "#";
    }
    throw new UnsupportedOperationException("Unknown operation: "+op);
  }
  public static String getIdent(ASTNode process) {
    if (process instanceof IdentifierNode) {
      return ((IdentifierNode) process).getIdentifier();
    } else if (process instanceof FunctionNode) {
      return getIdent(((FunctionNode) process).getProcess());
    }
    return null;
  }
  @AllArgsConstructor
  @Getter
  public class OperationProcess {
    private String ident;
    private boolean exists;
    private Location location;
  }
}
