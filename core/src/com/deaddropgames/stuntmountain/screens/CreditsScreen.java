package com.deaddropgames.stuntmountain.screens;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.deaddropgames.stuntmountain.StuntMountain;

public class CreditsScreen extends AbstractScreen {

    private static final String LOG = "CreditsScreen";

    CreditsScreen(StuntMountain game) {

        super(game);

        Gdx.app.debug(LOG, "CreditsScreen()");
    }

    @Override
    public void show() {

        Gdx.app.debug(LOG, "show()");
        super.show();

        Skin skin = super.getSkin();

        Table table = new Table(skin);
        table.setWidth(stage.getWidth());
        table.setHeight(stage.getHeight());
        stage.addActor(table);

        table.add(createTitleLabel("Credits")).spaceBottom(50 * game.scaleFactor);
        table.row();

        table.add(createButtonLabel("UI Graphics by Raymond \"Raeleus\" Buckley")).spaceRight(20 * game.scaleFactor);
        TextButton link1Button = new TextButton("Website", skin);
        link1Button.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {

                Gdx.net.openURI("https://github.com/czyzby/gdx-skins/tree/master/freezing");
            }
        });
        table.add(link1Button).size(150, getButtonHeight()).uniform().spaceBottom(10);
        table.row();

        table.add(createButtonLabel("UI Graphics Licensed by Creative Commons 4.0")).spaceRight(20 * game.scaleFactor);
        TextButton link2Button = new TextButton("Website", skin);
        link2Button.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {

                Gdx.net.openURI("https://creativecommons.org/licenses/by/4.0/");
            }
        });
        table.add(link2Button).size(150, getButtonHeight()).uniform().spaceBottom(10);
        table.row();

        table.add(createButtonLabel("Built with LibGDX")).spaceRight(20 * game.scaleFactor);
        TextButton link3Button = new TextButton("Website", skin);
        link3Button.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {

                Gdx.net.openURI("https://libgdx.badlogicgames.com/");
            }
        });
        table.add(link3Button).size(150, getButtonHeight()).uniform().spaceBottom(10);
        table.row();

        table.add(createButtonLabel("Everything else by Tristen Georgiou")).spaceRight(20 * game.scaleFactor);
        TextButton link4Button = new TextButton("Website", skin);
        link4Button.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {

                Gdx.net.openURI("https://www.linkedin.com/in/tristen-georgiou-b2a935/");
            }
        });
        table.add(link4Button).size(150, getButtonHeight()).uniform().spaceBottom(10);
        table.row();

        createBackButton();
        backButton.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {

                game.preferences.flush();
                game.setScreen(new MenuScreen(game));
            }
        });
    }

    @Override
    public void resize(int width, int height) {

        Gdx.app.debug(LOG, "resize(" + width + ", " + height + ")");
        super.resize(width, height);
    }

    @Override
    public boolean keyUp(int keycode) {

        Gdx.app.debug(LOG, "keyUp(" + keycode + ")");
        switch(keycode) {

            case Input.Keys.ENTER:
            case Input.Keys.BACK: { // enter key

                game.setScreen(new MenuScreen(game));
                break;
            }

            default:
                break;
        }

        return super.keyUp(keycode);
    }
}
