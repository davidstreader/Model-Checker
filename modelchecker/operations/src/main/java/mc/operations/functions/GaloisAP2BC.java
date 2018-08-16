package mc.operations.functions;

import com.microsoft.z3.Context;

import java.util.*;
import java.util.stream.Collectors;

import mc.Constant;
import mc.exceptions.CompilationException;
import mc.plugins.IProcessFunction;
import mc.processmodels.MultiProcessModel;
import mc.processmodels.automata.Automaton;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;

public class GaloisAP2BC implements IProcessFunction {
  /**
   * Gets the method name when it is called (e.g. {@code abs} in {@code abs(A)}).
   *
   * @return the name of the function
   */
  @Override
  public String getFunctionName() {
    return "fap2bc";
  }

  /**
   * Get the available flags for the function described by this interface (e.g. {@code unfair} in
   * {@code abs{unfair}(A)}.
   *
   * @return a collection of available flags (note, no variables may be flags)
   */
  @Override
  public Collection<String> getValidFlags() {
    return Collections.singleton("*");
  }

  /**
   * Gets the number of automata to parse into the function.
   *
   * @return the number of arguments
   */
  @Override
  public int getNumberArguments() {
    return 1;
  }

  /**
   * Execute the function on automata.
   *
   * @param id       the id of the resulting automaton
   * @param flags    the flags given by the function (e.g. {@code unfair} in {@code abs{unfair}(A)}
   * @param automata a variable number of automata taken in by the function
   * @return the resulting automaton of the operation
   */
  @Override
  public Automaton compose(String id, Set<String> flags, Context context, Automaton... automata)
    throws CompilationException {


    return null;
  }

  /**  Automata
   * Replace b^ with  R-bt!->x, x-ba?->E, x-br?->R
   * Replace b  with  R-bt?->y, y-ba!->E
   *  if b notin pi(n)  add  n-bt?->z, z-br!->n
   *      Last requirement on PetriNets is a refinment of ading "listening loops"
   *
   * @param id        the id of the resulting petrinet
   * @param flags     the flags given by the function (e.g. {@code unfair} in {@code abs{unfair}(A)}
   * @param context
   * @param petrinets the variable number of petrinets taken in by the function
   * @return the resulting petrinet of the operation
   * @throws CompilationException when the function fails
   */
  @Override
  public Petrinet compose(String id, Set<String> flags, Context context, Petrinet... petrinets) throws CompilationException {
    Petrinet petrinet = petrinets[0].reId("Gal");
    Set<String> alphabetListen = petrinet.getAlphabet().keySet().stream().filter(x->x.endsWith("?")).collect(Collectors.toSet());

    for (PetriNetPlace pl : petrinet.getPlaces().values()){
      Set<String> nextListen=  pl.post().stream().map(x->x.getLabel()).filter(x->x.endsWith("?")).collect(Collectors.toSet());
      Set<String> toAdd = alphabetListen.stream().filter(x-> ! nextListen.contains(x)).collect(Collectors.toSet());
      for(String lab: toAdd) {
        PetriNetPlace z = petrinet.addPlace();
        Set<PetriNetPlace> zset = new HashSet<>();
        zset.add(z);


      }
    }


    for(PetriNetTransition tr: petrinet.getTransitions().values()){
      String lab = tr.getLabel();
      if (lab.endsWith("^")) {
        String prefix = lab.substring(0,lab.length()-1);
        PetriNetPlace x = petrinet.addPlace();
        Set<PetriNetPlace> xset = new HashSet<>();
        xset.add(x);
        petrinet.addTransition(tr.pre(), prefix + lab+"t!", xset );
        petrinet.addTransition(xset, prefix + lab+"r?", tr.pre() );
        petrinet.addTransition(xset, prefix + lab+"a?", tr.post() );
        petrinet.removeTransition(tr);

      } else if (lab.endsWith("?")) {
        String prefix = lab.substring(0,lab.length()-1);
        PetriNetPlace y = petrinet.addPlace();
        Set<PetriNetPlace> yset = new HashSet<>();
        yset.add(y);
        petrinet.addTransition(tr.pre(), prefix + lab+"t?", yset );
        petrinet.addTransition(yset, prefix + lab+"a!", tr.post() );
        petrinet.removeTransition(tr);
      }

    }
    return   petrinet;

  }
  @Override
  public MultiProcessModel compose(String id, Set<String> flags, Context context, MultiProcessModel... multiProcess) throws CompilationException {
    return null;
  }
}

