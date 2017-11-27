package mc.process_models.automata.serializers;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import mc.process_models.automata.AutomatonEdge;

import java.io.IOException;

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
        jgen.writeObjectField("metaData", value.getMetaData());
        jgen.writeEndObject();
    }

}
