package mc.exceptions;

import lombok.Getter;
import mc.util.Location;

/**
 * Created by sheriddavi on 27/01/17.
 */
public class CompilationException extends Exception{
    @Getter
    Location location;
    @Getter
    Class<?> clazz;
    public CompilationException(Class<?> clazz, String message, Location location) {
        super(clazz.getSimpleName()+": "+message);
        this.clazz = clazz;
        this.location = location;
    }

    public CompilationException(Class<?> clazz, String message) {
        this(clazz,message,null);
    }
}
