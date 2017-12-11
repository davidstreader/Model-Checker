package mc.compiler.iterator;

import java.util.NoSuchElementException;

/**
 * Created by sheriddavi on 19/01/17.
 */
public class RangeIterator implements IndexIterator<Integer> {

    // fields
    private int current;
    private int end;

    public RangeIterator(int start, int end){
        this.current = start;
        this.end = end;
    }

    @Override
    public Integer next(){
        if(current+1 < end)
            return current++;
        throw new NoSuchElementException();
    }

    @Override
    public boolean hasNext(){
        return current <= end;
    }
}
