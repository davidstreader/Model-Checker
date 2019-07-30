package mc.compiler.ast;

/**
 * A dummy node, for testing purposes only.
 *
 * @author David Sheridan
 * @author Sanjay Govind
 * @author Jacob Beal
 * @see ASTNode
 */
public class EmptyTestOnlyNode extends ASTNode {

  /**
   * Initialise the Node.
   */
  public EmptyTestOnlyNode() {
    super(null,"EmptyTest");
  }

  public String toString() {
    return "EmptyTestOnlyNode()";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof EmptyTestOnlyNode)) return false;
    final EmptyTestOnlyNode other = (EmptyTestOnlyNode) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof EmptyTestOnlyNode;
  }

  public int hashCode() {
    int result = super.hashCode();
    return result;
  }
}
