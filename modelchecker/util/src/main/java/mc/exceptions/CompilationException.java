package mc.exceptions;

import mc.util.Location;

/**
 * Created by sheriddavi on 27/01/17.
 */
public class CompilationException extends Exception{
    Location location;
    Class<?> clazz;
    public CompilationException(Class<?> clazz, String message, Location location) {
        super(clazz.getSimpleName()+"Exception: "+message);
        this.clazz = clazz;
        this.location = location;
    }

    public CompilationException(Class<?> clazz, String message) {
        this(clazz,message,null);
    }

  public Location getLocation() {
    return this.location;
  }

  public Class<?> getClazz() {
    return this.clazz;
  }
}
