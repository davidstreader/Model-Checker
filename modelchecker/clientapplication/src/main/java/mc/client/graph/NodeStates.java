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
    START   (new Color(0,255,0,255)),
    START1  (new Color(100,150,0,255)),
    START2  (new Color(0,150,100,255)),
    START3  (new Color(100,150,0,90)),
    START4  (new Color(0,255,0,90)),
    START5  (new Color(0,150,150,90)),
    STOP    (Color.BLUE),
    ERROR   (Color.RED),
    NOMINAL (Color.LIGHT_GRAY),

    SELECT  (Color.CYAN);
    private final Color colorNodes;
}
