package mc.webserver.webobjects;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

@EqualsAndHashCode(callSuper = true)
@Data
/**
 * An error message with a stack trace to send back to the client
 */
public class ErrorMessage extends LogMessage {
    String type = "error";
    String stack = null;
    public ErrorMessage(String message, Location location) {
        super(message, false, true, location, -1,Thread.currentThread());
    }
    public ErrorMessage(String message, String stack, Location location) {
        super(message, false, true, location, -1, Thread.currentThread());
        this.stack = stack;
    }
}
