package mc.compiler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mc.compiler.ast.ASTNode;
import mc.exceptions.CompilationException;
import mc.process_models.automata.Automaton;
import mc.util.Location;
import org.fusesource.jansi.Ansi;

import java.util.List;

@Getter
public class OperationResult {
    private OperationProcess process1;
    private OperationProcess process2;
    private String operation;
    private String result;
    private String extra;
    public OperationResult(ASTNode process1, ASTNode process2, String ident1, String ident2, String operation, boolean negated, boolean result, String extra) throws CompilationException {
        this.process1 = new OperationProcess(Ansi.ansi().render("@|black "+ident1+"|@")+"", process1.getLocation());
        this.process2 = new OperationProcess(Ansi.ansi().render("@|black "+ident2+"|@")+"", process2.getLocation());
        this.operation = (negated ? "!" : "")+getOpSymbol(operation);
        this.result = result+"";
        this.extra = Ansi.ansi().render(extra)+"";
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
    public class OperationProcess {
        private String ident;
        private Location location;
    }
}
