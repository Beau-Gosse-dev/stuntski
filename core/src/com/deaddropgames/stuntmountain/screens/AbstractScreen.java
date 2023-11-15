package com.deaddropgames.stuntmountain.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.deaddropgames.stuntmountain.StuntMountain;


public abstract class AbstractScreen extends InputAdapter implements Screen {

    private static final String LOG = "AbstractScreen";

    final StuntMountain game;
    final Stage stage;
    TextureAtlas uiAtlas;
    ImageButton backButton;

    private InputMultiplexer inputMultiplexer;

    private Skin skin;
    private int buttonWidth = 300;
    private int buttonHeight = 60;

    AbstractScreen(StuntMountain game) {

        Gdx.app.debug(LOG, "AbstractScreen()");

        this.game = game;
        stage = new Stage();
        inputMultiplexer = new InputMultiplexer();
    }

    Skin getSkin() {

        if(skin == null) {

            skin = new Skin(Gdx.files.internal("assets/skin/freezing-ui.json"));
            buttonWidth *= game.scaleFactor;
            buttonHeight *= game.scaleFactor;
        }
        
        return skin;
    }

    @Override
    public void render(float delta) {

        stage.act(delta);

        Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {

        Gdx.app.debug(LOG, "resize(" + width + ", " + height + ")");
    }

    @Override
    public void show() {

        Gdx.app.debug(LOG, "show()");

        // want both the child class and the stage to be able to process events...
        inputMultiplexer.addProcessor(this);
        inputMultiplexer.addProcessor(stage);

        Gdx.input.setInputProcessor(this.inputMultiplexer);
        Gdx.input.setCatchBackKey(true);
    }

    @Override
    public void hide() {

        Gdx.app.debug(LOG, "hide()");
        dispose();
    }

    @Override
    public void pause() {

        Gdx.app.debug(LOG, "pause()");
    }

    @Override
    public void resume() {

        Gdx.app.debug(LOG, "resume()");
    }

    @Override
    public void dispose() {

        Gdx.app.debug(LOG, "dispose()");
        stage.dispose();

        if(skin != null) {

            skin.dispose();
            skin = null;
        }
    }

    int getButtonWidth() {
        return buttonWidth;
    }

    int getButtonHeight() {
        return buttonHeight;
    }

    void createBackButton() {

        if(uiAtlas == null)
            uiAtlas = game.assetManager.get("assets/packedimages/ui.atlas", TextureAtlas.class);
        Sprite backSpr = uiAtlas.createSprite("control_back");

        float btnWidth = Math.round(backSpr.getWidth() * game.scaleFactor);
        float btnHeight = Math.round(backSpr.getHeight() * game.scaleFactor);
        float fudge = Math.round(20 * game.scaleFactor);

        backButton = new ImageButton(new SpriteDrawable(backSpr));
        backButton.getImage().setScale(game.scaleFactor);

        Container container = new Container<ImageButton>(backButton);
        container.setX(Math.round(btnWidth / 2));
        container.setY(Gdx.graphics.getHeight() - Math.round(btnHeight / 2) - fudge);

        stage.addActor(container);
    }

    Label createTitleLabel(final String text) {

        return new Label(text, new Label.LabelStyle(skin.getFont("title"), skin.getColor("white")));
    }

    Label createButtonLabel(final String text) {

        return new Label(text, new Label.LabelStyle(skin.getFont("button"), skin.getColor("white")));
    }
}
