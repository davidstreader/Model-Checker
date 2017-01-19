package mc.compiler.iterator;

/**
 * Created by sheriddavi on 19/01/17.
 */
public class RangeIterator<Integer> implements IndexIterator<Integer> {

    // fields
    private int start;
    private int end;
    private int current;

    public RangeIterator(int start, int end){
        this.start = start;
        this.end = end;
        this.current = start;
    }

    @Override
    public Integer next(){
        return null;
    }

    @Override
    public boolean hasNext(){
        return current <= end;
    }
}
