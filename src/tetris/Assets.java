package tetris;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class Assets {

    public static BufferedImage background;

    public static void load() {

        try {

            background = ImageIO.read(
                    Objects.requireNonNull(Assets.class.getResourceAsStream(
                            "/images/background.png"
                    ))
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}