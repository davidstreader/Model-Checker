package mc.processmodels.conversion;

import mc.processmodels.automata.AutomatonEdge;

import java.util.Set;
import java.util.TreeSet;

public class Step implements Comparable {


    private Set<String> pre = new TreeSet<>();
    private String lab;
    private String id;
    private static int sid = 1;

    @Override
    public int compareTo(Object o) {
        if (o instanceof Step) {
            Step stepO = ((Step) o);

            return (lab+pre).compareTo(stepO.lab+stepO.pre);
           /* if (lab.equals(stepO.lab) &&
                (pre.size() == stepO.pre.size()) &&
                this.pre.stream().map(st -> (stepO.pre.contains(st))).
                    reduce(true, (x, y) -> x && y)) {
                return 0;
            }*/
        }
        return -100;
    }

    public String getLab() {
        return lab;
    }
    public static void reset(){sid=1;}
    public String getId(){return id;}
    public Set<String> getPre() {
        return pre;
    }
    public void setPre(Set<String> in) {
        pre = in;
    }

    public Step( String labIn) {
        //System.out.println("build Step "+preIn+"  lab= "+labIn);
        lab = labIn;
        pre = new TreeSet<>();

    }
    public Step copy(){
        Set<String> newPre = new TreeSet<>();
        this.pre.stream().forEach(x->newPre.add(x));
        return new Step(newPre,lab);
    }
    public Step(Set<String> preIn, String labIn) {
        System.out.println("build Step "+preIn+"  lab= "+labIn);
        lab = labIn;
        pre = new TreeSet<>();
        id = "S"+sid++;
        preIn.stream().forEach(x -> pre.add(x));
        System.out.println( this.myString());
    }

/*
   return  0 if equal Step
           1 if sub Step
 */

    public String isNewSubStep(Set<String> newPre, String labIn) {
        if (!lab.equals(labIn)) return "clash";
        if (newPre.equals(pre)) {
            return "newSameStep";
        } else if (pre.containsAll(newPre)) {
            return "newSubStep";
        } else if (newPre.containsAll(pre)) {
            return "newSuperStep";
        }else return "unrleated";
    }



    public String myString() {
        return "  >Step>  id "+id+ " pre " + pre + ",  lab " + lab;
    }
}
