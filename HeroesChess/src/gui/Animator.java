package gui;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

public class Animator {

    private ArrayList<BufferedImage> frames;

    public BufferedImage sprite;
    public Unit unit;

    private volatile boolean running = false;

    private long previousTime, speed, variance;
    private int frameAtPause, currentFrame;

    public Animator(ArrayList<BufferedImage> frames){
        this.frames = frames;
        this.unit = null;
        this.variance = new Random().nextInt(10);
    }

    public void setSpeed(long speed){
        this.speed = speed;
    }
    
    public BufferedImage getSpriteAtFrame(int frame) {
    	return frames.get(frame);
    }
    
    public int getSizeOfFrames() {
    	return frames.size();
    }

    // not used
    public int getMaxHeight() {
		int max = 0, temp;
		for (BufferedImage frame : frames) {
			temp = frame.getHeight();
			if( temp > max)
				max = temp;
		}
		return max;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public void update(long time) {
        if(running) {
            if(time - previousTime >= (speed + variance)) {
                //Update the animation
                if(currentFrame < frames.size()-1) {
                	currentFrame++;
               	} else {
               		currentFrame = 0;
				}
				sprite = frames.get(currentFrame);
                previousTime = time;
            }
        }
    }

    public void play() {
        running = true;
        previousTime = 0;
        frameAtPause = 0;
        currentFrame = 0;
    }

    public void stop() {
        running = false;
        previousTime = 0;
        frameAtPause = 0;
        currentFrame = 0;
    }

    public void pause() {
        frameAtPause = currentFrame;
        running = false;
    }

    public void resume() {
        currentFrame = frameAtPause;
        running = true;
    }

}