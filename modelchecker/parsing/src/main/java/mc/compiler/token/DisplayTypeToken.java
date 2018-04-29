package mc.compiler.token;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;
/*
  Could be dead code?
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DisplayTypeToken extends Token {

    private final String processType;

    public DisplayTypeToken(String processType, Location location){
        super(location);
        this.processType = processType;
    }

    @Override
    public String toString(){
        return processType;
    }
}