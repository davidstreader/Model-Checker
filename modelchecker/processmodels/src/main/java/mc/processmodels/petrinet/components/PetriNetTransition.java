package mc.processmodels.petrinet.components;


import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import mc.Constant;
import mc.processmodels.ProcessModelObject;
import mc.processmodels.conversion.Step;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PetriNetTransition extends ProcessModelObject implements Comparable {
    private String label;
    private Set<PetriNetEdge> incoming = new HashSet<>();
    private Set<PetriNetEdge> outgoing = new HashSet<>();
    private TreeSet<String> owners = new TreeSet<>();
    //id and type in ProcessModelObject id unique to process (not between processes)
    @Getter
    @Setter
    private String fromTran = "";


    public Set<PetriNetEdge> copyIncoming() {
        //System.out.println("in size "+incoming.size());
        Set<PetriNetEdge> out = new HashSet<>();
        for (PetriNetEdge ed : incoming) {
            //System.out.println("ed "+ed.myString());
            out.add(ed);
        }
        return out;
    }

    public Set<PetriNetEdge> copyOutgoing() {
        //System.out.println("out size "+outgoing.size());
        Set<PetriNetEdge> out = new HashSet<>();
        for (PetriNetEdge ed : outgoing) {
            //System.out.println("ed "+ed.myString());
            out.add(ed);
        }
        return out;
    }

    public boolean equals(Object tr) {
        if (!(tr instanceof PetriNetTransition))
            return false;
        else
            return this.getId().equals(((PetriNetTransition) tr).getId());
    }

    public boolean isHidden() {
        return this.label.equals(Constant.HIDDEN);
    }

    public boolean isBlocked() {
        return this.label.equals(Constant.DEADLOCK);
    }

    public boolean same(String fromId, String lab, String toId) {
        String ltr = label.split("\\:")[0];
        if (ltr.endsWith(Constant.BROADCASTSoutput) || ltr.endsWith(Constant.BROADCASTSinput) ) {
            ltr = ltr.substring(0, ltr.length() - 1);
        }
        if (lab.endsWith(Constant.BROADCASTSoutput) || lab.endsWith(Constant.BROADCASTSinput) ) {
            lab = lab.substring(0, lab.length() - 1);
        }
        if (ltr.equals(lab)) {
            if (preOne().equals(fromId) && postOne().equals(toId)){
                 return true;
            }
        }
        //System.out.println("SAME "+preOne()+"-"+fromId+", "+label.split("\\:")[0]+"-"+lab.split("\\:")[0] +", "+postOne()+"-"+toId );

        return false;
    }

    public void addOwner(String ownerName) {
        //System.out.println("addOwner "+ownerName);
        owners.add(ownerName);
        //System.out.println("X");
    }

    public void addOwners(Set<String> ownersName) {
        //System.out.println(this.owners.toString());
        for (String o : ownersName) {
            //System.out.println("o "+o);
            owners.add(o);
        }
    }

    public void clearOwners() {
        owners = new TreeSet<>();

    }

    public void removeEdge(PetriNetEdge ed) {
        //this seems clumsy but many shorter version failed
        Set<PetriNetEdge> xin = new HashSet<>();
        for (PetriNetEdge edi : incoming) {
            if (!edi.getId().equals(ed.getId())) {
                xin.add(edi);
            }
        }
        Set<PetriNetEdge> xout = new HashSet<>();
        for (PetriNetEdge edi : outgoing) {
            if (!edi.getId().equals(ed.getId())) {
                xout.add(edi);
            }
        }
        incoming = xin;
        outgoing = xout;

        //System.out.println("removed in "+incoming.size()+" removed out "+outgoing.size());
    }

    public PetriNetTransition(String id, String label) {
        super(id, "node");
        this.label = label;
    }

    private String preOne() {
        //System.out.println(incoming.size());
        if (incoming.size() != 1) return null;
        for (PetriNetEdge ed : incoming) {
            //System.out.println("ed "+ed.myString());
            return  ed.getFrom().getId();
            //System.out.println(p.getId());
        }
        return null;
    }
    private String postOne() {
        //System.out.println(incoming.size());
        if (outgoing.size() != 1) return null;
        for (PetriNetEdge ed : outgoing) {
            //System.out.println("ed "+ed.myString());
            return  ed.getTo().getId();
            //System.out.println(p.getId());
        }
        return null;
    }
    public boolean markedBy(Multiset<PetriNetPlace> mark) {
        return mark.containsAll(pre());
    }

    public Set<PetriNetPlace> pre() {
        //System.out.println(incoming.size());
        Set<PetriNetPlace> out = new HashSet<>();
        if (incoming.size() == 0) return out;
        for (PetriNetEdge ed : incoming) {
            //System.out.println("ed "+ed.myString());
            PetriNetPlace p = (PetriNetPlace) ed.getFrom();
            //System.out.println(p.getId());
            out.add(p);
        }
        //System.out.println("out.size "+out.size());
        return out;
  /*  return incoming.stream()
        .map(PetriNetEdge::getFrom)
        .map(PetriNetPlace.class::cast)
        .distinct()
        .collect(Collectors.toSet()); */
    }

    public Set<PetriNetPlace> preNotOptional() {
        return incoming.stream()
            .filter(ed -> !ed.getOptional())
            .map(PetriNetEdge::getFrom)
            .map(PetriNetPlace.class::cast)
            .distinct()
            .collect(Collectors.toSet());
    }

    public Set<PetriNetPlace> preOptional() {
        return incoming.stream()
            .filter(ed -> ed.getOptional())
            .map(PetriNetEdge::getFrom)
            .map(PetriNetPlace.class::cast)
            .distinct()
            .collect(Collectors.toSet());
    }

    public Set<PetriNetPlace> postNotOptional() {
        return outgoing.stream()
            .filter(ed -> !ed.getOptional())
            .map(PetriNetEdge::getTo)
            .map(PetriNetPlace.class::cast)
            .distinct()
            .collect(Collectors.toSet());
    }

    public Set<String> tr2preSet(Multiset<PetriNetPlace> mark) {
      /*System.out.println("tr2Step "+this.getId()+" pre= "+
            this.pre().stream().map(x->x.getId()+", ").collect(Collectors.joining())+"; mark= "+
            mark.stream().map(x->x.getId()+", ").collect(Collectors.joining())); */
        Set<String> markedPre = mark.stream().
            filter(x -> this.pre().contains(x)).
            map(x -> x.getId()).
            collect(Collectors.toSet());

        //System.out.println("markedPre "+markedPre);

        return markedPre;
    }



    public boolean NonBlockingEqu(PetriNetTransition tr) {
        Set<String> pre = preNotOptional().stream().
            map(PetriNetPlace::getId).
            collect(Collectors.toSet());
        Set<String> trPre = tr.preNotOptional().stream().
            map(PetriNetPlace::getId).
            collect(Collectors.toSet());

        Set<String> post = postNotOptional().stream().
            map(PetriNetPlace::getId).
            collect(Collectors.toSet());
        Set<String> trPost = tr.postNotOptional().stream().
            map(PetriNetPlace::getId).
            collect(Collectors.toSet());

        return pre.equals(trPre) && post.equals(trPost) && getLabel().equals(tr.getLabel());
    }


    public Set<String> optionalOwners() {  //TokenRule
        return incoming.stream()
            .filter(ed -> ed.getOptional())
            .map(PetriNetEdge::getFrom)
            .map(x -> ((PetriNetPlace) x).getOwners())
            .flatMap(x -> x.stream())
            .collect(Collectors.toSet());
    }

    public Set<String> nonOptionalOwners() {  //TokenRule
        return incoming.stream()
            .filter(ed -> ed.getOptional())
            .map(PetriNetEdge::getFrom)
            .map(x -> ((PetriNetPlace) x).getOwners())
            .flatMap(x -> x.stream())
            .collect(Collectors.toSet());
    }


    public Set<PetriNetPlace> post() {
        return outgoing.stream()
            .map(PetriNetEdge::getTo)
            .map(PetriNetPlace.class::cast)
            .distinct()
            .collect(Collectors.toSet());
    }

    public boolean postEqualsPre() {
        if (post().size() != pre().size()) return false;
        for (PetriNetPlace pl : post()) {
            if (!pre().contains(pl)) return false;
        }
        return true;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("transition{\n");
        builder.append("\tid:").append(getId());
        builder.append("\tlabel:").append(getLabel());
        builder.append("\n");
        builder.append("\tincoming:{");

        for (PetriNetEdge edge : getIncoming()) {
            builder.append(edge.getId()).append(",");
        }

        builder.append("}\n");

        builder.append("\toutgoing:{");
        for (PetriNetEdge edge : getOutgoing()) {
            builder.append(edge.getId()).append(",");
        }
        builder.append("}\n}");

        return builder.toString();
    }

    public String myString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getId() + ", ");
        for (PetriNetEdge edge : getIncoming()) {
            builder.append(edge.getFrom().getId() + "+" + edge.getOptional() + "-" + edge.getOptionNum() + " ");
            if (edge.getGuard() != null) builder.append(edge.getGuard().getGuardStr());
        }
        builder.append("-" + label + "->");
        for (PetriNetEdge edge : getOutgoing()) {
            if (edge.getGuard() != null) builder.append(edge.getGuard().getAssStr());
            builder.append("+" + edge.getOptional() + "-" + edge.getOptionNum() + " " + edge.getTo().getId());
        }
        builder.append(", own " + this.getOwners());
        return builder.toString();
    }

    public String getLabel() {
        return this.label;
    }

    public Set<PetriNetEdge> getIncoming() {
        return this.incoming;
    }

    public Set<PetriNetEdge> getOutgoing() {
        return this.outgoing;
    }

    public TreeSet<String> getOwners() {
        return this.owners;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setIncoming(Set<PetriNetEdge> incoming) {
        this.incoming = incoming;
    }

    public void setOutgoing(Set<PetriNetEdge> outgoing) {
        this.outgoing = outgoing;
    }

    public void setOwners(TreeSet<String> owners) {
        this.owners = owners;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof PetriNetTransition) {
            if (((PetriNetTransition) o).getId().equals(this.getId())) return 0;
        }
        return 1;
    }
}
