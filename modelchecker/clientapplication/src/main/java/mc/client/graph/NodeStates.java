package mc.client.graph;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.swing.plaf.ColorUIResource;
import java.awt.*;

/**
 * Created by bealjaco on 5/12/17.
 */
@RequiredArgsConstructor
@Getter
public enum NodeStates {
    START   (Color.GREEN),
    START1  (new Color(5,240,5,90)),
    START2  (new Color(0,200,10,80)),
    START3  (new Color(10,180,0,80)),
    STOP    (Color.BLUE),
    ERROR   (Color.RED),
    NOMINAL (Color.LIGHT_GRAY),

    SELECT  (Color.CYAN);
    private final Color colorNodes;
}
