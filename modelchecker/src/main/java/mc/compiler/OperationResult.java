package mc.compiler;

import lombok.Getter;
import mc.compiler.ast.ASTNode;
import mc.compiler.ast.FunctionNode;
import mc.compiler.ast.IdentifierNode;

@Getter
public class OperationResult {
  private String process1;
  private String process2;
  private String operation;
  private boolean result;

  public OperationResult(ASTNode process1, ASTNode process2, String operation, boolean result) {
    this.process1 = getIdent(process1);
    this.process2 = getIdent(process2);
    this.operation = getOpSymbol(operation);
    this.result = result;
  }
  private String getOpSymbol(String op) {
    switch (op) {
      case "bisimulation": return "~";
      case "traceequivilant": return "#";
    }
    throw new UnsupportedOperationException("Unknown operation: "+op);
  }
  private String getIdent(ASTNode process) {
    if (process instanceof IdentifierNode) {
      return ((IdentifierNode) process).getIdentifier();
    } else if (process instanceof FunctionNode) {
      return getIdent(((FunctionNode) process).getProcess());
    }
    return null;
  }
}
