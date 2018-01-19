package mc.client.graph;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum NodeType {
  AUTOMATA_NODE(new Ellipse2D.Double(-10, -10, 20,20)),
  PETRINET_PLACE(new Ellipse2D.Double(-10, -10, 20,20)),
  PETRINET_TRANSITION(new Rectangle(-10, -10, 20,20));

  private final Shape nodeShape;
}
