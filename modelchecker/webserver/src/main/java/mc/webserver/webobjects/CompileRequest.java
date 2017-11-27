package mc.webserver.webobjects;

import lombok.Data;
import mc.webserver.Context;

@Data
/**
 * The information sent from a client to compile a process model
 */
public class CompileRequest {
    private String code; // Code that the user wants to be compiled
    private Context context; // Web page settings, how many nodes displayable... etc
}
