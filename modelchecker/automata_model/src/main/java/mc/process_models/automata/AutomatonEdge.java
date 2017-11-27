package mc.process_models.automata;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import mc.Constant;
import mc.process_models.ProcessModelObject;
import mc.process_models.automata.serializers.JSONEdgeSerializer;

@JsonSerialize(using = JSONEdgeSerializer.class)
public class AutomatonEdge extends ProcessModelObject {

    private String label;
    private AutomatonNode from;
    private AutomatonNode to;

    public AutomatonEdge(String id, String label, AutomatonNode from, AutomatonNode to){
        super(id,"edge");
        this.label = label;
        this.from = from;
        this.to = to;
    }

    public String getLabel(){
        return label;
    }

    public void setLabel(String label){
        this.label = label;
    }

    public AutomatonNode getFrom(){
        return from;
    }

    public void setFrom(AutomatonNode from){
        this.from = from;
    }

    public AutomatonNode getTo(){
        return to;
    }

    public void setTo(AutomatonNode to){
        this.to = to;
    }

    public boolean isHidden(){
        return label.equals(Constant.HIDDEN);
    }

    public boolean isDeadlocked(){
        return label.equals(Constant.DEADLOCK);
    }

    public String toString(){
        String builder = "edge{\n" +
            "\tid:" + getId() + "\n" +
            "\tlabel:" + label + "\n" +
            "\tfrom:" + from.getId() + "\n" +
            "\tto:" + to.getId() + "\n" +
            "\tmetadata:" + getMetaData() + "\n" +
            "}";

        return builder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AutomatonEdge edge = (AutomatonEdge) o;

        if (!label.equals(edge.label)) return false;
        if (!from.getId().equals(edge.from.getId())) return false;
        return to.getId().equals(edge.to.getId());
    }

    @Override
    public int hashCode() {
        int result = label.hashCode();
        result = 31 * result + from.getId().hashCode();
        result = 31 * result + to.getId().hashCode();
        return result;
    }
}
