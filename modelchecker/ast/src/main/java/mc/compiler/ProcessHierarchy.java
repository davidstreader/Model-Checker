package mc.compiler;


import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class ProcessHierarchy {

    private final Multimap<String, String> dependencies = MultimapBuilder.hashKeys().hashSetValues().build();

    /**
     * This method finds what processes may be built based on dependencies found during reference replacement
     *
     * @param builtProcesses the processes that have already been built
     * @return the processes that may be built with currently compiled information
     */
    public Set<String> getBuildableProcesses(Set<String> builtProcesses){
        return dependencies.keySet().stream()
                .filter(k ->builtProcesses.containsAll(dependencies.get(k)))
                .filter(k -> !builtProcesses.contains(k))
                .collect(Collectors.toSet());
    }

    /**
     * This returns a set of all the processes required to build a specified process
     * @param string the process that is being searched
     * @return a set of processes required to build {@code string}
     */
    public Set<String> getDependencies(String string){
        Deque<String> dependenciesQueue = new ArrayDeque<>();
        Set<String>   dependenciesSet   = new HashSet<>();
        if(!dependencies.containsKey(string))
            return new HashSet<>();
        dependenciesQueue.offer(string);
        //DFS search of graph beneath a component
        while(!dependenciesQueue.isEmpty()){
            String search = dependenciesQueue.pop();
            dependenciesSet.add(search);
            for(String s:dependencies.get(search))
                if(!dependenciesSet.contains(s))
                    dependenciesQueue.offer(s);
        }
        dependenciesSet.remove(string);
        return dependenciesSet;
    }
}
