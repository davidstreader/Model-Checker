package mc.processmodels.automata.generator;

public class BinaryNode {
  BinaryNode left;
  BinaryNode right;

  public BinaryNode(BinaryNode left, BinaryNode right) {
    this.left = left;
    this.right = right;
  }

  public BinaryNode() {
  }

  public BinaryNode getLeft() {
    return this.left;
  }

  public BinaryNode getRight() {
    return this.right;
  }
}
