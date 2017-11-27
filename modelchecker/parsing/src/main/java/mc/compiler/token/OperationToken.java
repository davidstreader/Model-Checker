package mc.compiler.token;

import mc.util.Location;

/**
 * Created by sheriddavi on 10/02/17.
 */
public class OperationToken extends Token {

    public OperationToken(Location location){
        super(location);
    }

    public String toString(){
        return "operation";
    }
}
