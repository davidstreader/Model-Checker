package mc.operations;

import com.google.common.collect.ImmutableSet;
import com.microsoft.z3.Context;
import mc.Constant;
import mc.TraceType;
import mc.exceptions.CompilationException;
import mc.plugins.IOperationInfixFunction;
import mc.processmodels.ProcessModel;
import mc.processmodels.automata.AutomatonNode;

import java.util.*;
import java.util.stream.Collectors;

public class SafeTraceRefinement implements IOperationInfixFunction {
    /**
     * A method of tracking the function.
     *
     * @return The Human-Readable form of the function name
     */
    @Override
    public String getFunctionName() {
        return "TraceRefinement";
    }
    @Override
    public Collection<String> getValidFlags(){
        return ImmutableSet.of(Constant.UNFAIR, Constant.FAIR, Constant.CONGURENT);
    }

    /**
     * The form which the function will appear when composed in the text.
     *
     * @return the textual notation of the infix function
     */
    @Override
    public String getNotation() {
        return "<st";
    }
    @Override
    public String getOperationType(){return "automata";}
    /**
     * Evaluate the function.
     *
     * @param alpha
     * @param processModels automaton in the function (e.g. {@code A} in {@code A ~ B})
     * @return the resulting automaton of the operation
     */
    @Override
    public boolean evaluate(Set<String> alpha, Set<String> flags, Context context,
                            Stack<String> trace,
                            Collection<ProcessModel> processModels) throws CompilationException {
        ProcessModel[] pms = processModels.toArray(new ProcessModel[processModels.size()]);
        //System.out.println("TraceRefinement "+ alpha +" "+flags+ " "+ pms[0].getId()+ " "+pms[1].getId());
        TraceWork tw = new TraceWork();
        //Void parameters used elsewhere to build Failure,Singelton Fail, ....
        //SubSetDataConstructor doNothing = (x,y) -> new ArrayList<>();
        //SubSetEval yes = (x,y,z) -> true;

        return tw.evaluate(flags,context, processModels,
            TraceType.Trace,  // this is the only difference between trace and cmplerte trace
            //   TraceType.CompleteTrace,
            trace,
            this::readyWrapped,
            (s1, s2, cong, error) -> isReadySubset(s1, s2, cong, error));
    }

    /*
       function returns the union of the ready sets to be added to the dfa
     */
    public List<Set<String>> readyWrapped(Set<AutomatonNode> nds, boolean cong){

        List<Set<String>> readyWrap = new ArrayList<>();
        Set<String> ready = new TreeSet<>();
        nds.stream().map(x->x.readySet(cong)).forEach(s->ready.addAll(s));
        //System.out.println("readyWrapped "+ready);
        readyWrap.add(ready.stream().distinct().collect(Collectors.toSet()));
        //System.out.println("readyWrapped "+readyWrap);

        return readyWrap;
    }

  /* s2 subset of s1
    function to be applied to the data output from readyWrapped
    returns subset
   */

    private  boolean equivExternal(List<Set<String>> s1,List<Set<String>> s2) {
        Set<String> ex1 =  s1.get(0).stream().filter(Constant::observable).collect(Collectors.toSet());
        Set<String> ex2 =  s2.get(0).stream().filter(Constant::observable).collect(Collectors.toSet());
        return ex1.containsAll(ex2)&& ex2.containsAll(ex1);
    }

    public boolean isReadySubset(List<Set<String>> s1,List<Set<String>> s2, boolean cong, ErrorMessage error) {
        boolean out = true;
        System.out.println("tr isReadySubset cong "+cong+"  "+s1.get(0)+ " >> "+s2.get(0) );
        if (cong) {
            Set<String> small = s2.get(0).stream().filter(x->!Constant.external(x)).collect(Collectors.toSet());

            out = s1.get(0).containsAll(small) ; //&& equivExternal(s1,s2);
        }
        else {
            for (String lab :s2.get(0)) {
                if (Constant.external(lab)) continue;
                if (!s1.get(0).contains(lab)) {
                    out = false;
                    break;
                }
            }
        }
        if (!out) {
            error.error = s2.get(0).stream().
                filter(x->!s1.get(0).contains(x)).collect(Collectors.toSet()).toString();

            //System.out.println("error " + error.error);
        }
        return out;
    }
}


