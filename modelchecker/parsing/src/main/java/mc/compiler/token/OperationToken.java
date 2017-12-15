package mc.compiler.token;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

@Data
@EqualsAndHashCode(callSuper = true)
public class OperationToken extends Token {

    public OperationToken(Location location){
        super(location);
    }

    @Override
    public String toString(){
        return "operation";
    }
}
