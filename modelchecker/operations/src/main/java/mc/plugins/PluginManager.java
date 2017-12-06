package mc.plugins;


import lombok.Getter;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PluginManager {

    @Getter
    private static final PluginManager instance = new PluginManager();

    private final Reflections reflection = new Reflections("mc.operations");

    public Collection<? extends IProcessFunction> getFunctions(){
        return instantiateStream(reflection.getSubTypesOf(IProcessFunction.class));
    }

    public <V> Collection<? extends V> instantiateStream(Collection<Class<? extends V>> s){
        return s.stream().map(aClass -> {
                    try {
                        return aClass.newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList()
                );
    }

    public Collection<? extends IProcessInfixFunction> getInfixFunctions(){
        return instantiateStream(reflection.getSubTypesOf(IProcessInfixFunction.class));
    }

}
