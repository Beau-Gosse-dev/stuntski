package com.deaddropgames.stuntmountain.screens;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.deaddropgames.stuntmountain.StuntMountain;

public class MenuScreen extends AbstractScreen {

    private static final String LOG = "MenuScreen";

    public MenuScreen(StuntMountain game) {

        super(game);

        Gdx.app.debug(LOG, "MenuScreen()");
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

        table.add(createButtonLabel("dead drop games")).spaceBottom(10 * game.scaleFactor);
        table.row();

        table.add(createTitleLabel("Welcome to StuntSki Lite!")).spaceBottom(50 * game.scaleFactor);
        table.row();
        
        // play button
        TextButton startGameButton = new TextButton("Play", skin);
        
        // add button functionality
        startGameButton.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {

                game.setScreen(new LevelPacksScreen(game));
            }
        });
        
        table.add(startGameButton).size(getButtonWidth(), getButtonHeight()).uniform().fill().spaceBottom(10);
        table.row();
        
        // options button
        TextButton settingsButton = new TextButton("Settings", skin);
        settingsButton.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {

                game.setScreen(new SettingsScreen(game));
            }
        });
        
        table.add(settingsButton).uniform().fill().spaceBottom(10);
        table.row();

        // options button
        TextButton creditsButton = new TextButton("Credits", skin);
        creditsButton.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {

                game.setScreen(new CreditsScreen(game));
            }
        });

        table.add(creditsButton).uniform().fill().spaceBottom(10);
        table.row();

        
        // exit button - doesn't apply for web
        if(Gdx.app.getType() != Application.ApplicationType.WebGL) {

            TextButton exitButton = new TextButton("Exit", skin);
            exitButton.addListener(new ClickListener() {

                @Override
                public void clicked(InputEvent event, float x, float y) {

                onExit();
                }
            });

            table.add(exitButton).uniform().fill().spaceBottom(10);
            table.row();
        }
    }

    @Override
    public boolean keyUp(int keycode) {

        Gdx.app.debug(LOG, "keyUp(" + keycode + ")");
        switch(keycode) {

        case Keys.ENTER:
        case Keys.BACK: { // enter key

            onExit();
            break;
        }

        default:
            break;
        }

        return super.keyUp(keycode);
    }

    private void onExit() {

        Gdx.app.debug(LOG, "onExit()");
        Gdx.app.exit();
    }
}
