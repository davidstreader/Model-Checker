package mc.compiler.token;
import lombok.Getter;
import mc.util.Location;
/**
 * Created by smithjord3 on 6/12/17.
 */
public class CastToken extends Token{

    @Getter
    private String castType;

    public CastToken( String type, Location loc) {
        super(loc);
        castType = type;
    }

}
