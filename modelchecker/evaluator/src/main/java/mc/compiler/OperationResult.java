package mc.compiler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mc.compiler.ast.ASTNode;
import mc.exceptions.CompilationException;
import mc.util.Location;

import java.util.ArrayList;

/**
 *  For holding the output of operations in the operation block. Eg
 *  operation {
 *      P1 ~ P2.
 *      P3 # P4.
 *  }
 */
@Getter
public class OperationResult {

    private OperationProcess process1;
    private OperationProcess process2;
    private String operation;
    private String result;
    private String extra;
    private ArrayList<String> failures;
    public OperationResult(ASTNode process1, ASTNode process2, String ident1, String ident2, String operation, ArrayList<String> failedOps, boolean negated, boolean result, String extra) throws CompilationException {
        this.process1 = new OperationProcess(ident1, process1.getLocation());
        this.process2 = new OperationProcess(ident2, process2.getLocation());
        this.operation = (negated ? "!" : "")+getOpSymbol(operation);
        this.result = result+"";
        this.extra = extra;
        this.failures = failedOps;
    }

    public static String getOpSymbol(String op) throws CompilationException {
        switch (op) {
            case "bisimulation": return "~";
            case "traceEquivalent": return "#";
        }
        throw new CompilationException(OperationEvaluator.class,"Unknown operation: "+op);
    }
    @AllArgsConstructor
    @Getter
    public static class OperationProcess {
        private String ident;
        private Location location;
    }
}
