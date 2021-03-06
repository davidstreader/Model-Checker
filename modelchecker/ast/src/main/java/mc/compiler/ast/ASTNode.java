package mc.compiler.ast;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Object;
import com.rits.cloning.Cloner;
import mc.util.Location;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

//import mc.plugins.*;
//import modelchecker.processmodels.src.main.java.mc.plugins;
//import mc.compiler.Guard;  // Can not resolve symbol Guard  NO IDEA why
//import modelchecker.processmodels.src.main.java.mc.compiler.Guard



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
public abstract class ASTNode implements Serializable {
  private String name = "";
  private Set<String> references;
  private Set<String> fromReferences;
  private Location location;
  private HashMap<String, Object> modelVariables;  // appears to be $i used in SYMBOLIC processes
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
  public ASTNode instantiate(String from , String to) {
    return this;
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
  public String wholeString(){//Nice idea but NOT WORKING
    return myString("","");
  }

  private String myString(String ofset,String sofar) {
    String offset = ofset+">";
    if (this instanceof IdentifierNode) {
      sofar+=(offset+" " + this.getName()+"\n");
      //System.out.println(" IdentifierNode");
    } else if (this instanceof ForAllNode) {
      sofar+=(offset+" " + this.getName()+" "+ ((ForAllNode) this).getBound() + " \n");
      sofar+=((ASTNode) ((ForAllNode) this).getOp()).myString(offset,sofar);
    } else if (this instanceof ImpliesNode){

      sofar+=(offset+" " + this.getName()+"\n");
      if (((ImpliesNode)this).getFirstProcess()== null) System.out.println("NULL 1");
      else sofar+=((ImpliesNode) this).getFirstProcess().myString(offset,sofar);
      if (((ImpliesNode)this).getFirstProcess()== null) System.out.println("NULL 2");
      else sofar+=((ImpliesNode) this).getSecondProcess().myString(offset,sofar);
      if (getFromReferences() != null) {
        //System.out.println(" ImpliesNode  forall "+((ForAllStatementNode)getFromReferences()).getVariables());
        sofar+= "forall "+((ForAllStatementNode)getFromReferences()).getVariables();
      } else {
        //System.out.println(" ImpliesNode");
        sofar+= " ImpliesNode";
      }
    } else if (this instanceof OperationNode){
      if (getFromReferences() != null) {
        //System.out.println(" OperationNode  forall "+((ForAllStatementNode)getFromReferences()).getVariables());
        sofar+= "forall "+((ForAllStatementNode)getFromReferences()).getVariables();
      } else {
        //System.out.println(" OperationNode");
        sofar+= "OperationNode";
      }
      sofar+=(offset+" " + this.getName()+"\n");
      if (((OperationNode)this).getFirstProcess()== null) System.out.println("NULL 1");
      else sofar+=((OperationNode) this).getFirstProcess().myString(offset,sofar);
      if (((OperationNode)this).getFirstProcess()== null) System.out.println("NULL 2");
      else sofar+=((OperationNode) this).getSecondProcess().myString(offset,sofar);

    } else if (this instanceof ChoiceNode){
      //System.out.println(" ChoiceNode");
      sofar+=(offset+" " + this.getName()+"\n");
      sofar+=((ChoiceNode) this).getFirstProcess().myString(offset,sofar);
      sofar+=((ChoiceNode) this).getSecondProcess().myString(offset,sofar);

    } else  if (this instanceof CompositeNode) {
      //System.out.println(" CompositeNode");
      sofar+=(offset+" " + this.getName()+"\n");
    if (((CompositeNode)this).getFirstProcess()== null) System.out.println("NULL 1");
    else sofar+=((CompositeNode) this).getFirstProcess().myString(offset,sofar);
    if (((CompositeNode)this).getSecondProcess()== null)  System.out.println("NULL 2");
    else  sofar+=((CompositeNode) this).getSecondProcess().myString(offset,sofar);
    } else  if (this instanceof FunctionNode) {
      //System.out.println(" FunctionNode");
       sofar+=(offset+" " + this.getName()+"\n");
      for (ASTNode an:  ((FunctionNode) this).getProcesses()) {
        sofar+=an.myString(offset,sofar);
      };

    } else if(this instanceof ProcessRootNode) {
      //System.out.println(" ProcessRootNode");
      sofar+=(offset+" " + this.getName()+"\n");
      ((ProcessRootNode) this).getProcess().myString(offset,sofar);
    } else if (this instanceof IfStatementExpNode) {
       //System.out.println(" IfNode");
      sofar+=(offset+" " + this.getName()+"\n");
      sofar+=((IfStatementExpNode) this).getTrueBranch().myString(offset,sofar);
      if (((IfStatementExpNode) this).hasFalseBranch()) {
        sofar+=(offset+" " + this.getName()+"\n");
        sofar+=((IfStatementExpNode) this).getFalseBranch().myString(offset,sofar);
      }
    } else if (this instanceof SequenceNode) {
      sofar+=(offset+" " + this.getName()+"\n");
     // System.out.println(" SequenceNode");
      sofar+=(offset+" " + this.getName()+ " "+((SequenceNode) this).getEventLabel().getAction()+"\n");
      sofar+=((SequenceNode) this).getTo().myString(offset,sofar);
    }  else {
      sofar+=(offset+" " + this.getName()+"\n");
      System.out.println(" DO NOT KNOW Node "+ this.getName());
      //Throwable t = new Throwable();
      //t.printStackTrace();
    }
return sofar;
  }

  public String myString() {
    return "define for "+ this.getClass().getName();
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ASTNode)) return false;
    final ASTNode other = (ASTNode) o;
    if (!other.canEqual((Object) this)) return false;
    final Object this$name = this.name;
    final Object other$name = other.name;
    if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
    final Object this$references = this.references;
    final Object other$references = other.references;
    if (this$references == null ? other$references != null : !this$references.equals(other$references)) return false;
    final Object this$fromReferences = this.fromReferences;
    final Object other$fromReferences = other.fromReferences;
    if (this$fromReferences == null ? other$fromReferences != null : !this$fromReferences.equals(other$fromReferences))
      return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ASTNode;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $name = this.name;
    result = result * PRIME + ($name == null ? 43 : $name.hashCode());
    final Object $references = this.references;
    result = result * PRIME + ($references == null ? 43 : $references.hashCode());
    final Object $fromReferences = this.fromReferences;
    result = result * PRIME + ($fromReferences == null ? 43 : $fromReferences.hashCode());
    return result;
  }

  public String getName() {
    return this.name;
  }

  public Set<String> getReferences() {
    return this.references;
  }

  public Set<String> getFromReferences() {
    return this.fromReferences;
  }

  public Location getLocation() {
    return this.location;
  }

  public HashMap<String, Object> getModelVariables() {
    return this.modelVariables;
  }

  public Object getGuard() {
    return this.guard;
  }

  public void setModelVariables(HashMap<String, Object> modelVariables) {
    this.modelVariables = modelVariables;
  }

  public void setGuard(Object guard) {
    this.guard = guard;
  }
}
