package com.deaddropgames.stuntmountain.screens;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Json;
import com.deaddropgames.stuntmountain.level.*;
import com.deaddropgames.stuntmountain.sim.BipedJump;
import com.deaddropgames.stuntmountain.sim.BodyType;
import com.deaddropgames.stuntmountain.sim.ControlArea;
import com.deaddropgames.stuntmountain.sim.SimpleBiped;
import com.deaddropgames.stuntmountain.StuntMountain;
import com.deaddropgames.stuntmountain.sim.TerrainShape;
import com.deaddropgames.stuntmountain.sim.TreeModel;
import com.deaddropgames.stuntmountain.ui.LoginDialog;
import com.deaddropgames.stuntmountain.ui.RateLevelDialog;
import com.deaddropgames.stuntmountain.util.GameMessageQueue;
import com.deaddropgames.stuntmountain.util.LevelStats;
import com.deaddropgames.stuntmountain.web.LevelRepository;
import com.deaddropgames.stuntmountain.web.OnlineLevelList;

public class LevelScreen extends AbstractScreen implements ContactListener {

    private static final String LOG = "LevelScreen";

    private World world;
    private SimpleBiped skier;
    private boolean skierBailed;
    private boolean isPaused;
    private boolean levelFinished;

    // dynamic objects
    private TreeModel [] trees;

    // launch/land detection
    private boolean touchingTerrain;
    private boolean airborne;
    private long airTimeStart;
    private Vector2 launchPos;

    private int points;

    // flip detection
    private float lastRotationAngle;
    private float rotationCounter;
    private long rotationTimer;
    private BipedJump lastJump;

    private int height;
    private float aspectRatio;

    private OrthographicCamera camera;
    private OrthographicCamera hudCamera;

    // cached vectors that get re-used instead of new-ed
    private Vector2 extents;

    // to determine what shapes are within the viewport
    private Rectangle viewport;

    private float maxSpeedForMaxScaleInv;

    private ShapeRenderer shapeRenderer;

    // for the HUD
    private Label speedLabel;
    private String speedDesignator;
    private boolean isMetric;
    private long startTime;
    private long levelStartTime;
    private float btnWidth = -1;
    private float btnHeight = -1;

    private Label pointsLabel;
    private Label levelNameLabel;
    private Label bailedLabel;
    private TextButton bailedRestartBtn;

    private GameMessageQueue msgQueue;

    // some conversion constants
    private static final float mpsTokmph = 3.6f;
    private static final float mpsTomph = 2.2369356f;
    private float speedConversion = 0.0f;

    private List<TerrainShape> terrainShapeList;

    // string constants
    final private String tapToStartMsg = "Tap joypad to continue";
    final private String bailedMsg = "Bailed!";
    final private String levelFinishedMsg = "Level Complete";

    // control area
    private ControlArea controlArea;
    private ImageButton playBtn;
    private ImageButton pauseBtn;
    private ImageButton muteBtn;
    private Table buttonsTable;

    private Table finishedTable;
    private Table pausedTable;

    // level reference
    private Level level;
    private ILevelList levelList;

    // timestep related
    private float accumulator;
    private final float dt = 1.0f/100.0f;
    private static final float maxDt = 0.25f;

    //private FPSLogger fpsLogger;
    private LevelStats stats;

    private TextureAtlas uiAtlas;

    private Json json;

    // Sounds and related
    private Sound bindingsSound;
    private Sound skiingSound;
    private List<Sound> ouchSounds;
    private Sound clappingSound;
    private Random random;
    private Long skiingSoundId;
    private long lastBailSound;

    // modal dialogs
    private Dialog modalDlg;

    public LevelScreen(StuntMountain game) {

        super(game);

        Gdx.app.debug(LOG, "LevelScreen()");

        world = null;

        skier = null;
        skierBailed = false;

        isPaused = true;
        levelFinished = false;

        touchingTerrain = false;
        airborne = false;
        airTimeStart = 0;
        lastRotationAngle = 0;
        rotationCounter = 0;
        rotationTimer = 0;
        lastJump = null;

        height = 0;
        aspectRatio = 1.0f;

        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setColor(Color.WHITE);

        startTime = System.currentTimeMillis();
        levelStartTime = 0;
        isMetric = game.preferences.getBoolean("metric", true);
        if(isMetric) {

            speedConversion = mpsTokmph;
            speedDesignator = " km/h";
        } else {

            speedConversion = mpsTomph;
            speedDesignator = " mph";
        }

        terrainShapeList = new ArrayList<TerrainShape>();

        viewport = new Rectangle();
        launchPos = new Vector2();

        maxSpeedForMaxScaleInv = 1.0f / game.drawSettings.maxSpeedForMaxScale;

        accumulator = 0.0f;

        //fpsLogger = new FPSLogger();
        stats = new LevelStats();

        uiAtlas = game.assetManager.get("assets/packedimages/ui.atlas", TextureAtlas.class);

        json = new Json();
        json.setIgnoreUnknownFields(true);

        bindingsSound = game.assetManager.get("assets/sound/bindings.mp3", Sound.class);
        skiingSound = game.assetManager.get("assets/sound/skiing.mp3", Sound.class);
        skiingSoundId = null;

        ouchSounds = new ArrayList<Sound>();
        ouchSounds.add(game.assetManager.get("assets/sound/ouch001.mp3", Sound.class));
        ouchSounds.add(game.assetManager.get("assets/sound/ouch002.mp3", Sound.class));
        ouchSounds.add(game.assetManager.get("assets/sound/ouch003.mp3", Sound.class));
        ouchSounds.add(game.assetManager.get("assets/sound/ouch004.mp3", Sound.class));
        ouchSounds.add(game.assetManager.get("assets/sound/ouch005.mp3", Sound.class));
        ouchSounds.add(game.assetManager.get("assets/sound/ouch006.mp3", Sound.class));
        ouchSounds.add(game.assetManager.get("assets/sound/ouch007.mp3", Sound.class));

        clappingSound = game.assetManager.get("assets/sound/clapping.mp3", Sound.class);

        lastBailSound = 0;

        random = new Random();
    }

    // reset the level
    private void reset() {

        Gdx.app.debug(LOG, "reset()");

        destroySkier();
        skier = new SimpleBiped(world,
                game.skiSettings,
                game.bipedSettings,
                game.assetManager.get("assets/packedimages/biped1.atlas", TextureAtlas.class));
        skierBailed = false;
        levelFinished = false;
        isPaused = true;

        touchingTerrain = false;
        airborne = false;
        airTimeStart = 0;
        lastRotationAngle = 0;
        rotationCounter = 0;
        rotationTimer = 0;
        lastJump = null;
        levelStartTime = 0;

        points = 0;

        accumulator = 0.0f;

        createTrees();

        if(pointsLabel != null) {

            pointsLabel.setText("0");
        }

        if(msgQueue != null) {

            msgQueue.setStaticMessage(tapToStartMsg);
        }
        
        if(bailedLabel != null) {
            bailedLabel.setText("");
        }
        
        if(bailedRestartBtn != null) {
            bailedRestartBtn.setVisible(false);
        }

        if(speedLabel != null) {

            speedLabel.setText("0" + speedDesignator);
        }

        if(levelNameLabel != null && level != null) {

            levelNameLabel.setText(level.name);
        }

        if(controlArea != null) {

            controlArea.reset();
        }

        if(finishedTable != null) {

            stage.getActors().removeValue(finishedTable, true);
            finishedTable = null;
        }

        if(pausedTable != null) {

            stage.getActors().removeValue(pausedTable, true);
            pausedTable = null;
        }

        stats = new LevelStats();

        if(skiingSoundId != null) {

            skiingSound.stop(skiingSoundId);
            skiingSoundId = null;
        }

        lastBailSound = 0;
    }

    public void setLevel(final Level level) {

        Gdx.app.debug(LOG, "setLevel()");

        if (level == null) {

            game.setScreen(new ErrorScreen(game,
                    "Failed to load level. Please try again later."));
            return;
        }

        this.level = level;

        isPaused = true;

        terrainShapeList.clear();

        world = new World(new Vector2(0.0f, game.levelSettings.gravity), game.levelSettings.doSleep);

        // NOTE: since this class is also the contact listener, it gets initiated before the world,
        //       so we have to manually set it after the world is created
        world.setContactListener(this);

        // create terrain
        Vector2[] points;
        boolean isFirst = true;
        for(PolyLine polyLine : level.polyLines) {

            // the first terrain in the list should be the start, so we append on some buffer
            if(isFirst) {

                isFirst = false;
                points = new Vector2[polyLine.points.length];

                // add on the default level start
                float slope = (polyLine.points[0].y - polyLine.points[1].y) /
                        (polyLine.points[0].x - polyLine.points[1].x);
                points[0] = new Vector2(-20.0f, -20.0f * slope);
                for(int ii = 1; ii < polyLine.points.length; ii++) {

                    points[ii] = new Vector2(polyLine.points[ii].x, polyLine.points[ii].y);
                }
            } else {

                points = new Vector2[polyLine.points.length];
                for(int ii = 0; ii < polyLine.points.length; ii++) {

                    points[ii] = new Vector2(polyLine.points[ii].x, polyLine.points[ii].y);
                }
            }

            // creates the terrain used by the model
            createModelTerrain(points);

            // creates the terrain that gets drawn
            createDrawTerrain(points);
        }

        // append on some terrain in the x direction at the end
        points = new Vector2[2];
        PolyLine lastPolyLine = level.polyLines[level.polyLines.length - 1];
        Point lastPoint = lastPolyLine.points[lastPolyLine.points.length - 1];
        points[0] = new Vector2(lastPoint.x, lastPoint.y);
        points[1] = new Vector2(lastPoint.x + 20.0f, lastPoint.y);
        createModelTerrain(points);
        createDrawTerrain(points);
        createTrees();

        reset();
    }

    /**
     * A reference to the other levels in this pack
     */
    void setLevelList(final ILevelList levelList) {

        this.levelList = levelList;
    }

    private void createModelTerrain(final Vector2[] vertices) {

        Gdx.app.debug(LOG, "createModelTerrain()");

        if(vertices.length < 2) {

            Gdx.app.error(LOG, "Cannot create terrain with less than 2 points.");
            return;
        }

        // creates terrain as a chain
        FixtureDef bodyFixtureDef = new FixtureDef();
        bodyFixtureDef.friction = game.levelSettings.surfaceFriction;

        BodyDef bd = new BodyDef();
        bd.position.set(0.0f, 0.0f);

        ChainShape shape = new ChainShape();
        shape.createChain(vertices);

        Body section = world.createBody(bd);

        bodyFixtureDef.shape = shape;
        section.createFixture(bodyFixtureDef);
        section.setUserData(new BodyType(BodyType.BodyTypeTerrain));
    }

    // creates the terrain that gets drawn
    private void createDrawTerrain(final Vector2[] vertices) {

        Gdx.app.debug(LOG, "createDrawTerrain()");
        for(int ii = 0; ii < (vertices.length - 1); ii++) {

            terrainShapeList.add(new TerrainShape(vertices[ii], vertices[ii + 1]));
        }
    }

    private void createTrees() {

        destroyTrees();
        if(level.trees != null) {

            trees = new TreeModel[level.trees.length];
            for (int ii = 0; ii < level.trees.length; ii++) {

                trees[ii] = new TreeModel(world, level.trees[ii], game.treeSettings);
            }
        }
    }

    @Override
    public void pause() {

        Gdx.app.debug(LOG, "pause()");

        super.pause();

        isPaused = true;
    }

    @Override
    public void resume() {

        Gdx.app.debug(LOG, "resume()");

        super.resume();

        isPaused = false;
    }

    @Override
    public void resize(int width, int height) {

        Gdx.app.debug(LOG, "resize(" + width + ", " + height + ")");
        super.resize(width, height);

        this.height = height;
        aspectRatio = (float)width / (float)height;
        camera = new OrthographicCamera(width, height);
        hudCamera = new OrthographicCamera(width, height);

        // control area is a preference...
        int controlAreaType = game.preferences.getInteger("controlAreaType", game.levelSettings.defaultControlAreaType);
        Sprite controlKnob = uiAtlas.createSprite("controlknob");
        controlKnob.setScale(game.scaleFactor);
        controlArea = new ControlArea(width, height, controlAreaType, controlKnob);

        adjustViewport();
    }

    @Override
    public void show() {

        Gdx.app.debug(LOG, "show()");
        super.show();

        // add actors/groups to the stage
        Skin skin = super.getSkin();

        // create our image buttons...
        buttonsTable = new Table();
        buttonsTable.setWidth(stage.getWidth());
        buttonsTable.setHeight(stage.getHeight());
        buttonsTable.top().left();
        if(game.scaleFactor > 1f) {

            buttonsTable.padTop(10 * game.scaleFactor);
        }
        Sprite playSpr = uiAtlas.createSprite("control_play");
        btnWidth = playSpr.getWidth() * game.scaleFactor;
        btnHeight = playSpr.getHeight() * game.scaleFactor;
        playBtn = new ImageButton(new SpriteDrawable(playSpr));
        playBtn.getImage().setScale(game.scaleFactor);
        playBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                togglePause();
            }
        });
        buttonsTable.add(playBtn).pad(5 * game.scaleFactor).width(btnWidth).height(btnHeight);

        // create the other buttons...
        // Pause button
        Sprite pauseSpr = uiAtlas.createSprite("control_pause");
        pauseBtn = new ImageButton(new SpriteDrawable(pauseSpr));
        pauseBtn.getImage().setScale(game.scaleFactor);
        pauseBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                togglePause();
            }
        });

        // mute button
        Sprite muteSpr = uiAtlas.createSprite("sound_mute");
        Sprite unMuteSpr = uiAtlas.createSprite("sound");
        muteBtn = new ImageButton(new SpriteDrawable(muteSpr), null, new SpriteDrawable(unMuteSpr));
        muteBtn.getImage().setScale(game.scaleFactor);
        muteBtn.setChecked(game.preferences.getBoolean("mute"));
        muteBtn.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {

                toggleMute();
            }
        });
        buttonsTable.add(muteBtn).pad(5 * game.scaleFactor).width(btnWidth).height(btnHeight);

        // create the speed designation table
        Table table = new Table(skin);
        table.setWidth(stage.getWidth());
        table.setHeight(stage.getHeight());

        levelNameLabel = createButtonLabel(level != null ? level.name : "");
        levelNameLabel.setColor(.25f, .25f, .25f, 1f);
        levelNameLabel.setAlignment(Align.center);
        table.add(levelNameLabel).colspan(3).expandX().fillX().top().padTop(10 * game.scaleFactor);
        
        table.row();
        
        table.add(new Label("", skin)).minWidth(100 * game.scaleFactor).padLeft(10 * game.scaleFactor).top();

        msgQueue = new GameMessageQueue(tapToStartMsg, skin);
        table.add(msgQueue.getMsgLabel()).expandX().fillX();
        
        // Create a container for score label and value
        Table scoreTable = new Table(skin);
        Label scoreLabelText = new Label("Total Score", skin);
        scoreLabelText.setColor(.25f, .25f, .25f, 1f);
        scoreLabelText.setAlignment(Align.center);
        scoreTable.add(scoreLabelText).center();
        scoreTable.row();
        
        pointsLabel = createButtonLabel(points + "");
        pointsLabel.setColor(.25f, .25f, .25f, 1f);  // Back to black
        pointsLabel.setAlignment(Align.center);
        scoreTable.add(pointsLabel).center();
        
        table.add(scoreTable).minWidth(100 * game.scaleFactor).padRight(10 * game.scaleFactor).top();
        
        table.row();
        
        // Add bailed label and restart button container to the right side
        table.add(new Label("", skin)).colspan(2);  // Empty cells for alignment
        
        Table bailedTable = new Table(skin);
        bailedLabel = createTitleLabel("");  // Use title font for bigger text
        bailedLabel.setColor(1f, 0.2f, 0.2f, 1f);  // Red color
        bailedLabel.setAlignment(Align.center);
        bailedTable.add(bailedLabel).center();
        bailedTable.row();
        
        // Create restart button (initially hidden)
        bailedRestartBtn = new TextButton("Restart", skin);
        bailedRestartBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                reset();
            }
        });
        bailedRestartBtn.setVisible(false);
        bailedTable.add(bailedRestartBtn).padTop(5 * game.scaleFactor).size(80 * game.scaleFactor, 30 * game.scaleFactor);
        
        table.add(bailedTable).minWidth(120 * game.scaleFactor).padRight(10 * game.scaleFactor).top();
        
        table.row();
        
        speedLabel = createButtonLabel("0" + speedDesignator);
        speedLabel.setColor(.25f, .25f, .25f, 1f);
        int controlAreaType = game.preferences.getInteger("controlAreaType", game.levelSettings.defaultControlAreaType);
        if(controlAreaType == ControlArea.RIGHTBOTTOM) {

            table.add(speedLabel).expandY().bottom().left().padLeft(10).colspan(3);
        } else {
        
            table.add(speedLabel).expandY().bottom().right().padRight(10).colspan(3);
        }

        stage.addActor(table);
        stage.addActor(buttonsTable);

        if(game.isDebugMode()) {

            table.debug();
        }
    }

    private void toggleMute() {

        // toggle the state...
        boolean mute = !game.preferences.getBoolean("mute");
        game.preferences.putBoolean("mute", mute);
        game.preferences.flush();

        if(mute && skiingSoundId != null) {

            skiingSound.stop(skiingSoundId);
            skiingSoundId = null;
        }

        if(!mute && touchingTerrain && !isPaused) {

            skiingSoundId = skiingSound.loop(game.preferences.getFloat("fxVolume"));
        }

        if(buttonsTable.getRows() == 2 && buttonsTable.getColumns() == 4) {

            String muteStr = "Mute";
            if(mute) {

                muteStr = "Unmute";
            }
            buttonsTable.getCells().get(7).setActor(new Label(muteStr, getSkin()));
        }
    }

    private void simulate() {

        // game world updates...
        touchingTerrain = false;
        world.step(dt, game.levelSettings.velocityIterations, game.levelSettings.positionIterations);
        skier.updateMotors(dt);
        if(trees != null && trees.length > 0) {

            for(TreeModel tree : trees) {

                tree.updateMotors(dt);
            }
        }

        // attempt to detect ski's popping off
        if(!skier.leftSkiPopped() || !skier.rightSkiPopped()) {

            boolean skiPopped = skier.detectSkiPop(bindingsSound, game.preferences.getFloat("fxVolume"),
                    game.preferences.getBoolean("mute"));
            if(!skierBailed && skiPopped) {

                skierBailed = true;
                skier.ragDoll();

                if(!levelFinished) {

                    bailedLabel.setText(bailedMsg);
                    bailedRestartBtn.setVisible(true);
                }
            }
        }
    }

    private void adjustSkiingSound() {

        // just landed...
        if(airborne && touchingTerrain) {

            if(!game.preferences.getBoolean("mute")) {

                if (skiingSoundId == null) {

                    skiingSoundId = skiingSound.loop(game.preferences.getFloat("fxVolume"));
                } else {

                    skiingSound.resume(skiingSoundId);
                }
            }
        } else if(!touchingTerrain) { // see if we just launched

            if(skiingSoundId != null) {

                skiingSound.pause(skiingSoundId);
            }
        }

        // adjust sound for speed
        if(skiingSoundId != null) {

            float velocity = skier.getUpperBody().getLinearVelocity().len();
            float volume = game.preferences.getFloat("fxVolume");
            if (velocity < 10f) {

                volume *= (velocity / 10f);
            }
            skiingSound.setVolume(skiingSoundId, volume * .5f);
        }
    }

    private void gameLogic() {

        checkFinished();

        // show speed - update it twice per second
        if(System.currentTimeMillis() - startTime > 500) {

            float speed = Math.round(skier.getVelocity() * speedConversion);
            speedLabel.setText((int)speed + speedDesignator);
            startTime = System.currentTimeMillis();
        }

        msgQueue.updateMessages(System.currentTimeMillis());

        // skiing sound
        adjustSkiingSound();

        // don't record jumps if the skier has bailed or the level is done
        if(skierBailed || levelFinished) {

            lastJump = null;
        }

        // figure out if we just launched or landed (on skis)
        if(airborne) {

            // update our flip detection rotation numbers every 100ms
            if(System.currentTimeMillis() - rotationTimer > 100) {

                rotationCounter += skier.getSkiAngle() - lastRotationAngle;
                lastRotationAngle = skier.getSkiAngle();
                rotationTimer = System.currentTimeMillis();
            }

            // see if we just landed
            if(touchingTerrain) {

                // we only count jumps that are at least longer than half a second
                if(System.currentTimeMillis() - airTimeStart > 500) {

                    // record this jump's data...just because they touched the terrain it doesn't necessarily mean
                    //  they landed...we will check if they have bailed within a set amount of time before counting it
                    lastJump = new BipedJump(airTimeStart,
                            System.currentTimeMillis(),
                            rotationCounter,
                            launchPos,
                            skier.getUpperBody().getWorldCenter(),
                            isMetric);
                    
                    // Only show jump message if points were earned
                    if (lastJump.getPoints() > 0) {
                        msgQueue.addPersistentMessage(lastJump.getJumpMsg());
                    }
                }

                airborne = false;
            }
        } else {

            // see if we just launched
            if(!touchingTerrain) {

                airTimeStart = System.currentTimeMillis();
                airborne = true;
                lastRotationAngle = skier.getSkiAngle();
                rotationCounter = 0;
                rotationTimer = airTimeStart;
                launchPos.x = skier.getUpperBody().getWorldCenter().x;
                launchPos.y = skier.getUpperBody().getWorldCenter().y;
            }
        }

        // as long as they haven't bailed 1 second after landing, record the points
        if(lastJump != null && System.currentTimeMillis() - lastJump.getLandTime() > 1000) {

            points += lastJump.getPoints();
            pointsLabel.setText(points + "");

            // update stats
            stats.addAirTimeSeconds(lastJump.getAirTime());
            stats.addBackFlips(lastJump.getNumBackFlips());
            stats.addFrontFlips(lastJump.getNumFrontFlips());
            stats.addAirDistanceMetres(lastJump.getDistanceMetres());
            
            // Update longest jump stats
            stats.updateLongestJumpAirTime(lastJump.getAirTime());
            stats.updateLongestJumpDistance(lastJump.getDistanceMetres());

            // clear out this jump so we don't record it again
            lastJump = null;
        }
    }

    @Override
    public void render(float delta) {

        //fpsLogger.log();
        if(!isPaused) {

            if(delta > maxDt) {

                Gdx.app.error(LOG, "Delta time was greater than maxDt!");
                delta = maxDt;
            }

            accumulator += delta;
            while(accumulator >= dt) {

                simulate();
                accumulator -= dt;
            }

            // do the game logic less than the simulation
            gameLogic();
        }

        // draw sky
        Gdx.gl.glClearColor(0.529f, 0.808f, 0.98f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // update viewport to center on skier, and adjust zoom for speed
        adjustViewport();

        drawTrees();

        // draw the terrain as a bunch of filled rectangles and triangles
        drawTerrain();

        // draw biped
        if(game.isDebugMode()) {

            skier.drawBipedDebug(shapeRenderer, game.drawSettings);
        } else {

            skier.drawBipedSprites(camera, game.spriteBatch);
        }

        // NOTE: you must draw shape renderer shit outside of sprite batches!!!
        controlArea.drawShapes(hudCamera, shapeRenderer);

        // draw HUD sprites
        controlArea.drawSprites(hudCamera, game.spriteBatch);

        // draw hud
        stage.act(delta);
        stage.draw();
    }

    // contact listener functions
    @Override
    public void beginContact(Contact contact) {
    }

    @Override
    public void endContact(Contact contact) {
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

        checkContact(contact.getFixtureA(), contact, impulse);
        checkContact(contact.getFixtureB(), contact, impulse);
    }

    private void checkContact(Fixture fixture, Contact contact, ContactImpulse impulse) {

        BodyType bodyType = (BodyType)fixture.getUserData();
        if(bodyType != null) {

            if(bodyType.getBodyType() == BodyType.BodyTypeBiped || bodyType.getBodyType() == BodyType.BodyTypeSki) {

                touchingTerrain = true;
            }

            if(bodyType.getBodyType() == BodyType.BodyTypeBiped && bodyType.getId() == SimpleBiped.BIPED_CONTACT_ID) {

                checkBipedBails(impulse);
            }

            // check the force pointing away from the ski boot to see if we should pop the ski
            if(!skier.leftSkiPopped() || !skier.rightSkiPopped()) {

                if(bodyType.getBodyType() == BodyType.BodyTypeSki) {

                    Vector2 normal = contact.getWorldManifold().getNormal();
                    normal.add(fixture.getBody().getWorldCenter());

                    Vector2 localNormal = fixture.getBody().getLocalPoint(normal);
                    float force = getImpulseForce(impulse) * localNormal.y;
                    if(force < -game.skiSettings.normalSkiPopForce) {

                        skier.popSki(fixture.getBody());
                    }
                }
            }
        }
    }

    private void destroySkier() {

        Gdx.app.debug(LOG, "destroySkier()");

        if(skier != null) {

            try {

                skier.destroy();
                skier = null;
            } catch(Throwable ex) {

                ex.printStackTrace();
                Gdx.app.error(LOG, "Failed to destroy skier.", ex);
            }
        }
    }

    private void destroyTrees() {

        if(trees != null) {

            for(TreeModel tree : trees) {

                try {

                    tree.destroy();
                } catch (Throwable ex) {

                    ex.printStackTrace();
                    Gdx.app.error(LOG, "Failed to destroy a tree.", ex);
                }
            }

            trees = null;
        }
    }

    private void checkBipedBails(ContactImpulse impulse) {

        Gdx.app.debug(LOG, "checkBipedBails()");

        if(getImpulseForce(impulse) > game.levelSettings.bailForce)  {

            skierBailed = true;
            skier.ragDoll();

            if(!game.preferences.getBoolean("mute")) {

                if (System.currentTimeMillis() - lastBailSound > 1000) {

                    ouchSounds.get(random.nextInt(ouchSounds.size())).play(game.preferences.getFloat("fxVolume"));
                    lastBailSound = System.currentTimeMillis();
                }
            }

            if(!levelFinished) {

                bailedLabel.setText(bailedMsg);
                bailedRestartBtn.setVisible(true);
            }
        }
    }

    private float getImpulseForce(ContactImpulse impulse) {

        int count = impulse.getNormalImpulses().length;

        float maxImpulse = 0.0f;
        for(int ii = 0; ii < count; ++ii) {

            maxImpulse = Math.max(maxImpulse, impulse.getNormalImpulses()[ii]);
        }

        return maxImpulse;
    }

    private void adjustViewport() {

        if(world == null || skier.getUpperBody() == null) {

            return;
        }

        // adjust the zoom based on speed
        float speed = skier.getVelocity();
        float scale;
        if(speed <= 0) {

            scale = game.drawSettings.minScale;
        } else if(speed >= game.drawSettings.maxSpeedForMaxScale) {

            scale = game.drawSettings.maxScale;
        } else {

            scale = game.drawSettings.minScale + (game.drawSettings.maxScale - game.drawSettings.minScale) *
                speed * maxSpeedForMaxScaleInv;
        }

        if(extents == null) {

            extents = new Vector2();
        }

        extents.x = aspectRatio * game.drawSettings.extentsFactor;
        extents.y = game.drawSettings.extentsFactor;
        extents.scl(scale);

        // center viewport on the center body
        Vector2 lower = skier.getUpperBody().getWorldCenter().cpy().sub(extents);
        Vector2 upper = skier.getUpperBody().getWorldCenter().cpy().add(extents);

        // adjust skier's position on screen
        float xAdjust = 0.0f;
        int controlAreaType = game.preferences.getInteger("controlAreaType", game.levelSettings.defaultControlAreaType);
        if(controlAreaType == ControlArea.RIGHTBOTTOM) {

            xAdjust = game.drawSettings.viewportOffsetX * scale;

            // adjust for narrower screens (uses the desktop aspect ratio as it's yardstick)
            // width/height
            xAdjust *= aspectRatio / 1.6666666666666667;
        }
        float yAdjust = game.drawSettings.viewportOffsetY * scale;

        // set the camera to look directly at the skier
        Vector2 skierPos = skier.getUpperBody().getWorldCenter();
        camera.position.set(skierPos.x + xAdjust, skierPos.y - yAdjust, 10.0f);
        camera.zoom = (upper.x - lower.x) / (float)height;
        camera.update();

        // figure out where our camera is point in world space so we can draw appropriately
        Vector3 tr = new Vector3(Gdx.graphics.getWidth(), 0, 1);
        Vector3 bl = new Vector3(0, Gdx.graphics.getHeight(), 1);
        camera.unproject(tr);
        camera.unproject(bl);

        // update the viewport...
        viewport.x = bl.x;
        viewport.y = bl.y;
        viewport.width = Math.abs(tr.x - bl.x);
        viewport.height = Math.abs(tr.y - bl.y);

        shapeRenderer.setProjectionMatrix(camera.combined);
    }

    private void drawTerrain() {

        // only draw terrain that is within the viewport
        for(TerrainShape shape : terrainShapeList) {

            if(shape.isInViewport(viewport)) {

                shape.draw(shapeRenderer, viewport.y);
            }
        }
    }

    private void drawTrees() {

        if(trees != null) {

            for(TreeModel tree : trees) {

                if(tree.isInViewport(viewport)) {

                    tree.drawDebug(shapeRenderer, game.drawSettings);
                }
            }
        }
    }

    @Override
    public boolean keyUp(int keycode) {

        switch(keycode) {

            case Keys.SPACE: {

                togglePause();
                break;
            }

            case Keys.ENTER:
            case Keys.BACK: {

                if(isPaused) {

                    backToLastMenu();
                } else {

                    togglePause();
                    reset();
                }
                break;
            }

            default:
                break;
        }

        return super.keyUp(keycode);
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer) {

        onMouseTouchOrMove(x, y);
        return super.touchDragged(x, y, pointer);
    }

    @Override
    public boolean mouseMoved(int x, int y) {

        onMouseTouchOrMove(x, y);
        return super.mouseMoved(x, y);
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {

        // if the modal dialog is showing, pass the event on
        if(modalDlg != null && modalDlg.isVisible()) {

            return false;
        }

        // un-pause if the user touches the control area
        if(controlArea.contains(x, y)) {

            if(isPaused) {

                togglePause();
                return true;
            }
        }

        onMouseTouchOrMove(x, y);
        return super.touchDown(x, y, pointer, button);
    }

    private void onMouseTouchOrMove(int x, int y) {

        // if the modal dialog is showing, pass the event on
        if(isPaused || (modalDlg != null && modalDlg.isVisible())) {

            return;
        }

        // only process touches within the control area
        if(controlArea.contains(x, y)) {

            controlArea.calcRatios(x, y);
            skier.setCommandedAngles(controlArea.xRatio, controlArea.yRatio);
        }
    }

    private void togglePause() {

        buttonsTable.clear();
        if(isPaused) {

            if(skierBailed) {

                bailedLabel.setText(bailedMsg);
            } else if(levelFinished) {

                msgQueue.setStaticMessage(levelFinishedMsg);
            } else {

                msgQueue.clearMessages();
                bailedLabel.setText("");
                bailedRestartBtn.setVisible(false);
            }
            buttonsTable.add(pauseBtn).pad(5 * game.scaleFactor).width(btnWidth);

            if(!game.preferences.getBoolean("mute") && touchingTerrain) {

                skiingSoundId = skiingSound.loop(game.preferences.getFloat("fxVolume"));
            }

            // if the paused menu is showing, delete it
            if(pausedTable != null) {

                stage.getActors().removeValue(pausedTable, true);
                pausedTable = null;
            }
        } else {

            msgQueue.setStaticMessage(tapToStartMsg);
            buttonsTable.add(playBtn).pad(5 * game.scaleFactor).width(btnWidth).height(btnHeight);

            if (!levelFinished) {

                showPauseMenu();
            }

            // stop any looping sounds
            if(skiingSoundId != null) {

                skiingSound.stop(skiingSoundId);
                skiingSoundId = null;
            }
        }

        buttonsTable.add(muteBtn).pad(5 * game.scaleFactor).width(btnWidth).height(btnHeight);

        isPaused = !isPaused;
        
        // Set level start time when first unpausing
        if (!isPaused && levelStartTime == 0 && !levelFinished) {
            levelStartTime = System.currentTimeMillis();
        }
    }

    private void backToLastMenu() {

        if (levelList instanceof OnlineLevelList) {

            game.setScreen(new OnlineLevelSelectScreen(game));
        } else {

            LevelSelectScreen levelSelectScreen = new LevelSelectScreen(game);
            if(levelSelectScreen.loadLastLevelPack()) {

                game.setScreen(levelSelectScreen);
            } else {

                game.setScreen(new LevelPacksScreen(game));
            }
        }
    }

    private void checkFinished() {

        if(levelFinished || skierBailed || level.endX <= 0f) {

            return;
        }

        boolean finished = skier.getUpperBody().getWorldCenter().x >= level.endX;
        if(finished) {

            if(!game.preferences.getBoolean("mute")) {

                clappingSound.play(game.preferences.getFloat("fxVolume"));
            }

            levelFinished = true;
            msgQueue.setStaticMessage(levelFinishedMsg);
            bailedLabel.setText("");  // Clear bailed message when level is complete
            bailedRestartBtn.setVisible(false);

            // finalize our stats for this run
            stats.setPoints(points);
            
            // Calculate completion time
            if (levelStartTime > 0) {
                float completionTime = (System.currentTimeMillis() - levelStartTime) / 1000.0f;
                stats.setCompletionTimeSeconds(completionTime);
            }
            
            // Mark level as completed in progress tracker
            game.levelProgress.markLevelCompleted(level.getStatsId());

            // show the stats for this run
            showEndMenu();

            // merge the stats with any previous and save the results
            String levelId = level.getStatsId();
            if(game.levelStats.contains(levelId)) {

                LevelStats prevStats = json.fromJson(LevelStats.class, game.levelStats.getString(levelId));
                if(prevStats != null) {

                    // ensures we have the highests stats for this level...
                    stats.merge(prevStats);
                }
            }
            game.levelStats.putString(levelId, json.toJson(stats));
            game.levelStats.flush();
        }
    }

    private void showEndMenu() {

        Skin skin = super.getSkin();

        // create a table with buttons in the middle
        finishedTable = new Table(skin);
        finishedTable.setWidth(stage.getWidth());
        finishedTable.setHeight(stage.getHeight());

        // Add in the summary
        NinePatchDrawable patch = new NinePatchDrawable(uiAtlas.createPatch("background"));
        finishedTable.add(stats.getSummaryTable(isMetric, skin, patch)).colspan(2).padBottom(20);
        finishedTable.row();

        TextButton playAgainBtn = new TextButton("Play Again", skin);
        playAgainBtn.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {

                reset();
            }
        });

        finishedTable.add(playAgainBtn).spaceRight(20 * game.scaleFactor).uniform().size(200 * game.scaleFactor, 60 * game.scaleFactor);

        TextButton mainMenuBtn = new TextButton("Level Menu", skin);
        mainMenuBtn.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {

                if(skiingSoundId != null) {

                    skiingSound.stop(skiingSoundId);
                    skiingSoundId = null;
                }
                backToLastMenu();
            }
        });
        finishedTable.add(mainMenuBtn).spaceRight(20 * game.scaleFactor).uniform().size(200 * game.scaleFactor, 60 * game.scaleFactor);
        finishedTable.row();

        // we only allow ratings for online levels
        if (levelList instanceof OnlineLevelList) {

            TextButton rateBtn = new TextButton("Rate Level", skin);
            rateBtn.addListener(new ClickListener() {

                @Override
                public void clicked(InputEvent event, float x, float y) {

                    rateLevel();
                }
            });
            finishedTable.add(rateBtn).spaceRight(20 * game.scaleFactor).uniform().size(200 * game.scaleFactor, 60 * game.scaleFactor);
        }

        if (levelList != null && levelList.hasNextLevel(level)) {

            TextButton nextLevelBtn = new TextButton("Next Level", skin);
            nextLevelBtn.addListener(new ClickListener() {

                @Override
                public void clicked(InputEvent event, float x, float y) {

                    setLevel(levelList.getNextLevel(level));
                }
            });

            if (levelList instanceof OnlineLevelList) {

                finishedTable.add(nextLevelBtn).uniform().size(200 * game.scaleFactor, 60 * game.scaleFactor);
            } else {

                finishedTable.add(nextLevelBtn).uniform().size(200 * game.scaleFactor, 60 * game.scaleFactor).colspan(2).center();
            }
        }

        finishedTable.center();

        stage.addActor(finishedTable);
    }

    private void showPauseMenu() {

        Skin skin = super.getSkin();

        // create a table with buttons in the middle
        Table contentTable = new Table(skin);
        contentTable.setBackground(new NinePatchDrawable(uiAtlas.createPatch("background")));

        contentTable.add(createTitleLabel("Game Paused")).pad(10 * game.scaleFactor);
        contentTable.row();

        TextButton continueBtn = new TextButton("Continue", skin);
        continueBtn.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {

                togglePause();
            }
        });

        contentTable.add(continueBtn).pad(10 * game.scaleFactor).size(getButtonWidth(), getButtonHeight());
        contentTable.row();

        TextButton playAgainBtn = new TextButton("Restart", skin);
        playAgainBtn.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {

                reset();
            }
        });

        contentTable.add(playAgainBtn).pad(10 * game.scaleFactor).size(getButtonWidth(), getButtonHeight());
        contentTable.row();

        // we only allow ratings for online levels
        if (levelList instanceof OnlineLevelList) {

            TextButton rateBtn = new TextButton("Rate Level", skin);
            rateBtn.addListener(new ClickListener() {

                @Override
                public void clicked(InputEvent event, float x, float y) {

                    rateLevel();
                }
            });
            contentTable.add(rateBtn).pad(10 * game.scaleFactor).size(getButtonWidth(), getButtonHeight());
            contentTable.row();
        }

        TextButton mainMenuBtn = new TextButton("Level Menu", skin);
        mainMenuBtn.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {

                if(skiingSoundId != null) {

                    skiingSound.stop(skiingSoundId);
                    skiingSoundId = null;
                }
                backToLastMenu();
            }
        });
        contentTable.add(mainMenuBtn).pad(10 * game.scaleFactor).size(getButtonWidth(), getButtonHeight());
        contentTable.row();

        pausedTable = new Table(skin);
        pausedTable.setWidth(stage.getWidth());
        pausedTable.setHeight(stage.getHeight());
        pausedTable.add(contentTable).width(stage.getWidth() * 0.5f);
        pausedTable.row();

        pausedTable.center();

        stage.addActor(pausedTable);
    }

    private void rateLevel() {

        if(!LevelRepository.hasToken()) {

            modalDlg = new LoginDialog(getSkin(), uiAtlas, game.scaleFactor).show(stage);
            return;
        }

        modalDlg = new RateLevelDialog(getSkin(), level, uiAtlas, game.scaleFactor).show(stage);
    }

    @Override
    public void dispose() {

        super.dispose();
        if(world != null) {

            world.dispose();
        }
    }
}
