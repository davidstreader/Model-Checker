package mc.compiler;

import com.microsoft.z3.Context;
import mc.exceptions.CompilationException;
import mc.plugins.IProcessFunction;
import mc.processmodels.MultiProcessModel;
import mc.processmodels.ProcessType;
import mc.processmodels.automata.Automaton;
import mc.processmodels.conversion.OwnersRule;
import mc.processmodels.conversion.TokenRule;
import mc.processmodels.petrinet.Petrinet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class A2P2A implements IProcessFunction {
  /**
   * Gets the method name when it is called (e.g. {@code abs} in {@code abs(A)}).
   *
   * @return the name of the function
   */
  @Override
  public String getFunctionName() {
    return "NOTinUSE";
  }

  /**
   * Get the available flags for the function described by this interface (e.g. {@code unfair} in
   * {@code abs{unfair}(A)}.
   *
   * @return a collection of available flags (note, no variables may be flags)
   */
  @Override
  public Collection<String> getValidFlags() {
    return new HashSet<>();
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

  boolean dfaTerminating = false;
  /**
   * Execute the function on automata.
   *
   * @param id       the id of the resulting automaton
   * @param flags    the flags given by the function (e.g. {@code unfair} in {@code abs{unfair}(A)}
   * @param context  the z3 context
   * @param tt
   * @param automata a variable number of automata taken in by the function
   * @return the resulting automaton of the operation
   * @throws CompilationException when the function fails
   */
  @Override
  public Automaton compose(String id, Set<String> flags, Context context,
                            Automaton... automata)
      throws CompilationException {
    //System.out.println("Pingo2");
    assert automata.length == 1;
    Automaton inputA = automata[0].copy();
    inputA.setId(inputA.getId()+".2a");
    Petrinet pn = new Petrinet(id+".p",false);

 //   pn = PetrinetReachability.removeUnreachableStates( OwnersRule.ownersRule(inputA));
    //System.out.println("A2p2a input "+ inputA.myString());
    pn.addPetrinet(OwnersRule.ownersRule(inputA), true, true); //root needed
    //System.out.println("\n p in a2P2a "+pn.myString());
    Automaton   aut = TokenRule.tokenRule(pn) ;
    //System.out.println("\n End a in a2p2A "+aut.myString());

    return aut;
  }





  /**
   * DO NOT DO
   * Execute the function on one or more petrinet.
   *
   * @param id        the id of the resulting petrinet
   * @param flags     the flags given by the function (e.g. {@code unfair} in {@code abs{unfair}(A)}
   * @param context
   * @param petrinets the variable number of petrinets taken in by the function
   * @return the resulting petrinet of the operation
   * @throws CompilationException when the function fails
   */
  @Override
  public Petrinet compose(String id, Set<String> flags, Context context, Petrinet... petrinets)
          throws CompilationException {
    System.out.println("A2P2A input Petrinet");
    assert petrinets.length == 1;
    Petrinet inputP = petrinets[0].copy();
    inputP.setId(inputP.getId()+".2a");
    Automaton   at = TokenRule.tokenRule(inputP) ;

    //   pn = PetrinetReachability.removeUnreachableStates( OwnersRule.ownersRule(inputA));
    //System.out.println("A2p2a input "+ inputA.myString());
    Petrinet p = OwnersRule.ownersRule(at); //root needed
    //System.out.println("\n p in a2P2a "+pn.myString());
     TokenRule.tokenRule(p) ;
    return p;
  }

  @Override
  public MultiProcessModel compose(String id, Set<String> flags, Context context, MultiProcessModel... multiProcess)
          throws CompilationException {
    System.out.println("A2P2A  input MultiProcessModel");
    assert multiProcess.length == 1;
    MultiProcessModel inputM = multiProcess[0];


    Automaton inputA = (Automaton) inputM.getProcess(ProcessType.AUTOMATA);

    //   pn = PetrinetReachability.removeUnreachableStates( OwnersRule.ownersRule(inputA));
    //System.out.println("A2p2a input "+ inputA.myString());
    Petrinet p = OwnersRule.ownersRule(inputA); //root needed
    //System.out.println("\n p in a2P2a "+pn.myString());
    Automaton   a = TokenRule.tokenRule(p) ;
    MultiProcessModel m = new MultiProcessModel(inputA.getId());
    m.addProcess(p);
    m.addProcess(a);
    return m;
  }

}
