package mc.webserver.webobjects;

import lombok.Data;

@Data
/**
 * The information sent from a client to compile a process model
 */
public class CompileRequest {
    private String code;
    private Context context;
}
