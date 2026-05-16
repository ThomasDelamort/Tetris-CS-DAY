package tetris;

import java.awt.Color;

public class Piece {

    public int[][] shape;

    public int x;

    public int y;

    public Color color;

    public Piece(Tetromino tetromino) {

        shape = tetromino.getShape();

        color = tetromino.getColor();

        x = 3;

        y = 0;
    }

    public void rotate() {

        int rows = shape.length;

        int cols = shape[0].length;

        int[][] rotated =
                new int[cols][rows];

        for (int row = 0;
             row < rows;
             row++) {

            for (int col = 0;
                 col < cols;
                 col++) {

                rotated[col][rows - 1 - row] =
                        shape[row][col];
            }
        }

        shape = rotated;
    }
}