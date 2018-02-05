package mc.client.graph;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.*;

/**
 * Created by bealjaco on 5/12/17.
 */
@RequiredArgsConstructor
@Getter
public enum NodeStates {
    START   (Color.GREEN),
    STOP    (Color.BLUE),
    ERROR   (Color.RED),
    NOMINAL (Color.LIGHT_GRAY),

    SELECT  (Color.CYAN);
    private final Color colorNodes;
}
