package libGdx;

import interfaces.Renderer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
//import com.bsencan.openchess.OpenChess;
//import com.bsencan.openchess.model.Board;

/**
 * A {@link Renderer} for {@link GameScreen}.
 * 
 */
public class GameRenderer implements Renderer {

	private final Stage stage = new Stage();
	private Table hud;
	private TextButton resetButton;

	public GameRenderer(Board board) {
		Gdx.input.setInputProcessor(this.stage);
		this.stage.addActor(board);
		this.initUI();
	}

	private void initUI() {
		this.hud = new Table(Assets.skin);

		this.resetButton = new TextButton(" Reset Game ", Assets.skin);
		this.resetButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				OpenChess.game.setScreen(new GameScreen());
			}
		});

		this.hud.add(this.resetButton);
		this.hud.setTransform(true);
		this.hud.setScale(1 / this.resetButton.getHeight());
		this.stage.addActor(this.hud);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(.3f, .3f, .4f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		this.stage.draw();
	}

	@Override
	public void setSize(int width, int height) {
		float vW = OpenChess.UWIDTH; // Viewport width.
		float vH = vW * ((float) height / width); // Viewport height.

		this.hud.setX(vW / 2);
		this.hud.setY(vH - 1);
		this.stage.setViewport(vW, vH, true, vW / 2, vH / 2, width, height);
		Gdx.graphics.requestRendering();
	}

	@Override
	public void dispose() {
		this.stage.dispose();
	}

}

