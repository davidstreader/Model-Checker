package mc.compiler.iterator;

import mc.compiler.ast.ASTNode;
import mc.compiler.ast.RangeNode;
import mc.compiler.ast.SetNode;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by sheriddavi on 19/01/17.
 */
public interface IndexIterator<E> extends Iterator<E> {

    static IndexIterator construct(ASTNode astNode){
        if(astNode instanceof RangeNode){
            RangeNode range = (RangeNode)astNode;
            return new RangeIterator(range.getStart(), range.getEnd());
        }
        else if(astNode instanceof SetNode){
            SetNode set = (SetNode)astNode;
            return new SetIterator<String>(new ArrayList<String>(set.getSet()));
        }

        throw new IllegalArgumentException("incorrect ast node");
    }

}
