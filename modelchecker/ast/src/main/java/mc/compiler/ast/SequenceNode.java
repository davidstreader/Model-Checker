package mc.compiler.ast;

import mc.util.Location;

/**
 * SequenceNode represents a transition to another node.
 * <p>
 * The syntax of this is: {@code b->}
 *
 * @author David Sheridan
 * @author Sanjay Govind
 * @author Jacob Beal
 * @see InterruptNode
 * @see ActionLabelNode
 */
public class SequenceNode extends ASTNode {

  /**
   * The label of the transition.
   */
  private ActionLabelNode from;

  /**
   * The next ASTNode (where this transitions to).
   */
  private ASTNode to;

  /**
   * New instance of SequenceNode.
   *
   * @param from     the label of the action. {@link #from}
   * @param to       the node where this transitions to. {@link #to}
   * @param location the location this node is within the users code {@link ASTNode#location}
   */
  public SequenceNode(ActionLabelNode from, ASTNode to, Location location) {
    super(location,"Sequence");
    this.from = from;
    this.to = to;
  }
  public String myString(){
    StringBuilder sb = new StringBuilder();
    sb.append("("+from.getAction());
    sb.append("->");
    sb.append(to.myString()+")");
    return sb.toString();
  }
  @Override
  public SequenceNode instantiate(String fromI , String toI) {
    return new SequenceNode(from,to.instantiate(fromI,toI), getLocation());
  }

  public ActionLabelNode getFrom() {
    return this.from;
  }

  public ASTNode getTo() {
    return this.to;
  }

  public void setFrom(ActionLabelNode from) {
    this.from = from;
  }

  public void setTo(ASTNode to) {
    this.to = to;
  }

  public String toString() {
    return "SequenceNode(from=" + this.getFrom() + ", to=" + this.getTo() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof SequenceNode)) return false;
    final SequenceNode other = (SequenceNode) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$from = this.getFrom();
    final Object other$from = other.getFrom();
    if (this$from == null ? other$from != null : !this$from.equals(other$from)) return false;
    final Object this$to = this.getTo();
    final Object other$to = other.getTo();
    if (this$to == null ? other$to != null : !this$to.equals(other$to)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof SequenceNode;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $from = this.getFrom();
    result = result * PRIME + ($from == null ? 43 : $from.hashCode());
    final Object $to = this.getTo();
    result = result * PRIME + ($to == null ? 43 : $to.hashCode());
    return result;
  }
}
