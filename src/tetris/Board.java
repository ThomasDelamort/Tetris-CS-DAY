package tetris;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Random;

public class Board extends JPanel implements Runnable {

    private Thread gameThread;

    private boolean running;

    private double scale;

    private int xOffset;

    private int yOffset;

    private final Color[][] board;

    private Piece currentPiece;

    private final Random random;

    private long lastDropTime;

    private final long dropDelay = 500;

    private int score = 0;

    private boolean gameOver = false;

    public Board() {

        setFocusable(true);

        requestFocusInWindow();

        setDoubleBuffered(true);

        setBackground(Color.BLACK);

        setPreferredSize(
                new Dimension(
                        Constants.BASE_WIDTH,
                        Constants.BASE_HEIGHT
                )
        );

        board = new Color[
                Constants.BOARD_HEIGHT
                ][
                Constants.BOARD_WIDTH
                ];

        random = new Random();

        addKeyListener(new InputHandler(this));

        spawnPiece();
    }

    public void startGame() {

        running = true;

        gameThread = new Thread(this);

        gameThread.start();
    }

    @Override
    public void run() {

        long lastTime = System.nanoTime();

        double ns =
                1000000000.0 / Constants.FPS;

        double delta = 0;

        while (running) {

            long now = System.nanoTime();

            delta += (now - lastTime) / ns;

            lastTime = now;

            while (delta >= 1) {

                update();

                repaint();

                delta--;
            }
        }
    }

    private void update() {

        if (gameOver) {
            return;
        }

        if (System.currentTimeMillis()
                - lastDropTime > dropDelay) {

            movePiece(0, 1);

            lastDropTime =
                    System.currentTimeMillis();
        }
    }

    private void spawnPiece() {

        Tetromino[] values =
                Tetromino.values();

        currentPiece = new Piece(
                values[random.nextInt(values.length)]
        );

        if (!canMove(
                currentPiece.x,
                currentPiece.y,
                currentPiece.shape
        )) {

            gameOver = true;
        }
    }

    public void movePiece(int dx, int dy) {

        if (gameOver) {
            return;
        }

        if (canMove(
                currentPiece.x + dx,
                currentPiece.y + dy,
                currentPiece.shape
        )) {

            currentPiece.x += dx;

            currentPiece.y += dy;

        } else if (dy == 1) {

            lockPiece();

            clearLines();

            spawnPiece();
        }
    }

    public void rotatePiece() {

        int[][] backup =
                copyShape(currentPiece.shape);

        currentPiece.rotate();

        if (!canMove(
                currentPiece.x,
                currentPiece.y,
                currentPiece.shape
        )) {

            currentPiece.shape = backup;
        }
    }

    public void hardDrop() {

        while (canMove(
                currentPiece.x,
                currentPiece.y + 1,
                currentPiece.shape
        )) {

            currentPiece.y++;
        }

        lockPiece();

        clearLines();

        spawnPiece();
    }

    private boolean canMove(
            int x,
            int y,
            int[][] shape
    ) {

        for (int row = 0;
             row < shape.length;
             row++) {

            for (int col = 0;
                 col < shape[row].length;
                 col++) {

                if (shape[row][col] == 0) {
                    continue;
                }

                int newX = x + col;

                int newY = y + row;

                if (newX < 0
                        || newX >= Constants.BOARD_WIDTH
                        || newY >= Constants.BOARD_HEIGHT) {

                    return false;
                }

                if (newY >= 0
                        && board[newY][newX] != null) {

                    return false;
                }
            }
        }

        return true;
    }

    private void lockPiece() {

        for (int row = 0;
             row < currentPiece.shape.length;
             row++) {

            for (int col = 0;
                 col < currentPiece.shape[row].length;
                 col++) {

                if (currentPiece.shape[row][col] != 0) {

                    int boardX =
                            currentPiece.x + col;

                    int boardY =
                            currentPiece.y + row;

                    if (boardY >= 0) {

                        board[boardY][boardX] =
                                currentPiece.color;
                    }
                }
            }
        }
    }

    private void clearLines() {

        for (int row =
             Constants.BOARD_HEIGHT - 1;
             row >= 0;
             row--) {

            boolean full = true;

            for (int col = 0;
                 col < Constants.BOARD_WIDTH;
                 col++) {

                if (board[row][col] == null) {

                    full = false;

                    break;
                }
            }

            if (full) {

                removeLine(row);

                score += 100;

                row++;
            }
        }
    }

    private void removeLine(int line) {

        for (int row = line;
             row > 0;
             row--) {

            for (int col = 0;
                 col < Constants.BOARD_WIDTH;
                 col++) {

                board[row][col] =
                        board[row - 1][col];
            }
        }

        for (int col = 0;
             col < Constants.BOARD_WIDTH;
             col++) {

            board[0][col] = null;
        }
    }

    private int[][] copyShape(int[][] original) {

        int[][] copy =
                new int[
                        original.length
                        ][
                        original[0].length
                        ];

        for (int row = 0;
             row < original.length;
             row++) {

            for (int col = 0;
                 col < original[row].length;
                 col++) {

                copy[row][col] =
                        original[row][col];
            }
        }

        return copy;
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        scale = Math.min(
                getWidth()
                        / (double) Constants.BASE_WIDTH,

                getHeight()
                        / (double) Constants.BASE_HEIGHT
        );

        xOffset = (int)
                ((getWidth()
                        - Constants.BASE_WIDTH * scale) / 2);

        yOffset = (int)
                ((getHeight()
                        - Constants.BASE_HEIGHT * scale) / 2);

        g2d.translate(xOffset, yOffset);

        g2d.scale(scale, scale);

        drawBoard(g2d);

        drawPlacedBlocks(g2d);

        drawCurrentPiece(g2d);

        drawGrid(g2d);

        drawUI(g2d);
    }

    private void drawBoard(Graphics2D g2d) {

        int boardWidth =
                Constants.BOARD_WIDTH
                        * Constants.TILE_SIZE;

        int boardHeight =
                Constants.BOARD_HEIGHT
                        * Constants.TILE_SIZE;

        int boardX =
                (Constants.BASE_WIDTH - boardWidth) / 2;

        int boardY =
                (Constants.BASE_HEIGHT - boardHeight) / 2;

        g2d.setColor(Color.DARK_GRAY);

        g2d.fillRect(
                boardX,
                boardY,
                boardWidth,
                boardHeight
        );
    }

    private void drawGrid(Graphics2D g2d) {

        int boardWidth =
                Constants.BOARD_WIDTH
                        * Constants.TILE_SIZE;

        int boardHeight =
                Constants.BOARD_HEIGHT
                        * Constants.TILE_SIZE;

        int startX =
                (Constants.BASE_WIDTH - boardWidth) / 2;

        int startY =
                (Constants.BASE_HEIGHT - boardHeight) / 2;

        g2d.setColor(Color.GRAY);

        for (int row = 0;
             row <= Constants.BOARD_HEIGHT;
             row++) {

            g2d.drawLine(
                    startX,
                    startY + row * Constants.TILE_SIZE,
                    startX + boardWidth,
                    startY + row * Constants.TILE_SIZE
            );
        }

        for (int col = 0;
             col <= Constants.BOARD_WIDTH;
             col++) {

            g2d.drawLine(
                    startX + col * Constants.TILE_SIZE,
                    startY,
                    startX + col * Constants.TILE_SIZE,
                    startY + boardHeight
            );
        }
    }

    private void drawPlacedBlocks(Graphics2D g2d) {

        int boardWidth =
                Constants.BOARD_WIDTH
                        * Constants.TILE_SIZE;

        int boardHeight =
                Constants.BOARD_HEIGHT
                        * Constants.TILE_SIZE;

        int startX =
                (Constants.BASE_WIDTH - boardWidth) / 2;

        int startY =
                (Constants.BASE_HEIGHT - boardHeight) / 2;

        for (int row = 0;
             row < Constants.BOARD_HEIGHT;
             row++) {

            for (int col = 0;
                 col < Constants.BOARD_WIDTH;
                 col++) {

                if (board[row][col] != null) {

                    g2d.setColor(board[row][col]);

                    g2d.fillRect(
                            startX
                                    + col * Constants.TILE_SIZE,

                            startY
                                    + row * Constants.TILE_SIZE,

                            Constants.TILE_SIZE,
                            Constants.TILE_SIZE
                    );
                }
            }
        }
    }

    private void drawCurrentPiece(Graphics2D g2d) {

        int boardWidth =
                Constants.BOARD_WIDTH
                        * Constants.TILE_SIZE;

        int boardHeight =
                Constants.BOARD_HEIGHT
                        * Constants.TILE_SIZE;

        int startX =
                (Constants.BASE_WIDTH - boardWidth) / 2;

        int startY =
                (Constants.BASE_HEIGHT - boardHeight) / 2;

        g2d.setColor(currentPiece.color);

        for (int row = 0;
             row < currentPiece.shape.length;
             row++) {

            for (int col = 0;
                 col < currentPiece.shape[row].length;
                 col++) {

                if (currentPiece.shape[row][col] != 0) {

                    g2d.fillRect(
                            startX
                                    + (currentPiece.x + col)
                                    * Constants.TILE_SIZE,

                            startY
                                    + (currentPiece.y + row)
                                    * Constants.TILE_SIZE,

                            Constants.TILE_SIZE,
                            Constants.TILE_SIZE
                    );
                }
            }
        }
    }

    private void drawUI(Graphics2D g2d) {

        g2d.setColor(Color.WHITE);

        g2d.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        40
                )
        );

        g2d.drawString(
                "TETRIS",
                60,
                80
        );

        g2d.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        28
                )
        );

        g2d.drawString(
                "Score: " + score,
                60,
                150
        );

        g2d.drawString(
                "LEFT / RIGHT = Move",
                60,
                240
        );

        g2d.drawString(
                "UP = Rotate",
                60,
                290
        );

        g2d.drawString(
                "DOWN = Soft Drop",
                60,
                340
        );

        g2d.drawString(
                "SPACE = Hard Drop",
                60,
                390
        );

        g2d.drawString(
                "ESC = Exit",
                60,
                440
        );

        if (gameOver) {

            g2d.setColor(Color.RED);

            g2d.setFont(
                    new Font(
                            "Arial",
                            Font.BOLD,
                            64
                    )
            );

            g2d.drawString(
                    "GAME OVER",
                    420,
                    350
            );
        }
    }
}