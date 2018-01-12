package mc.compiler.ast;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

/**
 * This contains several distinct states
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RangesNode extends ASTNode {

  private List<IndexNode> ranges;

  public RangesNode(List<IndexNode> ranges, Location location) {
    super(location);
    this.ranges = ranges;
  }
}
