package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

/**
 * AutomataNode contains one process that must be interpreted as an automata
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AutomataNode extends ASTNode {

    /**
     * The first process used in the choice.
     * <p>
     * e.g. {@code B} in {@code A=B|C}
     */
    private ASTNode process;


    /**
     * Instantiate ChoiceNode.
     *
     * @param process  process to be interpreted as an automata
     * @param location      the location of the choice within the users code {@link ASTNode#location}
     */
    public AutomataNode(ASTNode process, Location location) {
        super(location,"Automata");
        this.process = process;

    }
    public String myString(){
        return "Automata "+process.myString();
    }
}

