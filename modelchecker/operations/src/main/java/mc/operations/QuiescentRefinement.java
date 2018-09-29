package mc.operations;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;
import com.microsoft.z3.Context;
import mc.Constant;
import mc.TraceType;
import mc.exceptions.CompilationException;
import mc.operations.functions.AbstractionFunction;
import mc.operations.functions.SimpFunction;
import mc.plugins.IOperationInfixFunction;
import mc.processmodels.ProcessModel;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonNode;

public class QuiescentRefinement implements IOperationInfixFunction {
  /**
   * A method of tracking the function.
   *
   * @return The Human-Readable form of the function name
   */
  @Override
  public String getFunctionName() {
    return "QuiescentRefinement";
  }

  /**
   * The form which the function will appear when composed in the text.
   *
   * @return the textual notation of the infix function
   */
  @Override
  public String getNotation() {
    return "<q";
  }
  @Override
  public String getOperationType(){return "automata";}
  @Override
  public Collection<String> getValidFlags(){
  return ImmutableSet.of(Constant.UNFAIR, Constant.FAIR, Constant.CONGURENT);
  }
  /**
   * Evaluate the quescten refinement function.
   *
   * @param alpha
   * @param processModels automaton in the function (e.g. {@code A} in {@code A ~ B})
   * @return the resulting automaton of the operation
   */
  @Override
  public boolean evaluate(Set<String> alpha, Set<String> flags, Context context, Collection<ProcessModel> processModels) throws CompilationException {
    System.out.println("\nQUIESCENT "+alpha);

    //ProcessModel[] pms =  processModels.toArray();
    Automaton a1 = ((Automaton) processModels.toArray()[0]).copy();
    Automaton a2 = ((Automaton) processModels.toArray()[1]).copy();
    System.out.println("****Quiescent a1 "+a1.readySets2String());
    System.out.println("****Quiescent a2 "+a2.readySets2String());
    //TraceRefinement teo = new TraceRefinement();

    AbstractionFunction abs = new AbstractionFunction();
    SimpFunction simp = new SimpFunction();
    //tw.evaluate(flags,processModels, TraceType.QuiescentTrace);
    //setQuiescentAndAddListeningLoops(alpha,a1);
    setQuiescent(a1);
    setQuiescent(a2);
    a1 = abs.GaloisBCabs(a1.getId(),flags,context,a1);
    a2 = abs.GaloisBCabs(a2.getId(),flags,context,a2);
    //a1 = simp.compose(a1.getId(),flags,context,a1);
    //a2 = simp.compose(a2.getId(),flags,context,a2);
    //AddListeningLoops(alpha,a1);  //PROBLEMS with doing this NOW
    //AddListeningLoops(alpha,a2);
    System.out.println("*** Q a1 before traceEval "+a1.readySets2String());
    System.out.println("*** Q a2 before traceEval "+a2.readySets2String());

    ArrayList<ProcessModel> pms = new ArrayList<>();;
    pms.add(a1);
    pms.add(a2);
    //return  teo.evaluate(alpha,flags,context,pms);
    TraceWork tw = new TraceWork();
    return tw.evaluate(flags,pms, TraceType.QuiescentTrace);
  }

  private void setQuiescentAndAddListeningLoops(Set<String> alphbet, Automaton a) throws CompilationException {
    System.out.println("addQuiescentAndListeningLoops");
    for(AutomatonNode nd : a.getNodes()){
      Set<String> notListening = nd.readySet().stream().filter(x->!x.endsWith("?")).collect(Collectors.toSet());
      nd.setQuiescent(notListening.size()==0);
      for(String lab: alphbet) {
        if (!nd.readySet().contains(lab)) {
          a.addEdge(lab,nd,nd,nd.getGuard(),false,false);
        }
      }
    }
  }
  private void setQuiescent( Automaton a) throws CompilationException {
    System.out.println("setQuiescent");
    for(AutomatonNode nd : a.getNodes()){
      Set<String> notListening = nd.readySet().stream().filter(x->!x.endsWith("?")).collect(Collectors.toSet());
      nd.setQuiescent(notListening.size()==0);
    }
  }
  private void AddListeningLoops(Set<String> alphbet, Automaton a) throws CompilationException {
    System.out.println("AddListeningLoops");
    for(AutomatonNode nd : a.getNodes()){
      System.out.println("  "+nd.getId()+"  "+nd.readySet());
      for(String lab: alphbet) {
        if (!nd.readySet().contains(lab)) {
          a.addEdge(lab,nd,nd,nd.getGuard(),false,false);
          System.out.println("     adding "+lab+" to "+nd.getId());
        } else {
          System.out.println("  NOTadding "+lab+" to "+nd.getId());
        }
      }
    }
  }

}

