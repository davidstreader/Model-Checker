package mc.plugins;


import lombok.Getter;
import org.reflections.Reflections;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

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
     * @return A collection of Functions
     */
    public Collection<? extends IProcessFunction> getFunctions(){
        return instantiateClasses(reflection.getSubTypesOf(IProcessFunction.class));
    }

    /**
     * Converts a collection of class objects into instantiated versions
     * @param classes a collection of class objects to be converted into new instances
     * @param <V> the type of object the instances will extend
     * @return An instantiated collection of objects
     */
    public <V> Collection<? extends V> instantiateClasses(Collection<Class<? extends V>> classes){
        return classes.stream()
                .distinct()
                .map(aClass -> {
                    try {
                        return aClass.newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * This retrieves an instance of every single valid infix function within the {@code mc.operations} package
     * @return A collection of InfixFunctions
     */
    public Collection<? extends IProcessInfixFunction> getInfixFunctions(){
        return instantiateClasses(reflection.getSubTypesOf(IProcessInfixFunction.class));
    }

}
