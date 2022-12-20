import java.awt.*;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.image.BufferedImage;

public class Tile {

    // public variables
    public static final int WIDTH = 80;
    public static final int HEIGHT = 80;
    public static final int SLIDE_SPEED = 20;
    public static final int ARC_WIDTH = 15;
    public static final int ARC_HEIGHT = 15;

    // private variables
    private int value;
    private BufferedImage tileImage;
    private Color background = new Color(0xedc22e);
    private Color text;
    private Font font;
    private int x;
    private int y;

    public Tile(int value, int x, int y) {
        this.value = value;
        this.x = x;
        this.y = y;
        tileImage = new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_ARGB);
        drawImage();
    }

    private void drawImage() {
        Graphics2D g = (Graphics2D) tileImage.getGraphics();
        switch (value) {
            case 2:    background = new Color(0xeee4da);
            case 4:    background = new Color(0xede0c8);
            case 8:    background = new Color(0xf2b179);
            case 16:   background = new Color(0xf59563);
            case 32:   background = new Color(0xf67c5f);
            case 64:   background = new Color(0xf65e3b);
            case 128:  background = new Color(0xedcf72);
            case 256:  background = new Color(0xedcc61);
            case 512:  background = new Color(0xedc850);
            case 1024: background = new Color(0xedc53f);
            case 2048: background = new Color(0xedc22e);
                break;
            default: background = new Color(0xedc22e);
        }
        text = new Color(value < 16 ? 0x776e65 : 0xf9f6f2);

        g.setColor(new Color(0,0,0,0));
        g.fillRect(0,0,WIDTH,HEIGHT);

        // background
        g.setColor(background);
        g.fillRoundRect(0,0,WIDTH, HEIGHT,ARC_WIDTH,ARC_HEIGHT);

        // foreground
        g.setColor(text);
        if (value <= 64)
            font = Game.main.deriveFont(36f);
        else
            font = Game.main;
        g.setFont(font);
        int drawX = WIDTH / 2 - DrawUtils.getMessageWidth("" + value, font, g) / 2;
        int drawY = HEIGHT / 2 - DrawUtils.getMessageHeight("" + value, font, g) / 2;
        g.drawString("" + value, drawX, drawY);
        g.dispose();

    }

}
