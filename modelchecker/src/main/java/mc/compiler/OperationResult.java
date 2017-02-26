package mc.compiler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mc.compiler.ast.ASTNode;
import mc.exceptions.CompilationException;
import mc.process_models.automata.Automaton;
import mc.util.Location;

import java.util.List;

@Getter
public class OperationResult {
    private OperationProcess process1;
    private OperationProcess process2;
    private String operation;
    private String result;
    private List<Automaton> test;
    public OperationResult(ASTNode process1, ASTNode process2, String ident1, String ident2, String operation, boolean negated, boolean result, List<Automaton> test) throws CompilationException {
        this.process1 = new OperationProcess(ident1, process1.getLocation());
        this.process2 = new OperationProcess(ident2, process2.getLocation());
        this.operation = (negated?"!":"")+getOpSymbol(operation);
        this.result = result+"";
        this.test = test;
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
        private Location location;
    }
}
