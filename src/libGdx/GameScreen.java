package libGdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
//import com.bsencan.openchess.Assets;
//import com.bsencan.openchess.model.Board;
//import com.bsencan.openchess.view.GameRenderer;

/**
 * Main game screen. Creates a new chess board and a game renderer, then tells
 * the renderer to render that board.
 */
public class GameScreen implements Screen {

	private GameRenderer renderer;

	@Override
	public void render(float delta) {
		this.renderer.render(delta);
	}

	@Override
	public void resize(int width, int height) {
		this.renderer.setSize(width, height);
	}

	@Override
	public void show() {
		Board board; // Can't call the constructor here. Assets have to be
						// loaded first.

		Assets.loadGame();
		board = new Board();
		board.populate();
		this.renderer = new GameRenderer(board);
		this.renderer
				.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	@Override
	public void hide() {
		this.renderer.dispose();
		Assets.disposeGame();
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
	} // Never called automatically.

}

