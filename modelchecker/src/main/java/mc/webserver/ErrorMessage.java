package mc.webserver;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

@EqualsAndHashCode(callSuper = true)
@Data
class ErrorMessage extends LogMessage {
    String type = "error";
    String stack = null;
    ErrorMessage(String message, Location location) {
        super(message, false, true, location, -1);
    }
    ErrorMessage(String message, String stack, Location location) {
        super(message, false, true, location, -1);
        this.stack = stack;
    }
}
