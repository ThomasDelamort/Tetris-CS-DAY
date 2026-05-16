package tetris;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Assets {

    public static BufferedImage background;

    public static void load() {

        try {

            background = ImageIO.read(
                    Assets.class.getResourceAsStream(
                            "/images/background.png"
                    )
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}