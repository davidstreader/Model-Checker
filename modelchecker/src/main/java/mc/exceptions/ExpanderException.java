package mc.exceptions;

import mc.util.Location;

/**
 * Created by sheriddavi on 27/01/17.
 */
public class ExpanderException extends CompilationException {

    public ExpanderException(String identifier, String message){
        super("ExpanderException(thrown in " + identifier + "): " + message);
    }

}
