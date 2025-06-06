package com.deaddropgames.stuntmountain;

import java.util.Locale;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Json;
import com.deaddropgames.stuntmountain.level.Level;
import com.deaddropgames.stuntmountain.screens.*;
import com.deaddropgames.stuntmountain.settings.*;

public class StuntMountain extends Game {

    // settings
    public BipedSettings bipedSettings;
    public DrawSettings drawSettings;
    public LevelSettings levelSettings;
    public SkiSettings skiSettings;
    public TreeSettings treeSettings;

    // preferences/options
    public Preferences preferences;
    public Preferences levelStats;

    // asset manager
    public AssetManager assetManager;

    // drawing utilities
    public SpriteBatch spriteBatch;

    public float scaleFactor = 1.0f;

    private static final String LOG = "SkiSim";

    private String startupLevelFilename;
    private boolean debugMode;

    public StuntMountain() {

        startupLevelFilename = null;
        debugMode = false;
    }

    /*
     * param args: can contain a startup JSON level file and/or a debug flag
     */
    public StuntMountain(String[] args) {

        startupLevelFilename = null;
        debugMode = false;
        if(args != null && args.length > 0) {

            for(String arg : args) {

                if(arg.compareToIgnoreCase("--debug") == 0) {

                    debugMode = true;
                } else if(arg.toLowerCase().endsWith(".json")) {

                    if(startupLevelFilename != null) {

                        Gdx.app.error(LOG, "Found more than one level filename in startup arguments; last specified level will be used.");
                    }
                    startupLevelFilename = arg;
                } else {

                    Gdx.app.error(LOG, "Found unknown argument: " + arg);
                }
            }
        }
    }

    @Override
    public void create() {

        if(debugMode) {

            Gdx.app.setLogLevel(Application.LOG_DEBUG);
        } else {

            Gdx.app.setLogLevel(Application.LOG_INFO);
        }

        Gdx.app.debug(LOG, "create()");

        // TODO: we may need different sized assets for different screens...
        assetManager = new AssetManager();
        assetManager.load("assets/packedimages/ui.atlas", TextureAtlas.class);
        assetManager.load("assets/packedimages/biped1.atlas", TextureAtlas.class);
        assetManager.load("assets/sound/bindings.mp3", Sound.class);
        assetManager.load("assets/sound/skiing.mp3", Sound.class);
        assetManager.load("assets/sound/clapping.mp3", Sound.class);
        assetManager.load("assets/sound/ouch001.mp3", Sound.class);
        assetManager.load("assets/sound/ouch002.mp3", Sound.class);
        assetManager.load("assets/sound/ouch003.mp3", Sound.class);
        assetManager.load("assets/sound/ouch004.mp3", Sound.class);
        assetManager.load("assets/sound/ouch005.mp3", Sound.class);
        assetManager.load("assets/sound/ouch006.mp3", Sound.class);
        assetManager.load("assets/sound/ouch007.mp3", Sound.class);
        assetManager.finishLoading();

        // load various settings
        Json json = new Json();
        FileHandle bipedSettingsFile = Gdx.files.internal("assets/settings/biped.json");
        bipedSettings = json.fromJson(BipedSettings.class, bipedSettingsFile);

        FileHandle drawSettingsFile = Gdx.files.internal("assets/settings/draw.json");
        drawSettings = json.fromJson(DrawSettings.class, drawSettingsFile);

        FileHandle levelSettingsFile = Gdx.files.internal("assets/settings/level.json");
        levelSettings = json.fromJson(LevelSettings.class, levelSettingsFile);

        FileHandle skiSettingsFile = Gdx.files.internal("assets/settings/ski.json");
        skiSettings = json.fromJson(SkiSettings.class, skiSettingsFile);

        FileHandle treeSettingsFile = Gdx.files.internal("assets/settings/tree.json");
        treeSettings = json.fromJson(TreeSettings.class, treeSettingsFile);

        // create preferences
        preferences = Gdx.app.getPreferences("user-preferences");
        if(!preferences.contains("metric")) {

            if(Locale.getDefault() == Locale.US) {

                preferences.putBoolean("metric", false);
            } else {

                preferences.putBoolean("metric", true);
            }
        }

        if(!preferences.contains("controlAreaType")) {

            preferences.putInteger("controlAreaType", levelSettings.defaultControlAreaType);
        }

        if(!preferences.contains("fxVolume")) {

            preferences.putFloat("fxVolume", 1.0f);
        }

        if(!preferences.contains("mute")) {

            preferences.putBoolean("mute", false);
        }

        preferences.flush();

        // get/create the level stats
        levelStats = Gdx.app.getPreferences("level-stats");

        spriteBatch = new SpriteBatch();

        scaleFactor = Gdx.graphics.getWidth() / 800f;

        // see if we should open directly to a level
        Level level = null;
        if(startupLevelFilename != null && startupLevelFilename.endsWith(".json")) {

            // attempt to load it
            FileHandle levelFilename = Gdx.files.absolute(startupLevelFilename);
            if(levelFilename.exists()) {

                level = json.fromJson(Level.class, levelFilename);
                if(level == null) {

                    Gdx.app.error(LOG, "'" + startupLevelFilename + "' failed to load.");
                }
            } else {

                Gdx.app.error(LOG, "'" + startupLevelFilename + "' does not exist.");
            }
        }

        // if we weren't able to load it or it wasn't specified, show the main menu
        if(level == null) {

            setScreen(new MenuScreen(this));
        } else {  // go directly to the loaded level

            LevelScreen levelScreen = new LevelScreen(this);
            levelScreen.setLevel(level);
            setScreen(levelScreen);
        }
    }

    @Override
    public void dispose() {

        Gdx.app.debug(LOG, "dispose()");

        // NOTE: if we dispose the sounds in our level screen, then they are gone forever
        //       if we don't dispose them manually, then we get an audio error on exit...
        assetManager.get("assets/sound/bindings.mp3", Sound.class).dispose();
        assetManager.get("assets/sound/skiing.mp3", Sound.class).dispose();
        assetManager.get("assets/sound/clapping.mp3", Sound.class).dispose();
        assetManager.get("assets/sound/ouch001.mp3", Sound.class).dispose();
        assetManager.get("assets/sound/ouch002.mp3", Sound.class).dispose();
        assetManager.get("assets/sound/ouch003.mp3", Sound.class).dispose();
        assetManager.get("assets/sound/ouch004.mp3", Sound.class).dispose();
        assetManager.get("assets/sound/ouch005.mp3", Sound.class).dispose();
        assetManager.get("assets/sound/ouch006.mp3", Sound.class).dispose();
        assetManager.get("assets/sound/ouch007.mp3", Sound.class).dispose();

        super.dispose();
    }

    @Override
    public void pause() {

        Gdx.app.debug(LOG, "pause()");
        super.pause();
    }

    @Override
    public void resume() {

        Gdx.app.debug(LOG, "resume()");
        super.resume();

        Screen currScreen = getScreen();
        if(currScreen != null) {

            currScreen.pause();
        }
    }

    @Override
    public void render() {

        super.render();
    }

    @Override
    public void resize(int width, int height) {

        Gdx.app.debug(LOG, "resize(" + width + ", " + height + ")");
        super.resize(width, height);
    }

    public boolean isDebugMode() {

        return debugMode;
    }
}
