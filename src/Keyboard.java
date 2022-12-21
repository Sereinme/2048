import java.awt.event.KeyEvent;

public class Keyboard {

    public static boolean[] pressed = new boolean[256];
    public static boolean[] prev = new boolean[256];

    // static class
    private Keyboard() {}

    public static void update() {
        for (int i = 0; i < 256; i++)
            prev[i] = pressed[i];
    }

    public static void keyPressed(KeyEvent e) {
        pressed[e.getKeyCode()] = true;
    }

    public static void keyReleased(KeyEvent e) {
        pressed[e.getKeyCode()] = false;
    }

    public static boolean typed(int key) {
        return !pressed[key] && prev[key];
    }
}
