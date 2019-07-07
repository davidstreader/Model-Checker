package mc.operations;

import mc.processmodels.automata.AutomatonNode;
import java.util.List;
import java.util.Set;

public interface SubSetDataConstructor {
  public List<Set<String>> op (Set<AutomatonNode> nodes, boolean cong) ;
}
