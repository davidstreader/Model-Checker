package mc.process_models.petrinet;


import lombok.Getter;
import lombok.Setter;
import mc.process_models.ProcessModelObject;


public class PetriNetEdge extends ProcessModelObject {

    @Getter
    @Setter
    private String label;

    @Getter
    @Setter
    private PetriNetTransitions from;// it can also br from a Place

    @Getter
    @Setter
    private PetriNetTransitions to;// it can also br from a Place

    public PetriNetEdge(String id, String label, PetriNetTransitions from, PetriNetTransitions to){
        super(id,"edge");
        this.label = label;
        this.from = from;
        this.to = to;
    }
}
