package mc.webserver.webobjects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class SendObject {
    Object data;
    String event;
}
