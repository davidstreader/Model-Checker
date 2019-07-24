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
    START1  (new Color(100,200,0,255)),
    START2  (new Color(0,200,100,255)),
    START3  (new Color(90,180,0,200)),
    START4  (new Color(0,180,90,200)),
    START5  (new Color(0,180,100,200)),

    STOP    (new Color(0,0,250,230)),
    STOP1    (new Color(70,0,220,200)),
    STOP2    (new Color(0,70,220,200)),
    STOP3    (new Color(0,40,180,200)),
    STOP4    (new Color(40,0,180,200)),
    STOP5    (new Color(20,20,200,200)),
    STOPSTART (new Color(250,0,220,255)),
    STOPSTART1 (new Color(220,0,150,245)),
    STOPSTART2 (new Color(180,0,150,235)),
    STOPSTART3 (new Color(200,100,150,225)),
    STOPSTART4 (new Color(250,150,50,215)),
    STOPSTART5 (new Color(250,180,0,255)),
    TEMP (new Color(250,180,0,255)),
    TEMP2 (new Color(255,90,0,255)),
    ERROR   (Color.DARK_GRAY),
    NOMINAL (Color.LIGHT_GRAY),
    SEQUENTIAL (new Color(50,0 ,0, 100)),
    CONCURRENT (new Color(15,15 ,15, 100)),
    SELECT  (Color.CYAN);

    private final Color colorNodes;

   /* public NodeStates(Color c) {
        colorNodes = c;
    } */
}
