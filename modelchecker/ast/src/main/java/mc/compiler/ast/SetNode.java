package mc.compiler.ast;

import mc.util.Location;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SetNode contains a set of transitions, or ranges.
 * <p>
 * This is used for indexing, function optional arguments and for event hiding.
 * Syntactically this is {@code SET :: "{" (ACTION ",")* ACTION "}"}
 *
 * @author David Sheridan
 * @author Sanjay Govind
 * @author Jacob Beal
 * @see HideNode
 * @see FunctionNode
 * @see ASTNode
 */
public class SetNode extends ASTNode {

  /**
   * The string items in the set.
   */
  private List<String> set;

  /**
   * The items within the set that are ranges.
   */
  private Map<Integer, RangesNode> rangeMap;

  /**
   * Instantiate a new set node, with ranges in.
   *
   * @param set      the strings within the set. {@link #set}
   * @param rangeMap the ranges (e.g. {@code [0..2]}) {@link #rangeMap}
   * @param location the location of this node within the users code {@link ASTNode#location}
   */
  public SetNode(List<String> set, Map<Integer, RangesNode> rangeMap, Location location) {
    super(location,"Set");
    this.set = set;
    this.rangeMap = rangeMap;
  }

  /**
   * Instantiate a new set node, without ranges in.
   *
   * @param set      the strings within the set. {@link #set}
   * @param location the location of this node within the users code {@link ASTNode#location}
   */
  public SetNode(List<String> set, Location location) {
    super(location,"Set");
    this.set = set;
    this.rangeMap = new HashMap<>();
  }

  public String myString(){
    StringBuilder sb = new StringBuilder();
    sb.append("Set "+ set);
    rangeMap.forEach((k,v)->sb.append(k+"->"+v.myString()+", "));
    return sb.toString();
  }

  public List<String> getSet() {
    return this.set;
  }

  public Map<Integer, RangesNode> getRangeMap() {
    return this.rangeMap;
  }

  public void setSet(List<String> set) {
    this.set = set;
  }

  public void setRangeMap(Map<Integer, RangesNode> rangeMap) {
    this.rangeMap = rangeMap;
  }

  public String toString() {
    return "SetNode(set=" + this.getSet() + ", rangeMap=" + this.getRangeMap() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof SetNode)) return false;
    final SetNode other = (SetNode) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$set = this.getSet();
    final Object other$set = other.getSet();
    if (this$set == null ? other$set != null : !this$set.equals(other$set)) return false;
    final Object this$rangeMap = this.getRangeMap();
    final Object other$rangeMap = other.getRangeMap();
    if (this$rangeMap == null ? other$rangeMap != null : !this$rangeMap.equals(other$rangeMap)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof SetNode;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $set = this.getSet();
    result = result * PRIME + ($set == null ? 43 : $set.hashCode());
    final Object $rangeMap = this.getRangeMap();
    result = result * PRIME + ($rangeMap == null ? 43 : $rangeMap.hashCode());
    return result;
  }
}
