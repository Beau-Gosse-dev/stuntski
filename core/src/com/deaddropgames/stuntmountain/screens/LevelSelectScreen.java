package com.deaddropgames.stuntmountain.screens;

import java.util.ArrayList;
import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Json;
import com.deaddropgames.stuntmountain.level.FileLevelList;
import com.deaddropgames.stuntmountain.level.Level;
import com.deaddropgames.stuntmountain.level.LevelPack;
import com.deaddropgames.stuntmountain.StuntMountain;

public class LevelSelectScreen extends AbstractScreen {

    private static final String LOG = "LevelSelectScreen";

    private LevelPack levelPack;

    private static String lastLevelPackFilename;

    private Sprite gcSprite;
    private Sprite bsSprite;
    private Sprite bdSprite;
    private Sprite dbdSprite;
    private TextureRegionDrawable trd;

    LevelSelectScreen(StuntMountain game) {

        super(game);

        Gdx.app.debug(LOG, "MenuScreen()");

        if(uiAtlas == null) {

            uiAtlas = game.assetManager.get("assets/packedimages/ui.atlas", TextureAtlas.class);
        }
        gcSprite = uiAtlas.createSprite("green_circle");
        bsSprite = uiAtlas.createSprite("blue_square");
        bdSprite = uiAtlas.createSprite("black_diamond");
        dbdSprite = uiAtlas.createSprite("double_black_diamond");

        Pixmap oddRowPixmap = new Pixmap(1, 1, Pixmap.Format.RGB565);
        oddRowPixmap.setColor(0f, 0f, 0f, 1f);
        oddRowPixmap.fill();

        trd = new TextureRegionDrawable(new TextureRegion(new Texture(oddRowPixmap)));
    }

    static void clearLastLevelPackFilename() {

        lastLevelPackFilename = null;
    }

    void loadLevelPack(final String levelPackFilename) {

        Gdx.app.debug(LOG, "loadLevelPack()");
        Json json = new Json();
        FileHandle levelFile = Gdx.files.internal(levelPackFilename);
        levelPack = json.fromJson(LevelPack.class, levelFile);

        if(levelPack == null) {

            Gdx.app.error(LOG, "Failed to load level pack: " + levelPackFilename);
        } else {

            lastLevelPackFilename = levelPackFilename;
        }
    }

    boolean loadLastLevelPack() {

        Gdx.app.debug(LOG, "loadLastLevelPack()");
        if(lastLevelPackFilename == null) {

            return false;
        }

        Json json = new Json();
        FileHandle levelFile = Gdx.files.internal(lastLevelPackFilename);
        levelPack = json.fromJson(LevelPack.class, levelFile);

        if(levelPack == null) {

            Gdx.app.error(LOG, "Failed to load LAST level pack: " + lastLevelPackFilename);
            return false;
        }

        return true;
    }

    @Override
    public void show() {

        Gdx.app.debug(LOG, "Show()");
        super.show();

        if(levelPack == null) {

            Gdx.app.error(LOG, "No level pack to load or level pack failed to load.");
            return;
        }

        Skin skin = super.getSkin();

        Table table = new Table(skin);
        table.setWidth(stage.getWidth());
        table.setHeight(stage.getHeight());

        table.add(createTitleLabel(String.format(Locale.getDefault(), "%s - %d Levels", levelPack.name, levelPack.levelFilenames.size()))).spaceBottom(10 * game.scaleFactor);
        table.row();
        
        Json json = new Json();
        final ArrayList<Level> levels = new ArrayList<Level>();
        for(String filename : levelPack.levelFilenames) {

            FileHandle levelFilename = Gdx.files.internal(filename);
            if(levelFilename.exists()) {

                Level level = json.fromJson(Level.class, levelFilename);
                if(level != null) {

                    levels.add(level);
                } else {

                    Gdx.app.error(LOG, "Failed to load level: " + filename);
                }
            } else {

                Gdx.app.error(LOG, "Level file does not exist: " + filename);
            }
        }

        FileLevelList levelList = new FileLevelList(levels);

        Table levelsTable = new Table(skin);
        for(final Level level : levels) {

            levelsTable.add(createLevelRow(level, levelList)).spaceBottom(5 * game.scaleFactor).pad(5 * game.scaleFactor);
            levelsTable.row();
        }

        ScrollPane scrollPane = new ScrollPane(levelsTable, skin);
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
                game.setScreen(new LevelPacksScreen(game));
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

            game.setScreen(new LevelPacksScreen(game));
            break;
        }

        default:
            break;
        }

        return super.keyUp(keycode);
    }

    private Table createLevelRow(final Level level, final FileLevelList fileLevelList) {

        Table row = new Table(getSkin());

        Table textTable = new Table(getSkin());
        
        // Create a horizontal group for level name and completion status
        Table nameTable = new Table(getSkin());
        nameTable.add(createButtonLabel(level.name)).left().pad(5 * game.scaleFactor);
        
        // Check if level is completed and add indicator
        if (game.levelProgress.isLevelCompleted(level.getStatsId())) {
            Label completedLabel = new Label(" [DONE]", getSkin());
            completedLabel.setColor(0, 0.8f, 0, 1); // Green color
            nameTable.add(completedLabel).padLeft(10 * game.scaleFactor);
        }
        
        textTable.add(nameTable).left();
        textTable.row();
        Label desc = new Label(level.description, getSkin());
        desc.setWrap(true);
        textTable.add(desc).left().width(stage.getWidth() * 0.5f).pad(5 * game.scaleFactor);

        row.add(textTable).spaceRight(10 * game.scaleFactor).left();

        Image image;
        switch (level.difficulty) {

            case 0:
                image = new Image(new SpriteDrawable(gcSprite));
                break;
            case 1:
                image = new Image(new SpriteDrawable(bsSprite));
                break;
            case 2:
                image = new Image(new SpriteDrawable(bdSprite));
                break;
            case 3:
            default:
                image = new Image(new SpriteDrawable(dbdSprite));
                break;
        }

        row.add(image).spaceRight(10 * game.scaleFactor)
                .maxHeight(getButtonHeight() * 0.5f * game.scaleFactor)
                .maxWidth(getButtonHeight() * 0.5f * game.scaleFactor);

        TextButton playButton = new TextButton("Play", getSkin());
        playButton.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {

                LevelScreen levelScreen = new LevelScreen(game);
                levelScreen.setLevel(level);
                levelScreen.setLevelList(fileLevelList);
                game.setScreen(levelScreen);
            }
        });

        row.add(playButton).height(getButtonHeight()).right().pad(5 * game.scaleFactor);
        row.setBackground(trd);

        return row;
    }
}
