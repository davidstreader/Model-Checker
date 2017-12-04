package mc.process_models.automata;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import mc.Constant;
import mc.compiler.Guard;
import mc.process_models.ProcessModelObject;
import mc.process_models.automata.serializers.JSONEdgeSerializer;

import java.util.Map;
import java.util.Set;

@JsonSerialize(using = JSONEdgeSerializer.class)
public class AutomatonEdge extends ProcessModelObject {

    @Getter
    @Setter
    private String label;

    @Getter
    @Setter
    private AutomatonNode from;

    @Getter
    @Setter
    private AutomatonNode to;

    @Getter
    @Setter
    private Guard guard;

    //Overriding the abstract class so we can see where these methods are being used
    // Temp code to find where the metadata is being set and used.
    public Map<String, Object> getMetaData(){
        System.out.println("Someone called getMetaData");
        return null;

    }

    public Object getMetaData(String key) {
        System.out.println("[Node]Asked for key: " + key);

        return null;
    }

    public void addMetaData(String key, Object value){
        System.out.println("[Node]Was told to add: " + key + " data: " + value.toString());

    }

    public void removeMetaData(String key) {
        System.out.println("[Node]Was told to remove: " + key);

    }

    public boolean hasMetaData(String key){
        System.out.println("[Node]Asked if " + key + " exists");
        return false;
    }

    public Set<String> getMetaDataKeys() {
        System.out.println("[Node]Was told to get keyset");
        return null;
    }

    //End temp

    public AutomatonEdge(String id, String label, AutomatonNode from, AutomatonNode to){
        super(id,"edge");
        this.label = label;
        this.from = from;
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
            "\tmetadata:" + getGuard() + "\n" +
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
