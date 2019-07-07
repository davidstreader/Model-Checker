package mc.compiler.token;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

@Data
@EqualsAndHashCode(callSuper = true)
public class CastToken extends Token{

    private String castType;

    public CastToken(String type, Location loc) {
        super(loc);
        castType = type;
    }

    @Override
    public String toString(){
        return castType;
    }
}