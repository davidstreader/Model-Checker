package mc.compiler.iterator;

import mc.compiler.ast.ASTNode;
import mc.compiler.ast.IndexExpNode;
import mc.compiler.ast.RangeNode;
import mc.compiler.ast.SetNode;

import java.util.ArrayList;
import java.util.Iterator;

public interface IndexIterator<E> extends Iterator<E> {

    static IndexIterator construct(ASTNode astNode){
        while(astNode instanceof IndexExpNode){
            astNode = ((IndexExpNode)astNode).getRange();
        }

        if(astNode instanceof RangeNode){
            RangeNode range = (RangeNode)astNode;
            return new RangeIterator(range.getStart(), range.getEnd());
        }
        else if(astNode instanceof SetNode){
            SetNode set = (SetNode)astNode;
            return new SetIterator(new ArrayList<>(set.getSet()));
        }

        throw new IllegalArgumentException("incorrect ast node");
    }

}
