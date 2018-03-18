package mc.ast.ast;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Object;
import com.rits.cloning.Cloner;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
//import mc.compiler.Guard;  // **cannot find symbol** Guard  NO IDEA why
// I have tried changing the package name to stop clashes
//import modelchecker.processmodels.src.main.java.mc.compiler.Guard
import mc.util.Location;



/**
 * ASTNode is the superclass of ASTNode implementations, it provides helper and common functions to
 * the specific implementation.
 *
 * @author David Sheridan
 * @author Sanjay Govind
 * @author Jacob Beal
 * @see AbstractSyntaxTree
 *
 */
@EqualsAndHashCode(exclude = {"location", "modelVariables", "guard"})
public abstract class ASTNode implements Serializable {

  @Getter
  private Set<String> references = new HashSet<>();
  @Getter
  private Set<String> fromReferences  = new HashSet<>();
  @Getter
  private Location location;
  @Getter
  @Setter
  private HashMap<String, Object> modelVariables;
  @Getter
  @Setter
  private Object guard;  //NASTY fix for **can not find symbol** Guard in import
  //private Guard guard;
  // Beware guard Object is a  Guard that contains both a guard and the Assignments

  /**
   * Instantiate an instance of ASTNode.
   *
   * @param location the location within the users code of the node {@link #location}
   */
  public ASTNode(Location location) {
    references = new HashSet<>();
    this.location = location;
  }


  /**
   * Stores text references to local states.
   *
   * @param reference the label of the reference
   * @see #references
   */
  public void addReference(String reference) {
    if (references == null)  references = new HashSet<>();

    references.add(reference);
  }
  public void addFromReference(String reference) {
    if (fromReferences == null)  fromReferences = new HashSet<>();

    fromReferences.add(reference);
  }

  /**
   * A method to find if the current ASTNode has a reference associated with it.
   *
   * @return if there is one or more references associated with the given ASTNode
   * @see #references
   */
  public boolean hasReferences() {
    return (references != null && references.size()>1);
  }

  /**
   * Clone the current Node.
   *
   * @return a deep copy of the current node.
   * @see Cloner
   */
  public ASTNode copy() {
    Cloner cloner = new Cloner();
    cloner.dontClone(Context.class);
    cloner.dontClone(Z3Object.class);
    cloner.dontClone(Expr.class);
    cloner.dontClone(BoolExpr.class);
    return cloner.deepClone(this);
  }
}
