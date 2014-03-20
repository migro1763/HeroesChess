package gui;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

public class Animator {

    private ArrayList<BufferedImage> frames;

    public BufferedImage sprite;
    public Unit unit;

    private volatile boolean running = false;
    private volatile boolean idle = false;

    private long previousTime, speed, variance;
	private long idlePause, idlePauseDuration, previousIdleTime; // time in millis to pause between idle anims
    private int frameAtPause, currentFrame;

    public Animator(ArrayList<BufferedImage> frames){
        this.frames = frames;
        unit = null;
        variance = new Random().nextInt(10);
		idlePauseDuration = (new Random().nextInt(20) + 10) * 500;
		idlePause = idlePauseDuration;
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
    		// pause sprite's idle animation until idlePause time has passed
    		if(time - previousIdleTime >= idlePause) {
    			idle = false;
                previousIdleTime = time;
    		}
            if(time - previousTime >= (speed + variance)) {
                //Update the animation
                if(currentFrame < frames.size()-1 && !idle) {
                	currentFrame++;
               	} else {
               		currentFrame = 0;
               		idle = true;
				}
				sprite = frames.get(currentFrame);
                previousTime = time;
            }
        }
    }
	
	public long getIdlePause() {
		return idlePause;
	}

	public void setIdlePause(long idlePause) {
		this.idlePause = idlePause;
	}
	
	public void resetIdlePause() {
		idlePause = idlePauseDuration;
	}

    public void play() {
        running = true;
        idle = false;
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