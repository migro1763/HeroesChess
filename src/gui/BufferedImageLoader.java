package gui;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class BufferedImageLoader {
    
    public BufferedImage loadImage(String pathRelativeToThis) {
        BufferedImage img = null;
		try {
			img = ImageIO.read(this.getClass().getResource(pathRelativeToThis));
		} catch (IOException e) {
			System.out.println("Cannot find image: " + pathRelativeToThis);
			e.getStackTrace();
		}
        return img;
    }
    
}