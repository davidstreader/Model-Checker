package mc.process_models.automata;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import mc.Constant;
import mc.process_models.ProcessModelObject;

import java.io.IOException;

/**
 * Created by sheriddavi on 24/01/17.
 */
@JsonSerialize(using = AutomatonEdge.AutomatonEdgeSerializser.class)
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
        StringBuilder builder = new StringBuilder();
        builder.append("edge{\n");
        builder.append("\tid:" + getId() + "\n");
        builder.append("\tlabel:" + label + "\n");
        builder.append("\tfrom:" + from.getId() + "\n");
        builder.append("\tto:" + to.getId() + "\n");
        builder.append("}");

        return builder.toString();
    }
  public static class AutomatonEdgeSerializser extends StdSerializer<AutomatonEdge> {

    public AutomatonEdgeSerializser() {
      super(AutomatonEdge.class, true);
    }

    @Override
    public void serialize(AutomatonEdge value, JsonGenerator jgen, SerializerProvider provider)
      throws IOException, JsonGenerationException {
      jgen.writeStartObject();
      jgen.writeObjectField("id", value.getId());
      jgen.writeObjectField("label", value.getLabel());
      jgen.writeObjectField("to", value.getTo().getId());
      jgen.writeObjectField("from", value.getFrom().getId());
      //TODO: add locationset
      jgen.writeObjectField("locationSet", null);
      jgen.writeObjectField("metaData", value.getMetaData());
      jgen.writeEndObject();

    }

  }
}
