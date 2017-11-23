package mc.compiler.iterator;

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
        return current++;
    }

    @Override
    public boolean hasNext(){
        return current <= end;
    }
}
