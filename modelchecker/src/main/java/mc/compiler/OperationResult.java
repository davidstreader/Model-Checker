package mc.compiler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mc.compiler.ast.ASTNode;
import mc.compiler.ast.FunctionNode;
import mc.compiler.ast.IdentifierNode;
import mc.exceptions.CompilationException;
import mc.util.Location;

@Getter
public class OperationResult {
  private OperationProcess process1;
  private OperationProcess process2;
  private String operation;
  private String result;

  public OperationResult(ASTNode process1, ASTNode process2, String ident1, String ident2, String operation, boolean negated, boolean result) throws CompilationException {
    this.process1 = new OperationProcess(ident1, true, process1.getLocation());
    this.process2 = new OperationProcess(ident2, true, process2.getLocation());
    this.operation = (negated?"!":"")+getOpSymbol(operation);
    this.result = result+"";
  }

  public OperationResult(ASTNode process1, ASTNode process2, String ident1, String ident2, String operation, boolean negated, boolean firstFound, boolean secondFound) throws CompilationException {
    this.process1 = new OperationProcess(ident1, firstFound, process1.getLocation());
    this.process2 = new OperationProcess(ident2, secondFound, process2.getLocation());
    this.operation = (negated?"!":"")+getOpSymbol(operation);
    this.result = "notfound";
  }

  private String getOpSymbol(String op) throws CompilationException {
    switch (op) {
      case "bisimulation": return "~";
      case "traceEquivalent": return "#";
    }
    throw new CompilationException(OperationEvaluator.class,"Unknown operation: "+op);
  }
  @AllArgsConstructor
  @Getter
  public class OperationProcess {
    private String ident;
    private boolean exists;
    private Location location;
  }
}
