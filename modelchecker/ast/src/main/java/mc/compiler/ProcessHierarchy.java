package mc.compiler;


import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ProcessHierarchy {

    private final Multimap<String, String> dependencies = MultimapBuilder.hashKeys().hashSetValues().build();

  public ProcessHierarchy() {
  }

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

  public Multimap<String, String> getDependencies() {
    return this.dependencies;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ProcessHierarchy)) return false;
    final ProcessHierarchy other = (ProcessHierarchy) o;
    if (!other.canEqual((Object) this)) return false;
    final Object this$dependencies = this.getDependencies();
    final Object other$dependencies = other.getDependencies();
    if (this$dependencies == null ? other$dependencies != null : !this$dependencies.equals(other$dependencies))
      return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ProcessHierarchy;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $dependencies = this.getDependencies();
    result = result * PRIME + ($dependencies == null ? 43 : $dependencies.hashCode());
    return result;
  }

  public String toString() {
    return "ProcessHierarchy(dependencies=" + this.getDependencies() + ")";
  }
}
