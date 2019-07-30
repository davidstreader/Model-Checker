package mc.compiler;

import java.util.Observable;


public class CompilationObservable extends Observable {

    private static final CompilationObservable instance = new CompilationObservable();

    private CompilationObservable(){}

  public static CompilationObservable getInstance() {
    return CompilationObservable.instance;
  }

  //package-private
    public void updateClient(CompilationObject c){
        setChanged();
        notifyObservers(c);
    }

}
