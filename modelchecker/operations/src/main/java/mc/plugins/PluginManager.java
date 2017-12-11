package mc.plugins;


import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import mc.compiler.OperationEvaluator;
import mc.compiler.interpreters.AutomatonInterpreter;
import mc.process_models.automata.generator.AutomatonGenerator;
import org.reflections.Reflections;

public class PluginManager {

    /**
     * Singleton pattern
     */
    @Getter
    private static final PluginManager instance = new PluginManager();

    /**
     * An org.reflections object that is used to retrieve classes within the {@code mc.operations} package
     */
    private final Reflections reflection = new Reflections("mc.operations");

    /**
     * This retrieves an instance of every single valid function within the {@code mc.operations} package
     *
     * @return A collection of Functions
     */
    private ImmutableSet<Class<? extends IProcessFunction>> getFunctions(){
        return ImmutableSet.copyOf(reflection.getSubTypesOf(IProcessFunction.class));
    }

    /**
     * This retrieves an instance of every single valid infix function within the {@code mc.operations} package
     *
     * @return A collection of Infix Functions
     */
    private ImmutableSet<Class<? extends IProcessInfixFunction>> getInfixFunctions(){
        return ImmutableSet.copyOf(reflection.getSubTypesOf(IProcessInfixFunction.class));
    }

    /**
     * This retrieves an instance of every single valid infix operation within the {@code mc.operations} package
     *
     * @return A collection of Infix Operations
     */
    private ImmutableSet<Class<? extends IOperationInfixFunction>> getInfixOperations(){
        return ImmutableSet.copyOf(reflection.getSubTypesOf(IOperationInfixFunction.class));
    }

    public void registerPlugins(){
        getFunctions().forEach(AutomatonInterpreter::addFunction);
        getInfixFunctions().forEach(AutomatonInterpreter::addInfixFunction);
        getInfixOperations().forEach(OperationEvaluator::addOperations);
        getInfixOperations().forEach(AutomatonGenerator::addOperation);
    }

}
