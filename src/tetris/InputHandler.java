package tetris;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class InputHandler extends KeyAdapter {

    private final Board board;

    public InputHandler(Board board) {

        this.board = board;
    }

    @Override
    public void keyPressed(KeyEvent e) {

        switch (e.getKeyCode()) {

            case KeyEvent.VK_LEFT:

                board.movePiece(-1, 0);

                break;

            case KeyEvent.VK_RIGHT:

                board.movePiece(1, 0);

                break;

            case KeyEvent.VK_DOWN:

                board.movePiece(0, 1);

                break;

            case KeyEvent.VK_UP:

                board.rotatePiece();

                break;

            case KeyEvent.VK_SPACE:

                board.hardDrop();

                break;

            case KeyEvent.VK_C:

                board.holdPiece();

                break;

            case KeyEvent.VK_ENTER:

                board.startPlaying();

                break;

            case KeyEvent.VK_P:

                board.togglePause();

                break;

            case KeyEvent.VK_R:

                board.restartGame();

                break;

            case KeyEvent.VK_ESCAPE:

                System.exit(0);

                break;
        }
    }
}