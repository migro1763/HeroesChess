package interfaces;

/**
 * An interface for all classes that needs to do rendering to a screen.
 * 
 */
public interface Renderer {

	/**
	 * Should be called when rendering needs to be done.
	 * 
	 * @param delta
	 *            The time in seconds since the last render.
	 */
	public void render(float delta);

	/**
	 * Should be called when device screen is resized.
	 * 
	 * @param width
	 *            The new width in pixels.
	 * @param height
	 *            The new height in pixels.
	 */
	public void setSize(int width, int height);

	/**
	 * Should be called when the renderer needs to release all resources.
	 */
	public void dispose();
}
