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
    START   (new Color(0,255,0,100)),
    START1  (new Color(5,240,5,90)),
    START2  (new Color(0,180,30,90)),
    START3  (new Color(30,180,0,90)),
    START4  (new Color(50,180,0,80)),
    START5  (new Color(0,180,50,80)),
    STOP    (Color.BLUE),
    ERROR   (Color.RED),
    NOMINAL (Color.LIGHT_GRAY),

    SELECT  (Color.CYAN);
    private final Color colorNodes;
}
