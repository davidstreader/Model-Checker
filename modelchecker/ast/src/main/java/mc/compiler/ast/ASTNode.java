package mc.compiler.ast;

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
//import mc.compiler.Guard;  // Can not resolve symbol Guard  NO IDEA why
//import modelchecker.processmodels.src.main.java.mc.compiler.Guard
import mc.util.Location;



/**
 * ASTNode is the superclass of ASTNode implementations, it provides helper and common functions to
 * the specific implementation.
 * THIS IS A CRAP DESIGN - all the important information is now hidden. Makes testing
 * very hard!
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
  private String name = "";
  @Getter
  private Set<String> references;
  @Getter
  private Set<String> fromReferences;
  @Getter
  private Location location;
  @Getter
  @Setter
  private HashMap<String, Object> modelVariables;  // appears to be $i used in SYMBOLIC processes
  @Getter
  @Setter
  private Object guard;  //NASTY fix for "can not resolve symbol" Guard in import
  //private Guard guard;
  // Beware guard Object is a  Guard that contains both a guard and the Assignments

  /**
   * Instantiate an instance of ASTNode.
   *
   * @param location the location within the users code of the node {@link #location}
   */
  public ASTNode(Location location, String name) {
    references = null;
    this.location = location;
    this.name = name;
  }


  /**
   * Stores text references to local states.
   *
   * @param reference the label of the reference
   * @see #references
   */
  public void addReference(String reference) {
    if (references == null) {
      references = new HashSet<>();
    }
    references.add(reference);
  }
  public void addFromReference(String reference) {
    if (fromReferences == null) {
      fromReferences = new HashSet<>();
    }
    fromReferences.add(reference);
  }
  /**
   * A method to find if the current ASTNode has a reference associated with it.
   *
   * @return if there is one or more references associated with the given ASTNode
   * @see #references
   */
  public boolean hasReferences() {
    return references != null;
  }

  public String toString(){
    return "ASTNode "+ location.toString()+" "+ modelVariables.keySet();
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

  //For debugging
  public String myString(){
    return myString("","");
  }
  private String myString(String ofset,String sofar) {
    String offset = ofset+">";
    if (this instanceof IdentifierNode) {
      sofar+=(offset+" " + this.getName()+"\n");
      System.out.println(" IdentifierNode");
    } else if (this instanceof ImpliesNode){
      System.out.println(" ImpliesNode");
      sofar+=(offset+" " + this.getName()+"\n");
      if (((ImpliesNode)this).getFirstProcess()== null) System.out.println("NULL 1");
      else((ImpliesNode) this).getFirstProcess().myString(offset,sofar);
      if (((ImpliesNode)this).getFirstProcess()== null) System.out.println("NULL 2");
      else((ImpliesNode) this).getSecondProcess().myString(offset,sofar);
    } else if (this instanceof OperationNode){
      System.out.println(" OperationNode");
      sofar+=(offset+" " + this.getName()+"\n");
      if (((OperationNode)this).getFirstProcess()== null) System.out.println("NULL 1");
      else((OperationNode) this).getFirstProcess().myString(offset,sofar);
      if (((OperationNode)this).getFirstProcess()== null) System.out.println("NULL 2");
      else((OperationNode) this).getSecondProcess().myString(offset,sofar);

    } else if (this instanceof ChoiceNode){
      System.out.println(" ChoiceNode");
      sofar+=(offset+" " + this.getName()+"\n");
      ((ChoiceNode) this).getFirstProcess().myString(offset,sofar);
      ((ChoiceNode) this).getSecondProcess().myString(offset,sofar);

    } else  if (this instanceof CompositeNode) {
      System.out.println(" CompositeNode");
      sofar+=(offset+" " + this.getName()+"\n");
    if (((CompositeNode)this).getFirstProcess()== null) System.out.println("NULL 1");
    else ((CompositeNode) this).getFirstProcess().myString(offset,sofar);
    if (((CompositeNode)this).getSecondProcess()== null)  System.out.println("NULL 2");
    else  ((CompositeNode) this).getSecondProcess().myString(offset,sofar);
    } else  if (this instanceof FunctionNode) {
      System.out.println(" FunctionNode");
       sofar+=(offset+" " + this.getName()+"\n");
      for (ASTNode an:  ((FunctionNode) this).getProcesses()) {
        an.myString(offset,sofar);
      };

    } else if(this instanceof ProcessRootNode) {
      System.out.println(" ProcessRootNode");
      sofar+=(offset+" " + this.getName()+"\n");
      ((ProcessRootNode) this).getProcess().myString(offset,sofar);
    } else if (this instanceof IfStatementExpNode) {
       System.out.println(" IfNode");
      sofar+=(offset+" " + this.getName()+"\n");
      ((IfStatementExpNode) this).getTrueBranch().myString(offset,sofar);
      if (((IfStatementExpNode) this).hasFalseBranch()) {
        sofar+=(offset+" " + this.getName()+"\n");
        ((IfStatementExpNode) this).getFalseBranch().myString(offset,sofar);
      }
    } else if (this instanceof SequenceNode) {
      sofar+=(offset+" " + this.getName()+"\n");
      System.out.println(" SequenceNode");
      sofar+=(offset+" " + this.getName()+ " "+((SequenceNode) this).getFrom().getAction()+"\n");
      ((SequenceNode) this).getTo().myString(offset,sofar);
    }  else {
      sofar+=(offset+" " + this.getName()+"\n");
      System.out.println(" DO NOT KNOW Node "+ this.getName());
      Throwable t = new Throwable();
      t.printStackTrace();
    }
return sofar;
  }
}
