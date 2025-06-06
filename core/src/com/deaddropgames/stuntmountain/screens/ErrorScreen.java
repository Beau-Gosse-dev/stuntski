package com.deaddropgames.stuntmountain.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.deaddropgames.stuntmountain.StuntMountain;

public class ErrorScreen extends AbstractScreen {

    private static final String LOG = "ErrorScreen";
    private String errMsg;

    ErrorScreen(StuntMountain game, final String errMsg) {

        super(game);

        this.errMsg = errMsg;

        Gdx.app.debug(LOG, "ErrorScreen()");
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

        table.add(createTitleLabel("Doh! Something bad happened...")).spaceBottom(50 * game.scaleFactor);
        table.row();

        Label errMsgLabel = createButtonLabel(errMsg);
        errMsgLabel.setWrap(true);
        table.add(errMsgLabel).spaceBottom(50 * game.scaleFactor).left();
        table.row();

        table.add("Sorry :(").spaceBottom(50 * game.scaleFactor);
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
