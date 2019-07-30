package mc.compiler.ast;

import com.google.common.collect.ImmutableSet;
import mc.util.Location;

import java.util.*;

public class ConversionNode extends ASTNode {

  public static final Set validConversions = ImmutableSet.of("automata","petrinet");
  public static final Map<String,List<String>> nameMap;
  static {
    nameMap = new HashMap<>();
    nameMap.put("tokenRule" , Arrays.asList("petrinet", "automata"));
    nameMap.put("ownersRule", Arrays.asList("automata", "petrinet"));
  }

  public final String from;
  public final String to;

  private ASTNode process;

  /**
   * Instantiate an instance of ASTNode.
   *
   * @param location the location within the users code of the node {@link #location}
   */
  public ConversionNode(String from, String to, ASTNode process, Location location) {
    super(location,"Conversion");
    assert validConversions.contains(from) && validConversions.contains(to);
    this.from = from;
    this.to = to;
    this.process = process;
  }

  public String toString() {
    return "ConversionNode(from=" + this.from + ", to=" + this.to + ", process=" + this.process + ")";
  }

  public ASTNode getProcess() {
    return this.process;
  }
}
