package mc.util.expr;

import mc.exceptions.CompilationException;
import mc.processmodels.automata.Automaton;
import mc.processmodels.petrinet.Petrinet;

public  class  MyAssert {
  private static boolean apply = true;

/*  public static void myAssert(Automaton, String mess) throws CompilationException {
    boolean b = true;
    if (apply) {
      System.out.println(mess);
      try {
        b = test.get();
      } catch (CompilationException e) {
        System.out.println(mess);
        throw e;
      }
      if (!test.get()) {
        System.out.println("Failure");
      }
    }
  } */
  public static void myAssert(Boolean test, String mess) {
    if (apply) {  // Wont work!
      System.out.println(mess+" "+test);
      if (!test) {
        System.out.println("Failure");
      }
    }
  }
  public static void setApply(boolean in){apply = in;}
  public static void validate(Petrinet petri,String mess) throws CompilationException {
    if (apply) {
      try {
        petri.validatePNet();
        System.out.println(mess+" Valid");
      } catch (Exception e){
        System.out.println(mess+" INVALID");
        throw e;
      }

    }
  }
  public static void validate(Automaton aut, String mess) throws CompilationException {
    if (apply) {
      try {
        aut.validateAutomaton();
        System.out.println(mess+" Valid");
      } catch (Exception e){
        System.out.println(mess+" INVALID");
        throw e;
      }

    }
  }
}


