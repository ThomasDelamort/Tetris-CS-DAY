package tetris;

import javax.swing.JFrame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

public class GameWindow extends JFrame {

    public GameWindow() {

        setTitle("Tetris");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setUndecorated(true);

        setResizable(false);

        Board board = new Board();

        add(board);

        GraphicsDevice gd = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();

        gd.setFullScreenWindow(this);

        setVisible(true);

        board.requestFocusInWindow();

        board.startGame();
    }
}