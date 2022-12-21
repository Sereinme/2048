import javax.xml.parsers.SAXParser;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Random;

public class GameBoard {

    public static final int ROWS = 4;
    public static final int COLS = 4;

    private final int startingTiles = 2;
    private Tile[][] board;
    private boolean dead;
    private boolean won;
    private BufferedImage gameBoard;
    private BufferedImage finalBoard;
    private int x;
    private int y;
    private boolean started;

    private static int SPACING = 10;
    public static int BOARD_WIDTH = (COLS + 1) * SPACING + COLS * Tile.WIDTH;
    public static int BOARD_HEIGHT = (ROWS + 1) * SPACING + ROWS * Tile.HEIGHT;

    public GameBoard(int x, int y) {
        this.x = x;
        this.y = y;
        board = new Tile[ROWS][COLS];
        gameBoard = new BufferedImage(BOARD_WIDTH, BOARD_HEIGHT, BufferedImage.TYPE_INT_RGB);
        finalBoard = new BufferedImage(BOARD_WIDTH, BOARD_HEIGHT, BufferedImage.TYPE_INT_RGB);

        createBoardImage();
        start();
    }

    private void createBoardImage() {
        Graphics2D g = (Graphics2D) gameBoard.getGraphics();
        g.setColor(new Color(87,74,62));
        g.fillRect(0,0,BOARD_WIDTH, BOARD_HEIGHT);
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

    public void render(Graphics2D g) {
        Graphics2D g2d = (Graphics2D) finalBoard.getGraphics();
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
    }

    public void update() {
        checkKeys();

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
        if (Keyboard.typed(KeyEvent.VK_LEFT)) {
            // move tiles left
            moveTiles(Direction.LEFT);

            if (!started) started = true;
        }
        if (Keyboard.typed(KeyEvent.VK_RIGHT)) {
            // move tiles right
            moveTiles(Direction.RIGHT);

            if (!started) started = true;
        }
        if (Keyboard.typed(KeyEvent.VK_UP)) {
            // move tiles up
            moveTiles(Direction.UP);

            if (!started) started = true;
        }
        if (Keyboard.typed(KeyEvent.VK_DOWN)) {
            // move tiles down
            moveTiles(Direction.DOWN);

            if (!started) started = true;
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
                // TODO: set combine animation
                // TODO: add to score
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
        // TODO: setHighScore
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

}
