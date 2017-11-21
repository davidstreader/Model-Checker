package mc.webserver.webobjects;

import lombok.Data;

@Data
/**
 * The information sent from a client to compile a process model
 */
public class CompileRequest {
    private String code; // Code that the user wants to be compiled
    private Context context;
}
