package mc.compiler.iterator;

import java.util.List;
import java.util.NoSuchElementException;


public class SetIterator implements IndexIterator<String> {

    // fields
    private List<String> set;
    private int index;

    public SetIterator(List<String> set){
        this.set = set;
        this.index = 0;
    }

    @Override
    public String next() {
        if(hasNext())
            return set.get(index++);
        throw new NoSuchElementException();
    }

    @Override
    public boolean hasNext() {
        return index < set.size();
    }
}
