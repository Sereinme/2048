import javax.xml.parsers.SAXParser;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.spec.ECField;
import java.util.Random;

public class GameBoard {

    public static final int ROWS = 4;
    public static final int COLS = 4;

    private final int startingTiles = 2;
    private final Tile[][] board;
    public boolean dead;
    public boolean won;
    private final BufferedImage gameBoard;
    private final BufferedImage finalBoard;
    private final int x;
    private final int y;
    private int score = 0;
    private int highScore = 0;
    private Font scoreFont;

    private static final int SPACING = 10;
    public static int BOARD_WIDTH = (COLS + 1) * SPACING + COLS * Tile.WIDTH;
    public static int BOARD_HEIGHT = (ROWS + 1) * SPACING + ROWS * Tile.HEIGHT;

    private long elapsedMS;
    private long fastestMS;
    private long startTime;
    private boolean started;
    private String formattedTime = "00:00";

    // saving
    private String saveDataPath;
    private String fileName = "score.txt";

    public GameBoard(int x, int y) {
        try {
            saveDataPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            if (saveDataPath.contains(".jar")) {
                int se = saveDataPath.lastIndexOf("/");
                saveDataPath = saveDataPath.substring(0, se);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        scoreFont = Game.main.deriveFont(20f);
        this.x = x;
        this.y = y;
        board = new Tile[ROWS][COLS];
        gameBoard = new BufferedImage(BOARD_WIDTH, BOARD_HEIGHT, BufferedImage.TYPE_INT_RGB);
        finalBoard = new BufferedImage(BOARD_WIDTH, BOARD_HEIGHT, BufferedImage.TYPE_INT_RGB);
        startTime = System.nanoTime();

        loadHighScore();

        createBoardImage();
        start();
    }

    private void createSaveData() {
        try {
            File file = new File(saveDataPath, fileName);
            FileWriter output = new FileWriter(file);
            BufferedWriter writer = new BufferedWriter(output);
            writer.write("" + 0);
            writer.newLine();
            writer.write("" + Integer.MAX_VALUE);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadHighScore() {
        try {
            File f = new File(saveDataPath, fileName);
            if (!f.isFile()) {
                createSaveData();
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            highScore = Integer.parseInt(reader.readLine());
            fastestMS = Long.parseLong(reader.readLine());
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setHighScore() {
       FileWriter output = null;

       try {
           File f = new File(saveDataPath, fileName);
           output = new FileWriter(f);
           BufferedWriter writer = new BufferedWriter(output);
           writer.write("" + highScore);
           writer.newLine();
           if (elapsedMS <= fastestMS && won) {
               writer.write("" + elapsedMS);
           } else {
               writer.write("" + fastestMS);
           }
           writer.close();
       } catch (Exception e) {
           e.printStackTrace();
       }
    }

    private void createBoardImage() {
        Graphics2D g = (Graphics2D) gameBoard.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setBackground(new Color(36, 32,11,75));
        g.clearRect(0,0,BOARD_WIDTH,BOARD_HEIGHT);
        g.setColor(new Color(87,74,62));
        g.fillRoundRect(0,0,BOARD_WIDTH, BOARD_HEIGHT, 15, 15);
        g.setColor(new Color(77,63,49));

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int tile_x = SPACING + SPACING * col + Tile.WIDTH * col;
                int tile_y = SPACING + SPACING * row + Tile.HEIGHT * row;
                g.fillRoundRect(tile_x, tile_y, Tile.WIDTH, Tile.HEIGHT, Tile.ARC_WIDTH, Tile.ARC_HEIGHT);
            }
        }

    }

    private void start() {
        for (int i = 0; i < startingTiles; i++) {
            spawnRandom();
        }
    }

    private void spawnRandom() {
        Random random = new Random();
        boolean notValid = true;

        while (notValid) {
            int location = random.nextInt(ROWS * COLS);
            int row = location / ROWS;
            int col = location % COLS;
            Tile current = board[row][col];
            if (current == null) {
                int value = random.nextInt(10) < 9 ? 2 : 4;
                Tile tile = new Tile(value, getTileX(col), getTileY(row));
                board[row][col] = tile;
                notValid = false;
            }
        }
    }

    public int getTileX(int col) {
        return SPACING + col * Tile.WIDTH + col * SPACING;
    }

    public int getTileY(int row) {
        return SPACING + row * Tile.HEIGHT + row * SPACING;
    }

    public void render(Graphics2D g, Color back) {
        Graphics2D g2d = (Graphics2D) finalBoard.getGraphics();
        g2d.setColor(back);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.fillRect(0,0,BOARD_WIDTH,BOARD_HEIGHT);
        g2d.drawImage(gameBoard, 0,0,null);

        // draw tiles
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Tile current = board[row][col];
                if (current == null) continue;
                current.render(g2d); // draw tile in final board
            }
        }

        g.drawImage(finalBoard, x, y, null);
        g2d.dispose();



        // score and time
        int rectX = 230;
        int rectY = 20;
        int scoreWidth = DrawUtils.getMessageWidth("" + score, scoreFont, g);
        int scoreHeight = DrawUtils.getMessageHeight("" + score, scoreFont, g);
        int highScoreWidth = DrawUtils.getMessageWidth("" + highScore, scoreFont, g);
        int highScoreHeight = DrawUtils.getMessageHeight("" + highScore, scoreFont, g);
        int textWidth = DrawUtils.getMessageWidth("SCORE", scoreFont.deriveFont(12f), g);
        int textHeight = DrawUtils.getMessageHeight("SCORE", scoreFont.deriveFont(12f), g);
        int rWidth = (Math.max(scoreWidth, textWidth)) + 30;
        int rHeight = scoreHeight + textHeight + 30;
        int rWidthHigh = (Math.max(highScoreWidth, textWidth)) + 30;
        int rHeightHigh = highScoreHeight + textHeight + 30;
        int timeWidth = DrawUtils.getMessageWidth(formattedTime, scoreFont, g);
        int timeHeight = DrawUtils.getMessageHeight(formattedTime, scoreFont, g);
        int rTimeWidth = (Math.max(timeWidth, textWidth)) + 30;
        int rTimeHeight = timeHeight + textHeight + 30;
        g.setColor(new Color(87,74, 62));
        g.fillRoundRect(rectX, rectY, rWidth, rHeight, 10,10);
        g.fillRoundRect(rectX + rWidth + 10, rectY, rWidthHigh, rHeightHigh, 10,10);
        g.fillRoundRect(rectX - rTimeWidth - 10, rectY, rTimeWidth, rTimeHeight, 10,10);

        // text
        g.setColor(new Color(227,181,126));
        g.setFont(scoreFont.deriveFont(12f));
        g.drawString("SCORE", rectX + rWidth / 2 - textWidth / 2,rectY + rHeight / 2 - textHeight);
        g.drawString("BEST", rectX + 15 + rWidth + rWidthHigh / 2 - textWidth / 2,rectY + rHeightHigh / 2 - textHeight);
        g.drawString("TIME", rectX - 5 - rTimeWidth + textWidth / 2,rectY + rTimeHeight / 2 - textHeight);

        // score
        g.setColor(new Color(232,230,227));
        g.setFont(scoreFont);
        g.drawString("" + score, rectX + rWidth / 2 - scoreWidth / 2,rectY + rHeight / 2 + 15);
        g.drawString("" + highScore, rectX + 10 + rWidth + rWidthHigh / 2 - highScoreWidth / 2,rectY + rHeight / 2 + 15);
        g.drawString(formattedTime, rectX - 10 - rTimeWidth / 2 - timeWidth / 2, rectY + rTimeHeight / 2 + 15);

        // 2048 LOGO
        g.setColor(new Color(163,155,142));
        Font logoFont = new Font("Ink Free", Font.BOLD, 72);
        g.setFont(logoFont);
        int logoWidth = DrawUtils.getMessageWidth("48", logoFont, g);
        int logoHeight = DrawUtils.getMessageHeight("48", logoFont, g);
        g.drawString("48", 25,130);
        g.drawString("20", 25, 130 - logoHeight - 5);

        Font alertFont = new Font("SansSerif", Font.BOLD, 16);
        g.setFont(alertFont);
        g.setColor(new Color(163,155,142));
        int alertHeight = DrawUtils.getMessageHeight("Won", alertFont, g);

        if (won) {
            g.drawString("Reach 2048! 🎉", logoWidth + 35, rectY + rHeight + alertHeight + 10);
            g.drawString("Enter <SPACE> for a new game.", logoWidth + 35, rectY + rHeight + alertHeight * 2 + 30);
        }

        if (dead) {
            g.drawString("Opps, you failed. ☹", logoWidth + 35, rectY + rHeight + alertHeight + 15);
            g.drawString("Enter <SPACE> for a new game.", logoWidth + 35, rectY + rHeight + alertHeight * 2 + 30);
        }
    }

    public void update() {

        if (!won && !dead) {
            if (started) {
                elapsedMS = (System.nanoTime() - startTime) / 1000000;
                formattedTime = formatTime(elapsedMS);
            }
            else {
                startTime = System.nanoTime();
            }
        }

        checkKeys();

        if (score >= highScore) highScore = score;

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Tile current = board[row][col];
                if (current == null) continue;
                current.update();
                resetPosition(current, row, col);
                if (current.getValue() == 2048)
                    won = true;
            }
        }
    }

    public void resetPosition(Tile current, int row, int col) {
        if (current == null) return;

        int x = getTileX(col);
        int y = getTileY(row);

        int distX = current.getX() - x;
        int distY = current.getY() - y;

        if (Math.abs(distX) < Tile.SLIDE_SPEED) {
            current.setX(current.getX() - distX);
        }
        if (Math.abs(distY) < Tile.SLIDE_SPEED) {
            current.setY(current.getY() - distY);
        }

        if (distX < 0) {
            current.setX(current.getX() + Tile.SLIDE_SPEED);
        }
        if (distY < 0) {
            current.setY(current.getY() + Tile.SLIDE_SPEED);
        }
        if (distX > 0) {
            current.setX(current.getX() - Tile.SLIDE_SPEED);
        }
        if (distY > 0) {
            current.setY(current.getY() - Tile.SLIDE_SPEED);
        }
    }

    public void checkKeys() {
        if (!won && !dead) {
            if (Keyboard.typed(KeyEvent.VK_LEFT) || Keyboard.typed(KeyEvent.VK_A)) {
                // move tiles left
                moveTiles(Direction.LEFT);

                if (!started) started = true;
            }
            if (Keyboard.typed(KeyEvent.VK_RIGHT) || Keyboard.typed(KeyEvent.VK_D)) {
                // move tiles right
                moveTiles(Direction.RIGHT);

                if (!started) started = true;
            }
            if (Keyboard.typed(KeyEvent.VK_UP) || Keyboard.typed(KeyEvent.VK_W)) {
                // move tiles up
                moveTiles(Direction.UP);

                if (!started) started = true;
            }
            if (Keyboard.typed(KeyEvent.VK_DOWN) || Keyboard.typed(KeyEvent.VK_S)) {
                // move tiles down
                moveTiles(Direction.DOWN);

                if (!started) started = true;
            }
        }
    }

    private boolean move(int row, int col, int horizontalDirection, int verticalDirection, Direction dir) {
        boolean canMove = false;

        Tile current = board[row][col];
        if (current == null) return false;
        boolean move = true;
        int newCol = col;
        int newRow = row;
        while (move) { // move or combine tiles on one dimension to bounds
            newCol += horizontalDirection;
            newRow += verticalDirection;
            if (checkOutOfBounds(dir, newRow, newCol)) break;
            if (board[newRow][newCol] == null) { // if tile going to move is null
                board[newRow][newCol] = current;
                board[newRow - verticalDirection][newCol - horizontalDirection] = null;
                board[newRow][newCol].setSlideTo(new Point(newRow, newCol));
                canMove = true;
            } else if (board[newRow][newCol].getValue() == current.getValue() && board[newRow][newCol].canCombine()) {
                board[newRow][newCol].setCanCombine(false);
                board[newRow][newCol].setValue(board[newRow][newCol].getValue() * 2);
                canMove = true;
                board[newRow - verticalDirection][newCol - horizontalDirection] = null;
                board[newRow][newCol].setSlideTo(new Point(newRow, newCol));
                board[newRow][newCol].setCombineAnimation(true);
                score += board[newRow][newCol].getValue();
            }
            else move = false;
        }

        return canMove;
    }

    private boolean checkOutOfBounds(Direction dir, int row, int col) {
        if (dir == Direction.LEFT)
            return col < 0;
        else if (dir == Direction.RIGHT)
            return col > COLS - 1;
        else if (dir == Direction.UP)
            return row < 0;
        else if (dir == Direction.DOWN)
            return row > ROWS - 1;
        else
            return false;
    }

    private void moveTiles(Direction dir) {
        boolean canMove = false;
        int horizontalDirection = 0;
        int verticalDirection = 0;

        if (dir == Direction.LEFT) {
            horizontalDirection = -1;
            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLS; col++) { // moving left should move the tiles at left first
                    if (!canMove) {
                        canMove = move(row, col ,horizontalDirection, verticalDirection, dir);
                    } else move(row, col, horizontalDirection, verticalDirection, dir);
                }
            }
        } else if (dir == Direction.RIGHT) {
            horizontalDirection = 1;
            for (int row = 0; row < ROWS; row++) {
                for (int col = COLS - 1; col >= 0; col--) { // moving right should move the tiles at right first
                    if (!canMove) {
                        canMove = move(row, col ,horizontalDirection, verticalDirection, dir);
                    } else move(row, col, horizontalDirection, verticalDirection, dir);
                }
            }
        } else if (dir == Direction.UP) {
            verticalDirection = -1;
            for (int row = 0; row < ROWS; row++) { // moving up should move the tiles at top first
                for (int col = 0; col < COLS; col++) {
                    if (!canMove) {
                        canMove = move(row, col ,horizontalDirection, verticalDirection, dir);
                    } else move(row, col, horizontalDirection, verticalDirection, dir);
                }
            }
        } else if (dir == Direction.DOWN) {
            verticalDirection = 1;
            for (int row = ROWS - 1; row >= 0; row--) { // moving down should move the tiles at bottom first
                for (int col = 0; col < COLS; col++) {
                    if (!canMove) {
                        canMove = move(row, col ,horizontalDirection, verticalDirection, dir);
                    } else move(row, col, horizontalDirection, verticalDirection, dir);
                }
            }
        } else {
            System.out.println(dir + "is not a valid direction");
        }

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Tile current = board[row][col];
                if (current == null) continue;
                current.setCanCombine(true);
            }
        }

        if (canMove) {
            spawnRandom();
            checkDead();
        }
    }

    private void checkDead() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (board[row][col] == null) return;
                if (checkSurroundingTiles(row, col, board[row][col])) return;
            }
        }

        dead = true;
        if (score >= highScore) highScore = score;
        setHighScore();
    }

    private boolean checkSurroundingTiles(int row, int col, Tile current) {
        if (row > 0) {
            Tile check = board[row - 1][col];
            if (check == null) return true;
            if (check.getValue() == current.getValue()) return true;
        }

        if (row < ROWS - 1) {
            Tile check = board[row + 1][col];
            if (check == null) return true;
            if (check.getValue() == current.getValue()) return true;
        }

        if (col > 0) {
            Tile check = board[row][col - 1];
            if (check == null) return true;
            if (check.getValue() == current.getValue()) return true;
        }

        if (col < COLS - 1) {
            Tile check = board[row][col + 1];
            if (check == null) return true;
            if (check.getValue() == current.getValue()) return true;
        }
        return false;
    }

    private String formatTime(long millis) {
       String formattedTime;

       String hourFormat = "";
       int hours = (int)(millis / 3600000);
       if (hours >= 1) {
           millis -= hours * 3600000L;
           if (hours < 10) {
               hourFormat = "0" + hours;
           } else {
               hourFormat = "" + hours;
           }
           hourFormat += ":";
       }

       String minuteFormat;
       int minutes = (int)(millis / 60000);
       if (minutes >= 1) {
           millis -= minutes * 60000L;
           if (minutes < 10) {
               minuteFormat = "0" + minutes;
           } else {
               minuteFormat = "" + minutes;
           }
       } else {
           minuteFormat = "00";
       }

       String secondFormat;
       int seconds = (int)(millis / 1000);
       if (seconds >= 1) {
           millis -= seconds * 1000L;
           if (seconds < 10) {
               secondFormat = "0" + seconds;
           } else {
               secondFormat = "" + seconds;
           }
       } else {
           secondFormat = "00";
       }

       formattedTime = hourFormat + minuteFormat + ":" + secondFormat;
       return formattedTime;
    }

}
