package gui;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.imageio.ImageIO;

public class BufferedImageLoader {
    
    public BufferedImage loadImage(String pathRelativeToThis) {
        URI uri = null;
		try {
			uri = new URI(pathRelativeToThis);
		} catch (URISyntaxException e1) {}
        URL url = null;
		try {
			url = uri.toURL();
		} catch (MalformedURLException e1) {}
        BufferedImage img = null;
		try {
			img = ImageIO.read(url);
		} catch (IOException e) {}
        return img;
    }
    
}