package mc.compiler;

import lombok.Getter;

import java.util.Observable;


public class CompilationObservable extends Observable {

    @Getter
    private static final CompilationObservable instance = new CompilationObservable();

    private CompilationObservable(){}

    //package-private
    void updateClient(CompilationObject c){
        setChanged();
        notifyObservers(c);
    }

}
