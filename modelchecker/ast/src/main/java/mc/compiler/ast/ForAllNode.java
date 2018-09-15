package mc.compiler.ast;


import com.google.common.collect.ImmutableSet;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This represents an the quantifier \forall. Used to compute Galois connectons
 *   forall{X,..}( operation )
 *   X,...  is held in vars  and operation in op
 *
 * @author David Streader
 * @see ASTNode
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ForAllNode extends OperationNode {

  private List<String> vars;
  private OperationNode op;
  /**
   * The symbol used in the operation.
   * <p>
   * e.g. {@code ~}
   */


  /**
   * first implies second
   *
   * @param Operation     the first Operation
   * @param location         the location within the users code where this takes
   *                         place {@link ASTNode#location}
   */
  public ForAllNode( OperationNode Operation, List<String> Vars, Location location) {
    // super(location,"Implies");
    super(location);
    if(Operation==null) {
      Throwable t = new Throwable(); t.printStackTrace();
      System.out.println("*******************\n\n");
    }
    op = Operation;
    this.vars = Vars;
    //this.setFlags(Collections.singletonList("*"));
    System.out.println("forAll Location "+vars+ " "+ op.getOperation()+" "+
                 op.getFlags()+" ");
  }
  public String myString(){
    StringBuilder sb = new StringBuilder();
    sb.append("forAll");
    if (vars!= null && vars.size()>0) sb.append(vars);
    sb.append(op.myString());
    return sb.toString();
  }

  public List<String> getBound(){
    return vars;
  }
}

