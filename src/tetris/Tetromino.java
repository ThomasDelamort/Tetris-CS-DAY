package tetris;

import java.awt.Color;
import java.util.Random;

public enum Tetromino {

    I(
            new int[][]{
                    {1, 1, 1, 1}
            },
            Color.CYAN
    ),

    O(
            new int[][]{
                    {1, 1},
                    {1, 1}
            },
            Color.YELLOW
    ),

    T(
            new int[][]{
                    {0, 1, 0},
                    {1, 1, 1},
                    {0, 0, 0}
            },
            Color.MAGENTA
    ),

    S(
            new int[][]{
                    {0, 1, 1},
                    {1, 1, 0},
                    {0, 0, 0}
            },
            Color.GREEN
    ),

    Z(
            new int[][]{
                    {1, 1, 0},
                    {0, 1, 1},
                    {0, 0, 0}
            },
            Color.RED
    ),

    J(
            new int[][]{
                    {1, 0, 0},
                    {1, 1, 1},
                    {0, 0, 0}
            },
            Color.BLUE
    ),

    L(
            new int[][]{
                    {0, 0, 1},
                    {1, 1, 1},
                    {0, 0, 0}
            },
            Color.ORANGE
    );

    private final int[][] shape;

    private final Color color;

    private static final Random RANDOM =
            new Random();

    Tetromino(
            int[][] shape,
            Color color
    ) {

        this.shape = copyShape(shape);

        this.color = color;
    }

    public int[][] getShape() {

        return copyShape(shape);
    }

    public Color getColor() {

        return color;
    }

    public static Tetromino randomPiece() {

        Tetromino[] values = values();

        return values[
                RANDOM.nextInt(values.length)
                ];
    }

    private static int[][] copyShape(
            int[][] original
    ) {

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
}