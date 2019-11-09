package mc.compiler;

import java.util.Observable;

/*
    Constructor is private getInstance returns the static Object
    THIS OBJECT  seems to be redundent? Rethink later
 */
public class CompilationObservable extends Observable {

    private static final CompilationObservable instance = new CompilationObservable();

    private CompilationObservable(){}

  public static CompilationObservable getInstance() {
    return CompilationObservable.instance;
  }

  //package-private
    /* This notifies the observers */
    public void updateClient(CompilationObject c){
        setChanged();
        notifyObservers(c);
    }

}
