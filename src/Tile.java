import java.awt.*;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.geom.AffineTransform;
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
    private Color background;
    private Color text;
    private Font font;
    private int x;
    private int y;
    private Point slideTo;
    private boolean canCombine = true;

    // Animation variables
    private boolean beginningAnimation = true;
    private double scaleFirst = 0.1;
    private BufferedImage beginningImage;
    private boolean combineAnimation = false;
    private double scaleCombine = 1.3;
    private BufferedImage combineImage;

   public Tile(int value, int x, int y) {
        this.value = value;
        this.x = x;
        this.y = y;
        this.slideTo = new Point(x, y);
        tileImage = new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_ARGB);
        beginningImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        combineImage = new BufferedImage(WIDTH*2, HEIGHT*2, BufferedImage.TYPE_INT_ARGB);
       drawImage();
    }

    private void drawImage() {
        Graphics2D g = (Graphics2D) tileImage.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        switch (value) {
            case 2:    background = new Color(0xeee4da); break;
            case 4:    background = new Color(0xede0c8); break;
            case 8:    background = new Color(0xf2b179); break;
            case 16:   background = new Color(0xf59563); break;
            case 32:   background = new Color(0xf67c5f); break;
            case 64:   background = new Color(0xf65e3b); break;
            case 128:  background = new Color(0xedcf72); break;
            case 256:  background = new Color(0xedcc61); break;
            case 512:  background = new Color(0xedc850); break;
            case 1024: background = new Color(0xedc53f); break;
            case 2048: background = new Color(0xedc22e); break;
            default: background = new Color(0xedbf2b);
        }
        text = new Color(value < 16 ? 0x776e65 : 0xf9f6f2);

        g.setColor(new Color(0,0,0,0));
        g.fillRect(0,0,WIDTH,HEIGHT);

        // background
        g.setColor(background);
        g.fillRoundRect(0,0,WIDTH, HEIGHT,ARC_WIDTH,ARC_HEIGHT);

        // foreground
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(text);
        if (value <= 64)
            font = Game.main.deriveFont(36f);
        else
            font = Game.main;
        g.setFont(font);
        int drawX = WIDTH / 2 - DrawUtils.getMessageWidth("" + value, font, g) / 2;
        int drawY = HEIGHT / 2 + DrawUtils.getMessageHeight("" + value, font, g) / 2;
        g.drawString("" + value, drawX, drawY);
        g.dispose();
    }

    public void update() {
        if (beginningAnimation) {
            AffineTransform transform = new AffineTransform();
            transform.translate(WIDTH / 2 - scaleFirst * WIDTH / 2, HEIGHT / 2 - scaleFirst * HEIGHT / 2);
            transform.scale(scaleFirst, scaleFirst);
            Graphics2D g2d = (Graphics2D) beginningImage.getGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setColor(new Color(0,0,0,0));
            g2d.fillRect(0,0,WIDTH,HEIGHT);
            g2d.drawImage(tileImage, transform, null);
            scaleFirst += 0.1;
            g2d.dispose();
            if (scaleFirst >= 1) beginningAnimation = false;
        }
        else if (combineAnimation) {
            AffineTransform transform = new AffineTransform();
            transform.translate(WIDTH / 2 - scaleCombine * WIDTH / 2, HEIGHT / 2 - scaleCombine * HEIGHT / 2);
            transform.scale(scaleCombine, scaleCombine);
            Graphics2D g2d = (Graphics2D) combineImage.getGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setColor(new Color(0,0,0,0));
            g2d.fillRect(0,0,WIDTH,HEIGHT);
            g2d.drawImage(tileImage, transform, null);
            scaleCombine -= 0.05;
            g2d.dispose();
            if (scaleCombine <= 1) combineAnimation = false;
        }
    }

    public void render(Graphics2D g) {
        if (beginningAnimation) {
            g.drawImage(beginningImage, x, y, null);
        } else if (combineAnimation) {
            g.drawImage(combineImage, (int) (x + WIDTH / 2 - scaleCombine * WIDTH / 2),
                    (int) (y + HEIGHT / 2 - scaleCombine * HEIGHT / 2), null);
        } else {
            g.drawImage(tileImage, x, y, null);
        }
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
        drawImage();
    }

    public boolean canCombine() {
        return canCombine;
    }

    public void setCanCombine(boolean canCombine) {
        this.canCombine = canCombine;
    }

    public Point getSlideTo() {
        return slideTo;
    }

    public void setSlideTo(Point slideTo) {
        this.slideTo = slideTo;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean isCombineAnimation() {
        return combineAnimation;
    }

    public void setCombineAnimation(boolean combineAnimation) {
        this.combineAnimation = combineAnimation;
        if (combineAnimation) scaleCombine = 1.3;
    }
}
