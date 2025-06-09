package com.deaddropgames.stuntmountain.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Json;
import com.deaddropgames.stuntmountain.level.LevelConfig;
import com.deaddropgames.stuntmountain.level.LevelPack;
import com.deaddropgames.stuntmountain.StuntMountain;

public class LevelPacksScreen extends AbstractScreen {

    private static final String LOG = "LevelPacksScreen";

    private LevelConfig levelConfig;

    LevelPacksScreen(StuntMountain game) {

        super(game);

        Gdx.app.debug(LOG, "LevelPacksScreen()");

        Json json = new Json();
        FileHandle levelConfigFile = Gdx.files.internal("assets/data/level_config.json");
        levelConfig = json.fromJson(LevelConfig.class, levelConfigFile);

        if(levelConfig == null) {

            Gdx.app.error(LOG, "Failed to load the level configuration file.");
        }
    }

    @Override
    public void show() {

        Gdx.app.debug(LOG, "Show()");
        super.show();

        Skin skin = super.getSkin();

        Table table = new Table(skin);
        table.setWidth(stage.getWidth());
        table.setHeight(stage.getHeight());

        table.add(createTitleLabel("Select Mountain")).spaceBottom(10 * game.scaleFactor);
        table.row();

        Table levelPacksTable = new Table(skin);
        for(final String levelPackFilename : levelConfig.levelPacks) {

            FileHandle levelPackFile = Gdx.files.internal(levelPackFilename);
            Json json = new Json();
            LevelPack levelPack = json.fromJson(LevelPack.class, levelPackFile);

            TextButton levelPackBtn = new TextButton(levelPack.name, skin);
            levelPackBtn.addListener(new ClickListener() {

                @Override
                public void clicked(InputEvent event, float x, float y) {

                    LevelSelectScreen levelSelectScreen = new LevelSelectScreen(game);
                    levelSelectScreen.loadLevelPack(levelPackFilename);
                    game.setScreen(levelSelectScreen);
                }
            });

            levelPacksTable.add(levelPackBtn).size(getButtonWidth(), getButtonHeight()).uniform().spaceBottom(10);
            levelPacksTable.row();
        }

        final String userSubTitle = "User Submitted - Latest";
        TextButton userSubmittedButton = new TextButton(userSubTitle, skin);
        userSubmittedButton.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {

                game.setScreen(new OnlineLevelSelectScreen(game, userSubTitle, OnlineLevelSelectScreen.USER_SUB_LATEST));
            }
        });
        levelPacksTable.add(userSubmittedButton).size(getButtonWidth(), getButtonHeight()).uniform().spaceBottom(10);
        levelPacksTable.row();

        final String topRatedTitle = "User Submitted - Top Rated";
        TextButton topRatedButton = new TextButton(topRatedTitle, skin);
        topRatedButton.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {

                game.setScreen(new OnlineLevelSelectScreen(game, topRatedTitle, OnlineLevelSelectScreen.USER_SUB_TOP_RATED));
            }
        });
        levelPacksTable.add(topRatedButton).size(getButtonWidth(), getButtonHeight()).uniform().spaceBottom(10);
        levelPacksTable.row();
        
        ScrollPane scrollPane = new ScrollPane(levelPacksTable, skin);
        scrollPane.setWidth(stage.getWidth());
        scrollPane.setHeight(stage.getHeight());
        scrollPane.setScrollingDisabled(true, false); // Disable horizontal scrolling, enable vertical
        scrollPane.setFadeScrollBars(false); // Keep scrollbars visible

        table.add(scrollPane).width(stage.getWidth());
        table.row();

        stage.addActor(table);
        
        // Set focus on the scroll pane for immediate mouse wheel scrolling
        stage.setScrollFocus(scrollPane);

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

        case Keys.ENTER:
        case Keys.BACK: { // enter key

            game.setScreen(new MenuScreen(game));
            break;
        }

        default:
            break;
        }

        return super.keyUp(keycode);
    }
}
