package mc.processmodels.conversion;

import mc.processmodels.automata.AutomatonEdge;

import java.util.Set;
import java.util.TreeSet;

public class Step implements Comparable {


    private Set<String> pre = new TreeSet<>();
    private Set<String> post = new TreeSet<>();
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
    public Set<String> getPost() {
        return post;
    }

    public Step(Set<String> preIn, String labIn, Set<String> postIn) {
        //System.out.println("build Step "+preIn+"  lab= "+labIn);
        lab = labIn;
        this.pre = new TreeSet<>();
        this.post = new TreeSet<>();
        preIn.stream().forEach(x -> pre.add(x));
        postIn.stream().forEach(x -> post.add(x));

    }

/*
   return  0 if equal Step
           1 if sub Step
 */

    public int isSubStep(Step sin) {
        if (!lab.equals(sin.getLab())) return -1;
        if (sin.getPre().equals(pre)) {
            return 0;
        } else if (sin.getPre().containsAll(pre)) {
            return 1;
        } else return -1;
    }

    public Set<String> hasMoreStep(Step sin) {
        Set<String> more = new TreeSet<>();
        if (!lab.equals(sin.getLab())) return more;

        pre.stream().forEach(x -> more.add(x));
        more.removeAll(sin.pre);
        return more;
    }

    public String myString() {
        return "pre " + pre + ",  lab " + lab;
    }
}
