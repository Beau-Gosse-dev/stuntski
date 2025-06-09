package com.deaddropgames.stuntmountain.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import com.deaddropgames.stuntmountain.StuntMountain;
import com.deaddropgames.stuntmountain.level.Level;
import com.deaddropgames.stuntmountain.web.LevelRepository;
import com.deaddropgames.stuntmountain.web.LevelSummary;
import com.deaddropgames.stuntmountain.web.LevelSummaryResult;
import com.deaddropgames.stuntmountain.web.OnlineLevelList;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Locale;


public class OnlineLevelSelectScreen extends AbstractScreen implements Runnable {

    private static final String LOG = "OnlineLevelSelectScreen";

    static final int USER_SUB_LATEST = 0;
    static final int USER_SUB_TOP_RATED = 1;

    private static int lastLevelQueryType = 0;
    private static String lastTitle = "";
    private static String lastFetchUrl = null;

    private Sprite gcSprite;
    private Sprite bsSprite;
    private Sprite bdSprite;
    private Sprite dbdSprite;
    private TextureRegionDrawable trd;
    private Table table;
    private Table loadingTable;
    private String title;
    private int levelQueryType;
    private String fetchUrl;

    OnlineLevelSelectScreen(StuntMountain game) {

        // assumes we have called the other contstructor once already...
        // this constructor is only used when coming back from a level since we want to remember where we were
        this(game, lastTitle, lastLevelQueryType);
    }

    OnlineLevelSelectScreen(StuntMountain game, final String title, final int levelQueryType) {

        super(game);

        this.title = title;
        this.levelQueryType = levelQueryType;
        fetchUrl = null;

        lastTitle = title;
        lastLevelQueryType = levelQueryType;

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

    @Override
    public void show() {

        Gdx.app.debug(LOG, "Show()");
        super.show();

        fetchLevels(lastFetchUrl);
    }

    private void initLoadingTable(Skin skin) {

        if (loadingTable != null) {

            stage.getActors().removeValue(loadingTable, true);
            loadingTable = null;
        }

        loadingTable = new Table(skin);
        loadingTable.add(createButtonLabel("Fetching levels...please wait"));
        loadingTable.setWidth(stage.getWidth());
        loadingTable.setHeight(stage.getHeight());

        stage.addActor(loadingTable);
    }

    private void fetchLevels(final String url) {

        if (table != null) {

            stage.getActors().removeValue(table, true);
            table = null;
        }

        fetchUrl = url;
        lastFetchUrl = fetchUrl;

        Skin skin = super.getSkin();

        table = new Table(skin);
        table.setWidth(stage.getWidth());
        table.setHeight(stage.getHeight());
        table.top();

        table.add(createTitleLabel(title)).spaceBottom(10 * game.scaleFactor);
        table.row();

        initLoadingTable(skin);

        stage.addActor(table);

        createBackButton();
        backButton.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {

                // reset our last params when we hit back
                lastLevelQueryType = 0;
                lastTitle = "";
                lastFetchUrl = null;

                game.preferences.flush();
                game.setScreen(new LevelPacksScreen(game));
            }
        });

        // load the levels asynchronously
        new Thread(this).start();
    }

    @Override
    public boolean keyUp(int keycode) {

        Gdx.app.debug(LOG, "keyUp(" + keycode + ")");
        switch(keycode) {

            case Input.Keys.ENTER:
            case Input.Keys.BACK: { // enter key

                game.setScreen(new LevelPacksScreen(game));
                break;
            }

            default:
                break;
        }

        return super.keyUp(keycode);
    }

    @Override
    public void resize(int width, int height) {

        Gdx.app.debug(LOG, "resize(" + width + ", " + height + ")");
        super.resize(width, height);
    }

    private Table createLevelRow(final LevelSummary levelSummary, final LevelSummaryResult levelSummaryResult) {

        Table row = new Table(getSkin());

        Table textTable = new Table(getSkin());
        
        // Create a horizontal group for level name and completion status
        Table nameTable = new Table(getSkin());
        nameTable.add(createButtonLabel(levelSummaryResult.name)).left().pad(5 * game.scaleFactor);
        
        // Check if level is completed and add indicator
        String levelStatsId = "online:" + levelSummaryResult.id;
        if (game.levelProgress.isLevelCompleted(levelStatsId)) {
            Label completedLabel = new Label(" [DONE]", getSkin());
            completedLabel.setColor(0, 0.8f, 0, 1); // Green color
            nameTable.add(completedLabel).padLeft(10 * game.scaleFactor);
        }
        
        textTable.add(nameTable).left();
        textTable.row();
        Label desc = new Label(levelSummaryResult.description, getSkin());
        desc.setWrap(true);
        textTable.add(desc).left().width(stage.getWidth() * 0.5f).pad(5 * game.scaleFactor);
        textTable.row();
        textTable.add("By: " + levelSummaryResult.author).left().width(stage.getWidth() * 0.5f).pad(5 * game.scaleFactor);

        row.add(textTable).spaceRight(10 * game.scaleFactor).left();

        Image image;
        switch (levelSummaryResult.difficulty) {

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
                Level level = null;
                try {

                    level = LevelRepository.getLevel(levelSummaryResult.id);
                    level.id = levelSummaryResult.id;
                } catch (IOException e) {

                    Gdx.app.error(LOG, String.format(Locale.getDefault(), "Failed to fetch level %d",
                            levelSummaryResult.id), e);
                } catch (URISyntaxException e) {

                    Gdx.app.error(LOG, String.format(Locale.getDefault(), "Failed to fetch level %d",
                            levelSummaryResult.id), e);
                }

                if (level != null) {

                    LevelSelectScreen.clearLastLevelPackFilename();
                    levelScreen.setLevel(level);
                    OnlineLevelList levelList = new OnlineLevelList(levelSummary.results, levelSummaryResult.id);
                    levelScreen.setLevelList(levelList);
                    game.setScreen(levelScreen);
                } else {

                    game.setScreen(new ErrorScreen(game,
                            String.format(Locale.getDefault(),
                                    "Failed to fetch level '%s' from the server. Please try again later.",
                                    levelSummaryResult.name)));
                }
            }
        });

        row.add(playButton).height(getButtonHeight()).right().pad(5 * game.scaleFactor);
        row.setBackground(trd);

        return row;
    }

    @Override
    public void run() {

        // fetch the level summary
        LevelSummary levelSummary = null;
        try {

            switch(levelQueryType) {

                case USER_SUB_LATEST:
                    levelSummary = LevelRepository.getLevelList(fetchUrl);
                    break;
                case USER_SUB_TOP_RATED:
                default:
                    levelSummary = LevelRepository.getTopRatedList(fetchUrl);
                    break;
            }

        } catch (IOException e) {

            Gdx.app.error(LOG, "Failed to fetch level list", e);
        } catch (URISyntaxException e) {

            Gdx.app.error(LOG, "Failed to fetch level list", e);
        }

        // post the Runnable to LibGDX since it's not thread safe
        final LevelSummary ls = levelSummary;
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {

                doUiUpdate(ls);
            }
        });
    }

    private void doUiUpdate(final LevelSummary levelSummary) {

        Skin skin = getSkin();
        if (loadingTable != null) {

            stage.getActors().removeValue(loadingTable, true);
            loadingTable = null;
        }

        Table levelsTable = new Table(skin);

        // if the request timed out or failed, show an error message
        if (levelSummary == null) {

            Label label = createButtonLabel("Doh! We failed to read from the server, please try again later.");
            label.setWrap(true);
            levelsTable.add(label);
        } else {

            // otherwise populate the levels table
            for (final LevelSummaryResult levelSummaryResult : levelSummary.results) {

                levelsTable.add(createLevelRow(levelSummary, levelSummaryResult)).spaceBottom(5 * game.scaleFactor).pad(5 * game.scaleFactor);
                levelsTable.row();
            }
        }

        ScrollPane scrollPane = new ScrollPane(levelsTable, skin);
        scrollPane.setWidth(stage.getWidth());
        scrollPane.setHeight(stage.getHeight());
        scrollPane.setScrollingDisabled(true, false); // Disable horizontal scrolling, enable vertical
        scrollPane.setFadeScrollBars(false); // Keep scrollbars visible

        table.add(scrollPane).width(stage.getWidth());
        table.row();
        
        // Set focus on the scroll pane for immediate mouse wheel scrolling
        stage.setScrollFocus(scrollPane);

        // and next/prev buttons
        if (levelSummary != null && (levelSummary.previous != null || levelSummary.next != null)) {

            Table navTable = new Table(skin);
            if (levelSummary.previous != null && levelSummary.previous.length() > 0) {

                TextButton previousBtn = new TextButton("< Prev", skin);
                final String url = levelSummary.previous;
                previousBtn.addListener(new ClickListener() {

                    @Override
                    public void clicked(InputEvent event, float x, float y) {

                        fetchLevels(url);
                    }
                });
                navTable.add(previousBtn).size(getButtonWidth()/2, getButtonHeight()).left().spaceRight(5 * game.scaleFactor);
            } else {

                navTable.add().size(getButtonWidth()/2, getButtonHeight());
            }

            if (levelSummary.next != null && levelSummary.next.length() > 0) {

                TextButton nextBtn = new TextButton("Next >", skin);
                final String url = levelSummary.next;
                nextBtn.addListener(new ClickListener() {

                    @Override
                    public void clicked(InputEvent event, float x, float y) {

                        fetchLevels(url);
                    }
                });
                navTable.add(nextBtn).size(getButtonWidth()/2, getButtonHeight()).right().spaceLeft(5 * game.scaleFactor);
            } else {

                navTable.add().size(getButtonWidth()/2, getButtonHeight());
            }

            navTable.row();

            table.add(navTable).height(getButtonHeight()).expandX().center().space(5 * game.scaleFactor);
            table.row();
        }
    }
}
