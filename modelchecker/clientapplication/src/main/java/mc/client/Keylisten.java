package mc.client;

import javafx.scene.input.KeyCode;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Keylisten extends KeyAdapter {

    private Boolean xKey = false;

    public Keylisten(Boolean k) {
        xKey = k;
    }
    public void keyPressed(KeyEvent e) {
        System.out.println("You pressed "+e.getKeyChar());
        if (e.getKeyChar()=='x' ) {
            xKey = true;
            System.out.println("You pressed X");
        }
    }
    public void keyReleased(KeyEvent e) {
        System.out.println("You released "+ e.getKeyChar());
        if (e.getKeyChar()=='x' ) {
            xKey = false;
            System.out.println("You released X");
        }
    }
    public Boolean isxKey() {return xKey;}
}
