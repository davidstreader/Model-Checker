package mc.webserver;

import lombok.Data;

@Data
class CompileRequest {
    private String code;
    private Context context;
}
