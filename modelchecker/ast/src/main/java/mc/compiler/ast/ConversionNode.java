package mc.compiler.ast;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.ToString;
import mc.util.Location;

@ToString
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

  @Getter
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
}
