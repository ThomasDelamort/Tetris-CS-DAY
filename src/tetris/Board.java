package tetris;

import javax.swing.JPanel;
import java.awt.*;
import java.util.Random;

public class Board extends JPanel implements Runnable {

    private Thread gameThread;

    private boolean running;

    private boolean paused = false;

    private boolean started = false;

    private double scale;

    private int xOffset;

    private int yOffset;

    private final Color[][] board;

    private Piece currentPiece;

    private Tetromino nextTetromino;

    private Tetromino heldTetromino;

    private boolean canHold = true;

    private final Random random;

    private long lastDropTime;

    private long dropDelay = 500;

    private int score = 0;

    private int level = 1;

    private int totalLinesCleared = 0;

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

        nextTetromino = Tetromino.randomPiece();

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

        if (gameOver || paused || !started) {
            return;
        }

        if (System.currentTimeMillis()
                - lastDropTime > dropDelay) {

            movePiece(0, 1);

            lastDropTime =
                    System.currentTimeMillis();
        }
    }

    public void startPlaying() {

        started = true;
    }

    public void togglePause() {

        if (!started || gameOver) {
            return;
        }

        paused = !paused;
    }

    public void restartGame() {

        for (int row = 0;
             row < Constants.BOARD_HEIGHT;
             row++) {

            for (int col = 0;
                 col < Constants.BOARD_WIDTH;
                 col++) {

                board[row][col] = null;
            }
        }

        score = 0;

        level = 1;

        totalLinesCleared = 0;

        dropDelay = 500;

        paused = false;

        started = true;

        gameOver = false;

        heldTetromino = null;

        nextTetromino = Tetromino.randomPiece();

        spawnPiece();
    }

    private void spawnPiece() {

        currentPiece = new Piece(nextTetromino);

        nextTetromino = Tetromino.randomPiece();

        canHold = true;

        if (!canMove(
                currentPiece.x,
                currentPiece.y,
                currentPiece.shape
        )) {

            gameOver = true;
        }
    }

    public void movePiece(int dx, int dy) {

        if (gameOver || paused || !started) {
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

        if (gameOver || paused || !started) {
            return;
        }

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

        if (gameOver || paused || !started) {
            return;
        }

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

    public void holdPiece() {

        if (!canHold || gameOver || paused || !started) {
            return;
        }

        Tetromino currentTetromino =
                getCurrentTetromino();

        if (heldTetromino == null) {

            heldTetromino = currentTetromino;

            spawnPiece();

        } else {

            Tetromino temp = heldTetromino;

            heldTetromino = currentTetromino;

            currentPiece = new Piece(temp);
        }

        canHold = false;
    }

    private Tetromino getCurrentTetromino() {

        for (Tetromino t : Tetromino.values()) {

            if (t.getColor().equals(currentPiece.color)) {
                return t;
            }
        }

        return Tetromino.I;
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

        int cleared = 0;

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

                cleared++;

                row++;
            }
        }

        if (cleared > 0) {

            totalLinesCleared += cleared;

            switch (cleared) {

                case 1:
                    score += 100 * level;
                    break;

                case 2:
                    score += 300 * level;
                    break;

                case 3:
                    score += 500 * level;
                    break;

                case 4:
                    score += 800 * level;
                    break;
            }

            int newLevel =
                    totalLinesCleared / 10 + 1;

            if (newLevel > level) {

                level = newLevel;

                dropDelay = Math.max(
                        100,
                        500 - ((level - 1) * 40)
                );
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

            System.arraycopy(
                    original[row],
                    0,
                    copy[row],
                    0,
                    original[row].length
            );
        }

        return copy;
    }

    private int getGhostY() {

        int ghostY = currentPiece.y;

        while (canMove(
                currentPiece.x,
                ghostY + 1,
                currentPiece.shape
        )) {

            ghostY++;
        }

        return ghostY;
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

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

        drawGhostPiece(g2d);

        drawCurrentPiece(g2d);

        drawGrid(g2d);

        drawNextPiece(g2d);

        drawHeldPiece(g2d);

        drawUI(g2d);
    }

    private int getBoardStartX() {

        int boardWidth =
                Constants.BOARD_WIDTH
                        * Constants.TILE_SIZE;

        return (Constants.BASE_WIDTH - boardWidth) / 2;
    }

    private int getBoardStartY() {

        int boardHeight =
                Constants.BOARD_HEIGHT
                        * Constants.TILE_SIZE;

        return (Constants.BASE_HEIGHT - boardHeight) / 2;
    }

    private void drawBoard(Graphics2D g2d) {

        int boardWidth =
                Constants.BOARD_WIDTH
                        * Constants.TILE_SIZE;

        int boardHeight =
                Constants.BOARD_HEIGHT
                        * Constants.TILE_SIZE;

        g2d.setColor(Color.DARK_GRAY);

        g2d.fillRect(
                getBoardStartX(),
                getBoardStartY(),
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

        int startX = getBoardStartX();

        int startY = getBoardStartY();

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

        int startX = getBoardStartX();

        int startY = getBoardStartY();

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

    private void drawGhostPiece(Graphics2D g2d) {

        int startX = getBoardStartX();

        int startY = getBoardStartY();

        int ghostY = getGhostY();

        Color ghostColor = new Color(
                currentPiece.color.getRed(),
                currentPiece.color.getGreen(),
                currentPiece.color.getBlue(),
                80
        );

        g2d.setColor(ghostColor);

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
                                    + (ghostY + row)
                                    * Constants.TILE_SIZE,

                            Constants.TILE_SIZE,
                            Constants.TILE_SIZE
                    );
                }
            }
        }
    }

    private void drawCurrentPiece(Graphics2D g2d) {

        int startX = getBoardStartX();

        int startY = getBoardStartY();

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

    private void drawMiniPiece(
            Graphics2D g2d,
            Tetromino tetromino,
            int x,
            int y
    ) {

        if (tetromino == null) {
            return;
        }

        int[][] shape = tetromino.getShape();

        g2d.setColor(tetromino.getColor());

        for (int row = 0; row < shape.length; row++) {

            for (int col = 0; col < shape[row].length; col++) {

                if (shape[row][col] != 0) {

                    g2d.fillRect(
                            x + col * 20,
                            y + row * 20,
                            20,
                            20
                    );
                }
            }
        }
    }

    private void drawNextPiece(Graphics2D g2d) {

        g2d.setColor(Color.WHITE);

        g2d.setFont(new Font("Arial", Font.BOLD, 28));

        g2d.drawString("NEXT", 1000, 100);

        drawMiniPiece(g2d, nextTetromino, 1000, 130);
    }

    private void drawHeldPiece(Graphics2D g2d) {

        g2d.setColor(Color.WHITE);

        g2d.setFont(new Font("Arial", Font.BOLD, 28));


        drawMiniPiece(g2d, heldTetromino, 60, 580);
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
                "Level: " + level,
                60,
                200
        );

        g2d.drawString(
                "Lines: " + totalLinesCleared,
                60,
                250
        );


        g2d.drawString(
                "SPACE = Hard Drop",
                60,
                390
        );

        g2d.drawString(
                "P = Pause",
                60,
                440
        );

        g2d.drawString(
                "R = Restart",
                60,
                490
        );

        g2d.drawString(
                "ESC = Exit",
                60,
                540
        );

        if (!started) {

            g2d.setColor(Color.WHITE);

            g2d.setFont(
                    new Font(
                            "Arial",
                            Font.BOLD,
                            64
                    )
            );

            g2d.drawString(
                    "PRESS ENTER",
                    410,
                    300
            );

            g2d.setFont(
                    new Font(
                            "Arial",
                            Font.PLAIN,
                            32
                    )
            );

            g2d.drawString(
                    "TO START",
                    563,
                    360
            );
        }

        if (paused) {

            g2d.setColor(Color.WHITE);

            g2d.setFont(
                    new Font(
                            "Arial",
                            Font.BOLD,
                            64
                    )
            );

            g2d.drawString(
                    "PAUSED",
                    510,
                    320
            );
        }

        if (gameOver) {

            g2d.setColor(Color.WHITE);

            g2d.setFont(
                    new Font(
                            "Arial",
                            Font.BOLD,
                            64
                    )
            );

            g2d.drawString(
                    "GAME OVER",
                    450,
                    300
            );

            g2d.setFont(
                    new Font(
                            "Arial",
                            Font.PLAIN,
                            32
                    )
            );

            g2d.drawString(
                    "PRESS R TO PLAY AGAIN",
                    450,
                    360
            );
        }
    }
}