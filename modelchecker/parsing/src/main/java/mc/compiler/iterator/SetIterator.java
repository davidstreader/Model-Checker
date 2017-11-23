package mc.compiler.iterator;

import java.util.List;


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
        return set.get(index++);
    }

    @Override
    public boolean hasNext() {
        return index < set.size();
    }
}
