package mc.operations.impl;

import com.google.common.collect.Iterables;
import java.util.HashSet;
import java.util.Set;
import mc.processmodels.petrinet.Petrinet;

public class PetrinetParallelFunction {

  private static Set<String> unsynchedActions;
  private static Set<String> synchronisedActions;

  public static Petrinet compose(Petrinet p1, Petrinet p2) {
    clear();
    setupActions(p1, p2);
    return null;
  }

  private static void setupActions(Petrinet p1, Petrinet p2) {
    Set<String> actions1 = p1.getAlphabet().keySet();
    Set<String> actions2 = p2.getAlphabet().keySet();

    for (String action : Iterables.concat(actions1, actions2)) {
      //one of these must be true, therefore there must be a synchronisation
      if (actions1.contains(action) == actions2.contains(action)) {
        synchronisedActions.add(on);
        continue;
      }

    }

  }

  private static void clear() {
    unsynchedActions = new HashSet<>();
    synchronisedActions = new HashSet<>();

  }

  private static boolean actionEquals(){
    return false;
  }
}
