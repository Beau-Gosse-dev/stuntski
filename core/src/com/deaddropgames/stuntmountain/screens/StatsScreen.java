package com.deaddropgames.stuntmountain.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.deaddropgames.stuntmountain.StuntMountain;
import com.deaddropgames.stuntmountain.util.LevelStats;
import java.util.Locale;

public class StatsScreen extends AbstractScreen {

    private static final String LOG = "StatsScreen";

    public StatsScreen(StuntMountain game) {
        super(game);
        Gdx.app.debug(LOG, "StatsScreen()");
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

        table.add(createTitleLabel("Your Stats")).spaceBottom(30 * game.scaleFactor);
        table.row();

        // Create stats table with background
        Table statsTable = new Table(skin);
        // Initialize uiAtlas if not already loaded
        if (uiAtlas == null) {
            uiAtlas = game.assetManager.get("assets/packedimages/ui.atlas", TextureAtlas.class);
        }
        NinePatchDrawable patch = new NinePatchDrawable(uiAtlas.createPatch("background"));
        statsTable.setBackground(patch);
        statsTable.pad(20 * game.scaleFactor);

        // Get stats data
        int completedLevels = game.levelProgress.getCompletedLevelCount();
        
        // Find highest score and longest airtime
        Json json = new Json();
        int highestScore = 0;
        String highestScoreLevel = "";
        float longestAirtime = 0f;
        String longestAirtimeLevel = "";
        
        // Iterate through all saved level stats
        for (String key : game.levelStats.get().keySet()) {
            String statsJson = game.levelStats.getString(key);
            if (statsJson != null && !statsJson.isEmpty()) {
                try {
                    LevelStats stats = json.fromJson(LevelStats.class, statsJson);
                    if (stats != null) {
                        // Check for highest score
                        if (stats.getPoints() > highestScore) {
                            highestScore = stats.getPoints();
                            highestScoreLevel = formatLevelName(key);
                        }
                        
                        // Check for longest airtime
                        if (stats.getAirTimeSeconds() > longestAirtime) {
                            longestAirtime = stats.getAirTimeSeconds();
                            longestAirtimeLevel = formatLevelName(key);
                        }
                    }
                } catch (Exception e) {
                    Gdx.app.error(LOG, "Failed to parse stats for: " + key, e);
                }
            }
        }

        // Display stats
        addStatRow(statsTable, "Levels Completed:", String.valueOf(completedLevels), "");
        
        if (highestScore > 0) {
            statsTable.row().padTop(10 * game.scaleFactor);
            addStatRow(statsTable, "Highest Score:", 
                String.format(Locale.getDefault(), "%d points", highestScore), 
                highestScoreLevel);
        }
        
        if (longestAirtime > 0) {
            statsTable.row().padTop(10 * game.scaleFactor);
            addStatRow(statsTable, "Longest Airtime:", 
                String.format(Locale.getDefault(), "%.1f seconds", longestAirtime), 
                longestAirtimeLevel);
        }

        table.add(statsTable).width(stage.getWidth() * 0.8f).spaceBottom(30 * game.scaleFactor);
        table.row();

        // Back button
        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MenuScreen(game));
            }
        });

        table.add(backButton).size(getButtonWidth(), getButtonHeight());
    }

    private void addStatRow(Table table, String label, String value, String level) {
        Label labelWidget = createButtonLabel(label);
        labelWidget.setColor(0.8f, 0.8f, 0.8f, 1f);
        table.add(labelWidget).right().padRight(10 * game.scaleFactor);
        
        Label valueWidget = createButtonLabel(value);
        valueWidget.setColor(1f, 1f, 1f, 1f);
        table.add(valueWidget).left().padRight(20 * game.scaleFactor);
        
        if (!level.isEmpty()) {
            Label levelWidget = createButtonLabel("on " + level);
            levelWidget.setColor(0.7f, 0.9f, 0.7f, 1f); // Light green
            table.add(levelWidget).left();
        } else {
            table.add(); // Empty cell to maintain column alignment
        }
        
        table.row().padTop(5 * game.scaleFactor);
    }

    private String formatLevelName(String levelId) {
        // Handle online levels
        if (levelId.startsWith("online:")) {
            return "Online Level #" + levelId.substring(7);
        }
        
        // Handle local levels - extract just the level name
        String[] parts = levelId.split(",");
        if (parts.length > 0) {
            return parts[0].trim();
        }
        
        return levelId;
    }

    @Override
    public boolean keyUp(int keycode) {
        Gdx.app.debug(LOG, "keyUp(" + keycode + ")");
        
        switch(keycode) {
            case Keys.ESCAPE:
            case Keys.BACK:
                game.setScreen(new MenuScreen(game));
                break;
            default:
                break;
        }

        return super.keyUp(keycode);
    }
}