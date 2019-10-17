package mc.processmodels.conversion;

import mc.processmodels.automata.AutomatonEdge;

import java.util.Set;
import java.util.TreeSet;

public class Step implements Comparable {


    private Set<String> pre = new TreeSet<>();
    private String lab;

    @Override
    public int compareTo(Object o) {
        if (o instanceof Step) {
            Step stepO = ((Step) o);
            if (lab.equals(stepO.lab) &&
                (pre.size() == stepO.pre.size()) &&
                this.pre.stream().map(st -> (stepO.pre.contains(st))).
                    reduce(true, (x, y) -> x && y)) {
                return 0;
            }
        }
        return -1;
    }

    public String getLab() {
        return lab;
    }

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
    public Step(Set<String> preIn, String labIn) {
        //System.out.println("build Step "+preIn+"  lab= "+labIn);
        lab = labIn;
        pre = new TreeSet<>();
        preIn.stream().forEach(x -> pre.add(x));
    }

/*
   return  0 if equal Step
           1 if sub Step
 */

    public String isSubStep(Set<String> newPre, String labIn) {
        if (!lab.equals(labIn)) return "clash";
        if (newPre.equals(pre)) {
            return "sameStep";
        } else if (pre.containsAll(newPre)) {
            return "subStep";
        } else if (newPre.containsAll(pre)) {
            return "superStep";
        }else return "unrleated";
    }



    public String myString() {
        return "pre " + pre + ",  lab " + lab;
    }
}
