package com.deaddropgames.stuntmountain.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.deaddropgames.stuntmountain.level.Level;
import com.deaddropgames.stuntmountain.web.LevelRepository;

import java.io.IOException;
import java.net.URISyntaxException;

public class RateLevelDialog extends BaseDialog implements Runnable {

    private static final String LOG = "RateLevelDialog";

    private Level level;
    private int vote;

    public RateLevelDialog(Skin skin, Level level, TextureAtlas uiAtlas, float scaleFactor) {

        this("Gnar Rating", skin, level, uiAtlas, scaleFactor);
    }

    private RateLevelDialog(String title, Skin skin, Level level, TextureAtlas uiAtlas, float scaleFactor) {

        super(title, skin, uiAtlas, scaleFactor);
        this.level = level;

        getContentTable().add(createLabel(level.name, skin)).space(20).spaceBottom(100);

        TextButton sickButton = new TextButton("Sick!", skin);
        button(sickButton, 1);

        TextButton lameButton = new TextButton("Lame!", skin);
        button(lameButton, 0);

        TextButton cancelButton = new TextButton("Cancel", skin);
        button(cancelButton, null);
    }

    @Override
    protected void result(Object object) {

        removeBackdrop();
        setVisible(false);

        // if cancel was pressed - no-op!
        if(object == null) {

            return;
        }

        vote = (Integer)object;

        // async update - close dialog
        // this is safe to do since we aren't updating the UI - no need to post a runnable to libGDX
        new Thread(this).start();
    }

    @Override
    public void run() {

        try {
            LevelRepository.voteForLevel(level.id, vote);
        } catch (IOException e) {

            Gdx.app.error(LOG, "Failed to initialize token", e);
        } catch (URISyntaxException e) {

            Gdx.app.error(LOG, "Failed to initialize token", e);
        }
    }

    @Override
    public Dialog button (Button button, Object object) {

        getButtonTable().add(button).size(buttonWidth*2/3*scaleFactor, buttonHeight*scaleFactor).space(10);
        setObject(button, object);
        return this;
    }
}
