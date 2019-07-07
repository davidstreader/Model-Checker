package mc.processmodels.petrinet.utils;


import mc.processmodels.automata.util.ColouringUtil;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class PetriColouring {
    private static final int BASE_COLOUR = 1;
    private static final int STOP_COLOUR = 0;
    private static final int ERROR_COLOUR = -1;
    private static final int ROOT_COLOUR = -2;
    private static final int ROOT_STOP_COLOUR = -3;
    private static final int ROOT_ERROR_COLOUR = -4;
    private int nextColourId = 1;

    /*
      is the gluing together of SETs of Places needed?
       Colour of a Place must be as set of colours to allow the equality of
      two sets of places to be computed!
     */
    public Map<String, Integer> initColour(Petrinet petri){
        Map<String, Integer> colourMap = new TreeMap<>();
        for(String pl: petri.getPlaces().keySet()) {
            if (petri.getPlaces().get(pl).isStart())
                colourMap.put(pl,1);
            else
              colourMap.put(pl,0);
        }
    return colourMap;
    }
    public void doColour(Petrinet petri, Map<String, Integer> initCol){
        System.out.println("doColour START");
        Set<PetriColourComponent> colPi = new TreeSet<>();
        Multimap<Set<PetriColourComponent>,PetriNetPlace> colPiMap = ArrayListMultimap.create();
        //multiset with key an object
        PetriColourComponent pcc = null;
        //set inital colouring
        for(String pid: initCol.keySet()){
            PetriNetPlace pl = petri.getPlaces().get(pid);
            pl.setColour(initCol.get(pid));
            //System.out.println("place "+pl.getId()+" col "+pl.getColour());
        }
        int colSize = 1;
        while(true) {
            //build ColPi
            for (PetriNetPlace pl : petri.getPlaces().values()) {
                for (PetriNetTransition tr : pl.post()) {
                    colPi.add(new PetriColourComponent(tr));  //uses .equals()
                }
        //System.out.println("colPi  "+pl.getId()+"->"+  colPi.toString());
                boolean fnd = false;
                for(Set<PetriColourComponent> found : colPiMap.keySet()){
                    //System.out.println(" check  "+found.toString());
                    if (found.equals(colPi) ) {
                        //System.out.print("found ");
                        colPiMap.put(found, pl);
           //System.out.println(colPiMap.get(found).stream().map(x->x.getId()).reduce("",(x,y)->x+y+" "));
                        fnd = true;
                        break;
                    }
                }
                if (!fnd) colPiMap.put(colPi, pl);
                System.out.println("ColPiMap "+ colPi.toString()+"->>"+pl.getId());
                colPi = new TreeSet<>();
            }
            //System.out.println("*****colSize = "+colSize+ " colPiMap.keySet().size() "+colPiMap.keySet().size());
            /*System.out.println("colPiMap keys "+
                    colPiMap.keySet().stream().map(x->x.toString()).reduce("",(x,y)->x+y+" ")); */
            if  (colSize == colPiMap.keySet().size()) break;
            colSize = colPiMap.keySet().size();
            //System.out.println("ColPi built");
            //reColour places
            nextColourId = 1;
            //boolean terminate = true;
            for (Set<PetriColourComponent> k : colPiMap.keySet()) {
                //System.out.println(k+" -> "+colPiMap.get(k).size());
                int cnt = 0;
                for (PetriNetPlace pl : colPiMap.get(k)) {
                    pl.setColour(nextColourId);
                    cnt++;
                    //System.out.println(pl.myString());
                }
                //if (cnt >1) terminate = false;
                nextColourId++;
            }
            //terminate if finished

            //if (terminate || (colSize == colPiMap.size())) break;
            colPiMap = ArrayListMultimap.create();
            //System.out.println("NEXT "+colSize);
        }
        //System.out.println("COLOURing ENDS");
    }

    public class PetriColourComponent implements Comparable<PetriColourComponent>{
        //private Set<Integer> from = new TreeSet();
        private String label;
        private Set<String> owners = new TreeSet();
        private Set<Integer> to = new TreeSet();
       public String getLabel(){return label;}

        public String getOwnLabel() {
            return label+"." +owners.stream().reduce("", (x,y)->x+y+" ");
        }
        public String getToCol(){
           return to.stream().map(x->x.toString()).reduce("", (x,y)->x+y+" ");
       }

       public int compareTo(PetriColourComponent pcc){
           //int x = getOwnLabel().compareTo(pcc.getOwnLabel());
           int x = getLabel().compareTo(pcc.getLabel());
           if (x != 0 )return x;
           return getToCol().compareTo(pcc.getToCol());
        }
        public PetriColourComponent(PetriNetTransition tr){

            //from = tr.pre().stream().map(x->x.getColour()).collect(Collectors.toSet());
            to  = tr.post().stream().map(x->x.getColour()).collect(Collectors.toSet());
            label = tr.getLabel();
            owners = tr.pre().stream().flatMap(x->x.getOwners().stream()).collect(Collectors.toSet());
        }
        public String toString(){
            return label+"."+owners+"->"+to;
        }
        public boolean equals(PetriColourComponent pcc) {
           return this.compareTo(pcc) == 0;
        }
    }
    public Set<Integer> markColor(Set<PetriNetPlace> mark){
        return mark.stream().map(x->x.getColour()).collect(Collectors.toSet());
    }
}
