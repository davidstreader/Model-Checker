package mc.processmodels.automata.serializers;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import mc.processmodels.automata.AutomatonEdge;

public class JSONEdgeSerializer extends StdSerializer<AutomatonEdge> {

  public JSONEdgeSerializer() {
    super(AutomatonEdge.class);
  }

  @Override
  public void serialize(AutomatonEdge value, JsonGenerator jgen, SerializerProvider provider)
      throws IOException {
    jgen.writeStartObject();
    jgen.writeObjectField("id", value.getId());
    jgen.writeObjectField("label", value.getLabel());
    jgen.writeObjectField("to", value.getTo().getId());
    jgen.writeObjectField("from", value.getFrom().getId());
//        Todo we need to rewrite this if we want use it. This use to include metadata as the main way of seralizing it.
    jgen.writeEndObject();
  }

}
