package com.deaddropgames.stuntmountain.sim;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.deaddropgames.stuntmountain.settings.BipedSettings;
import com.deaddropgames.stuntmountain.settings.DrawSettings;
import com.deaddropgames.stuntmountain.settings.SkiSettings;
import com.deaddropgames.stuntmountain.util.PIDController;
import com.deaddropgames.stuntmountain.util.SimpleRenderer;
import com.deaddropgames.stuntmountain.util.SpriteStruct;

public class SimpleBiped {

    final public static short BIPED_CONTACT_ID = 111; // to detect when the head gets hit

    private World world;
    private BipedSettings bipedSettings;

    // body parts and joints
    private Body lowerLegLeft, lowerLegRight;
    private Fixture leftSki, leftSkiTip1, leftSkiTip2;
    private Fixture rightSki, rightSkiTip1, rightSkiTip2;
    private Body upperLegLeft, upperLegRight;
    private RevoluteJoint kneeJointLeft, kneeJointRight;
    private Body torso;
    private RevoluteJoint hipJointLeft, hipJointRight;
    private Body head;
    private RevoluteJoint neckJoint;
    private Body upperArmLeft, upperArmRight;
    private RevoluteJoint shoulderJointLeft, shoulderJointRight;
    private Body lowerArmLeft, lowerArmRight;
    private RevoluteJoint elbowJointLeft, elbowJointRight;

    // motor commanded angles
    private float kneeAngle, hipAngle, shoulderAngle, elbowAngle;

    // ski specific stuff to simulate the skis popping off
    private SkiSettings skiSettings;
    private boolean leftSkiPopped, rightSkiPopped;
    private Body leftPoppedSki, rightPoppedSki;

    // size constants
    private final float lowerLegWidth = 0.12f;
    private final float lowerLegHeight = 0.5f;
    private final float upperLegWidth = 0.16f;
    private final float upperLegHeight = 0.5f;
    private final float lowerTorsoWidth = 0.2f;
    private final float lowerTorsoHeight = 0.3f;
    private final float upperTorsoWidth = 0.2f;
    private final float upperTorsoHeight = 0.3f;
    private final float upperArmWidth = 0.09f;
    private final float upperArmHeight = 0.3f;
    private final float lowerArmWidth = 0.09f;
    private final float lowerArmHeight = 0.3f;
    private final float headRadius = 0.15f;

    // sprite structs
    private SpriteStruct lowerLegLeftSpriteStruct;
    private SpriteStruct lowerLegRightSpriteStruct;
    private SpriteStruct upperLegLeftSpriteStruct;
    private SpriteStruct upperLegRightSpriteStruct;
    private SpriteStruct torsoSpriteStruct;
    private SpriteStruct upperArmLeftSpriteStruct;
    private SpriteStruct upperArmRightSpriteStruct;
    private SpriteStruct lowerArmLeftSpriteStruct;
    private SpriteStruct lowerArmRightSpriteStruct;
    private SpriteStruct headSpriteStruct;
    private SpriteStruct leftSkiSpriteStruct;
    private SpriteStruct rightSkiSpriteStruct;

    // sprites used when ski is popped
    private Sprite lowerLegSprite;

    // PID motor controllers
    private PIDController leftKneeController;
    private PIDController rightKneeController;
    private PIDController leftHipController;
    private PIDController rightHipController;
    // these PID controller's aren't really necessary for the arms...
    /*private PIDController leftShoulderController;
    private PIDController rightShoulderController;
    private PIDController leftElbowController;
    private PIDController rightElbowController;*/

    private boolean doPopLeftSki = false;
    private boolean doPopRightSki = false;

    public SimpleBiped(World world, final SkiSettings skiSettings, final BipedSettings bipedSettings, final TextureAtlas atlas) {

        this.world = world;
        this.skiSettings = skiSettings;
        this.bipedSettings = bipedSettings;
        create(new Vector2(bipedSettings.startX, bipedSettings.startY));

        // load textures and related info for rendering sprites
        lowerLegLeftSpriteStruct = new SpriteStruct(lowerLegLeft, atlas.createSprite("legandski"),
                new Rectangle(-skiSettings.length * 0.5f, -lowerLegHeight * 0.5f,
                        skiSettings.length, lowerLegHeight + lowerLegWidth + 0.1f));
        lowerLegLeftSpriteStruct.sprite.setOrigin(skiSettings.length * 0.5f,
                lowerLegHeight * 0.5f);

        lowerLegRightSpriteStruct = new SpriteStruct(lowerLegRight, atlas.createSprite("legandski"),
                new Rectangle(-skiSettings.length * 0.5f, -lowerLegHeight * 0.5f,
                        skiSettings.length, lowerLegHeight + lowerLegWidth + 0.1f));
        lowerLegRightSpriteStruct.sprite.setOrigin(skiSettings.length * 0.5f,
                lowerLegHeight * 0.5f);

        float width = upperLegWidth * 1.75f;
        upperLegLeftSpriteStruct = new SpriteStruct(upperLegLeft, atlas.createSprite("upperleg"),
                new Rectangle(-width * 0.5f, -upperLegHeight * 0.5f, width, upperLegHeight + upperLegWidth));
        upperLegLeftSpriteStruct.sprite.setOrigin(width * 0.5f, upperLegHeight * 0.5f);

        upperLegRightSpriteStruct = new SpriteStruct(upperLegRight, atlas.createSprite("upperleg"),
                new Rectangle(-width * 0.5f, -upperLegHeight * 0.5f, width, upperLegHeight + upperLegWidth));
        upperLegRightSpriteStruct.sprite.setOrigin(width * 0.5f, upperLegHeight * 0.5f);

        width = upperTorsoWidth * 1.75f;
        torsoSpriteStruct = new SpriteStruct(torso, atlas.createSprite("body"),
                new Rectangle(-width * 0.5f, -(lowerTorsoHeight + upperTorsoHeight + 0.2f) * 0.5f,
                        width, lowerTorsoHeight + upperTorsoHeight + 0.1f));
        torsoSpriteStruct.sprite.setOrigin(width * 0.5f, (lowerTorsoHeight + upperTorsoHeight + 0.2f) * 0.5f);

        width = upperArmWidth * 1.75f;
        upperArmLeftSpriteStruct = new SpriteStruct(upperArmLeft, atlas.createSprite("upperarm"),
                new Rectangle(-width * 0.5f, -upperArmHeight * 0.5f, width, upperArmHeight + upperArmWidth));
        upperArmLeftSpriteStruct.sprite.setOrigin(width * 0.5f, upperArmHeight * 0.5f);

        upperArmRightSpriteStruct = new SpriteStruct(upperArmRight, atlas.createSprite("upperarm"),
                new Rectangle(-width * 0.5f, -upperArmHeight * 0.5f, width, upperArmHeight + upperArmWidth));
        upperArmRightSpriteStruct.sprite.setOrigin(width * 0.5f, upperArmHeight * 0.5f);


        width = lowerArmWidth * 1.75f;
        lowerArmLeftSpriteStruct = new SpriteStruct(lowerArmLeft, atlas.createSprite("lowerarm"),
                new Rectangle(-width * 0.5f, -lowerArmHeight * 0.5f, width, lowerArmHeight + lowerArmWidth));
        lowerArmLeftSpriteStruct.sprite.setOrigin(width * 0.5f, lowerArmHeight * 0.5f);

        lowerArmRightSpriteStruct = new SpriteStruct(lowerArmRight, atlas.createSprite("lowerarm"),
                new Rectangle(-width * 0.5f, -lowerArmHeight * 0.5f, width, lowerArmHeight + lowerArmWidth));
        lowerArmRightSpriteStruct.sprite.setOrigin(width * 0.5f, lowerArmHeight * 0.5f);

        headSpriteStruct = new SpriteStruct(head, atlas.createSprite("head"),
                new Rectangle(-headRadius, -headRadius, headRadius*2.0f, headRadius*2.0f));
        headSpriteStruct.sprite.setOrigin(headRadius, headRadius);

        // need to create a new struct for popped ski's and lower legs
        float skiHeight = 0.15f;
        leftSkiSpriteStruct = new SpriteStruct(leftPoppedSki, atlas.createSprite("ski"),
                new Rectangle(-skiSettings.length * 0.5f, -skiHeight * 0.5f, skiSettings.length, skiHeight));
        leftSkiSpriteStruct.sprite.setOrigin(skiSettings.length * 0.5f, skiHeight * 0.5f);

        rightSkiSpriteStruct = new SpriteStruct(rightPoppedSki, atlas.createSprite("ski"),
                new Rectangle(-skiSettings.length * 0.5f, -skiHeight * 0.5f, skiSettings.length, skiHeight));
        rightSkiSpriteStruct.sprite.setOrigin(skiSettings.length * 0.5f, skiHeight * 0.5f);

        lowerLegSprite = atlas.createSprite("lowerleg");
        lowerLegSprite.setOrigin(skiSettings.length * 0.5f, lowerLegHeight * 0.5f);

        // PID controllers
        leftKneeController = new PIDController(bipedSettings.kneeMultiplierP,
                0.5f, 2.0f);
        rightKneeController = new PIDController(bipedSettings.kneeMultiplierP,
                0.5f, 2.0f);

        leftHipController = new PIDController(bipedSettings.hipMultiplierP,
                bipedSettings.hipMultiplierI,
                bipedSettings.hipMultiplierD);
        rightHipController = new PIDController(bipedSettings.hipMultiplierP,
                bipedSettings.hipMultiplierI,
                bipedSettings.hipMultiplierD);

        /*leftShoulderController = new PIDController(bipedSettings.shoulderMultiplier, 0.0f, 0.0f);
        rightShoulderController = new PIDController(bipedSettings.shoulderMultiplier, 0.0f, 0.0f);

        leftElbowController = new PIDController(bipedSettings.elbowMultiplier, 0.0f, 0.0f);
        rightElbowController = new PIDController(bipedSettings.elbowMultiplier, 0.0f, 0.0f);*/
    }

    private void create(final Vector2 position) {

        kneeAngle = 0.0f;
        hipAngle = 0.0f;
        shoulderAngle = 0.0f;
        elbowAngle = 0.0f;

        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.DynamicBody;
        bd.position.set(0.0f, 0.0f);
        //bd.bullet = true;

        FixtureDef bodyFixtureDef = new FixtureDef();
        bodyFixtureDef.filter.groupIndex = bipedSettings.groupIndex;
        bodyFixtureDef.density = bipedSettings.density;
        bodyFixtureDef.restitution = bipedSettings.restitution;
        bodyFixtureDef.friction = bipedSettings.friction;

        bd.position.add(position);

        // lower leg left
        float yOffset = lowerLegHeight * 0.5f;
        PolygonShape lowerLegShape = new PolygonShape();
        lowerLegShape.setAsBox(lowerLegWidth * 0.5f, lowerLegHeight * 0.5f, new Vector2(0.0f, yOffset), 0.0f);

        lowerLegLeft = world.createBody(bd);

        bodyFixtureDef.shape = lowerLegShape;
        lowerLegLeft.createFixture(bodyFixtureDef).setUserData(new BodyType(BodyType.BodyTypeBiped));

        PolygonShape footShape = new PolygonShape();
        Vector2[] vertices = new Vector2[3];
        vertices[0] = new Vector2(0.06f, 0.0f);
        vertices[1] = new Vector2(0.24f, 0.0f);
        vertices[2] = new Vector2(0.06f, 0.2f);
        footShape.set(vertices);

        bodyFixtureDef.shape = footShape;
        lowerLegLeft.createFixture(bodyFixtureDef).setUserData(new BodyType(BodyType.BodyTypeBiped));

        // attach left ski
        FixtureDef skiDef = new FixtureDef();
        skiDef.filter.groupIndex = bipedSettings.groupIndex; // keep same index as biped for now...only change it if it pops off
        skiDef.density = skiSettings.density;
        skiDef.restitution = skiSettings.restitution;

        float halfSkiLength = skiSettings.length / 2.0f;

        PolygonShape skiShape = new PolygonShape();
        skiShape.setAsBox(halfSkiLength, skiSettings.thickness);
        skiDef.shape = skiShape;
        leftSki = lowerLegLeft.createFixture(skiDef);
        leftSki.setUserData(new BodyType(BodyType.BodyTypeSki));

        CircleShape tipShape = new CircleShape();
        tipShape.setRadius(skiSettings.height);
        tipShape.setPosition(new Vector2(halfSkiLength, 0.0f));
        skiDef.shape = tipShape;
        leftSkiTip1 = lowerLegLeft.createFixture(skiDef);
        leftSkiTip1.setUserData(new BodyType(BodyType.BodyTypeSki));
        tipShape.setPosition(new Vector2(-halfSkiLength, 0.0f));
        leftSkiTip2 = lowerLegLeft.createFixture(skiDef);
        leftSkiTip2.setUserData(new BodyType(BodyType.BodyTypeSki));

        // lower leg right
        lowerLegRight = world.createBody(bd);

        bodyFixtureDef.shape = lowerLegShape;
        lowerLegRight.createFixture(bodyFixtureDef).setUserData(new BodyType(BodyType.BodyTypeBiped));
        bodyFixtureDef.shape = footShape;
        lowerLegRight.createFixture(bodyFixtureDef).setUserData(new BodyType(BodyType.BodyTypeBiped));

        // attach right ski
        skiDef.shape = skiShape;
        rightSki = lowerLegRight.createFixture(skiDef);
        rightSki.setUserData(new BodyType(BodyType.BodyTypeSki));
        skiDef.shape = tipShape;
        rightSkiTip1 = lowerLegRight.createFixture(skiDef);
        rightSkiTip1.setUserData(new BodyType(BodyType.BodyTypeSki));
        tipShape.setPosition(new Vector2(halfSkiLength, 0.0f));
        rightSkiTip2 = lowerLegRight.createFixture(skiDef);
        rightSkiTip2.setUserData(new BodyType(BodyType.BodyTypeSki));

        // upper leg left
        yOffset += upperLegHeight;
        PolygonShape upperLegShape = new PolygonShape();
        upperLegShape.setAsBox(upperLegWidth * 0.5f, upperLegHeight * 0.5f, new Vector2(0.0f, yOffset), 0.0f);

        upperLegLeft = world.createBody(bd);
        bodyFixtureDef.shape = upperLegShape;
        upperLegLeft.createFixture(bodyFixtureDef).setUserData(new BodyType(BodyType.BodyTypeBiped));

        // upper leg right
        upperLegRight = world.createBody(bd);
        upperLegRight.createFixture(bodyFixtureDef).setUserData(new BodyType(BodyType.BodyTypeBiped));

        // knee joint
        RevoluteJointDef kneeDef = new RevoluteJointDef();
        Vector2 vec = new Vector2(0.0f, lowerLegHeight);
        vec.add(position);
        kneeDef.initialize(lowerLegLeft, upperLegLeft, vec);
        kneeDef.enableLimit = true;
        kneeDef.enableMotor = true;
        kneeDef.lowerAngle = 0.0f;
        kneeDef.upperAngle = MathUtils.PI * 3.0f / 4.0f; // 135
        kneeDef.maxMotorTorque = bipedSettings.maxMotorLimit;
        kneeJointLeft = (RevoluteJoint)world.createJoint(kneeDef);

        kneeDef.initialize(lowerLegRight, upperLegRight, vec);
        kneeDef.enableLimit = true;
        kneeDef.enableMotor = true;
        kneeDef.lowerAngle = 0.0f;
        kneeDef.upperAngle = MathUtils.PI * 3.0f / 4.0f; // 135
        kneeDef.maxMotorTorque = bipedSettings.maxMotorLimit;
        kneeJointRight = (RevoluteJoint)world.createJoint(kneeDef);

        // torso
        yOffset += lowerTorsoHeight + 0.1f; // NOTE: can't figure out why this works...but its needed!
        PolygonShape lowerBodyShape = new PolygonShape();
        lowerBodyShape.setAsBox(lowerTorsoWidth * 0.5f, lowerTorsoHeight * 0.5f, new Vector2(0.0f, yOffset), 0.0f);

        yOffset += upperTorsoHeight;
        PolygonShape upperBodyShape = new PolygonShape();
        upperBodyShape.setAsBox(upperTorsoWidth * 0.5f, upperTorsoHeight * 0.5f, new Vector2(0.0f, yOffset), 0.0f);

        torso = world.createBody(bd);
        bodyFixtureDef.shape = lowerBodyShape;
        torso.createFixture(bodyFixtureDef).setUserData(new BodyType(BodyType.BodyTypeBiped, BIPED_CONTACT_ID));
        bodyFixtureDef.shape = upperBodyShape;
        torso.createFixture(bodyFixtureDef).setUserData(new BodyType(BodyType.BodyTypeBiped, BIPED_CONTACT_ID));

        // hip joint
        RevoluteJointDef hipDef = new RevoluteJointDef();
        vec = new Vector2(0.0f, lowerLegHeight + upperLegHeight);
        vec.add(position);
        hipDef.initialize(upperLegLeft, torso, vec);
        hipDef.enableLimit = true;
        hipDef.enableMotor = true;
        hipDef.lowerAngle = -MathUtils.PI * 3.0f / 4.0f; // -135
        hipDef.upperAngle = MathUtils.PI * 0.5f; // 90
        hipDef.maxMotorTorque = bipedSettings.maxMotorLimit;
        hipJointLeft = (RevoluteJoint)world.createJoint(hipDef);

        hipDef.initialize(upperLegRight, torso, vec);
        hipDef.enableLimit = true;
        hipDef.enableMotor = true;
        hipDef.lowerAngle = -MathUtils.PI * 3.0f / 4.0f; // -135
        hipDef.upperAngle = MathUtils.PI * 0.5f; // 90
        hipDef.maxMotorTorque = bipedSettings.maxMotorLimit;
        hipJointRight = (RevoluteJoint)world.createJoint(hipDef);

        // head
        CircleShape headshape = new CircleShape();
        headshape.setRadius(headRadius);
        vec = new Vector2(0.0f, lowerLegHeight + upperLegHeight + lowerTorsoHeight + upperTorsoHeight + headshape.getRadius());
        headshape.setPosition(vec);
        head = world.createBody(bd);
        bodyFixtureDef.shape = headshape;
        head.createFixture(bodyFixtureDef).setUserData(new BodyType(BodyType.BodyTypeBiped, BIPED_CONTACT_ID));

        // neck joint
        RevoluteJointDef neckDef = new RevoluteJointDef();
        neckDef.initialize(torso, head, new Vector2(0.0f, 1.6f).add(position));
        neckDef.enableLimit = true;
        neckDef.enableMotor = true;
        neckDef.lowerAngle = -MathUtils.PI * 1.0f / 4.0f; // -45
        neckDef.upperAngle = MathUtils.PI * 1.0f / 4.0f; // 45
        neckDef.maxMotorTorque = bipedSettings.maxMotorLimit;
        neckJoint = (RevoluteJoint)world.createJoint(neckDef);

        // upper arm left
        yOffset -= 0.1f;
        PolygonShape upperArmShape = new PolygonShape();
        upperArmShape.setAsBox(upperArmWidth * 0.5f, upperArmHeight * 0.5f, new Vector2(0.0f, yOffset), 0.0f);

        upperArmLeft = world.createBody(bd);
        bodyFixtureDef.shape = upperArmShape;
        upperArmLeft.createFixture(bodyFixtureDef).setUserData(new BodyType(BodyType.BodyTypeBiped));
    
        // upper arm right
        upperArmRight = world.createBody(bd);
        upperArmRight.createFixture(bodyFixtureDef).setUserData(new BodyType(BodyType.BodyTypeBiped));

        // shoulder joint
        RevoluteJointDef shoulderDef = new RevoluteJointDef();
        vec = new Vector2(0.0f, lowerLegHeight + upperLegHeight + lowerTorsoHeight + upperTorsoHeight - 0.1f);
        vec.add(position);
        shoulderDef.initialize(torso, upperArmLeft, vec);
        shoulderDef.enableLimit = true;
        shoulderDef.enableMotor = true;
        shoulderDef.lowerAngle = -MathUtils.PI * 1.0f / 4.0f; // -45
        shoulderDef.upperAngle = MathUtils.PI; // 180
        shoulderDef.maxMotorTorque = bipedSettings.maxMotorLimit;
        shoulderJointLeft = (RevoluteJoint)world.createJoint(shoulderDef);

        shoulderDef.initialize(torso, upperArmRight, vec);
        shoulderDef.enableLimit = true;
        shoulderDef.enableMotor = true;
        shoulderDef.lowerAngle = -MathUtils.PI * 1.0f / 4.0f; // -45
        shoulderDef.upperAngle = MathUtils.PI; // 180
        shoulderDef.maxMotorTorque = bipedSettings.maxMotorLimit;
        shoulderJointRight = (RevoluteJoint)world.createJoint(shoulderDef);

        // lower arm left
        yOffset -= upperArmHeight;
        PolygonShape lowerArmShape = new PolygonShape();
        lowerArmShape.setAsBox(lowerArmWidth * 0.5f, lowerArmHeight * 0.5f, new Vector2(0.0f, yOffset), 0.0f);

        // lower arm left
        lowerArmLeft = world.createBody(bd);
        bodyFixtureDef.shape = lowerArmShape;
        lowerArmLeft.createFixture(bodyFixtureDef).setUserData(new BodyType(BodyType.BodyTypeBiped));

        // lower arm right
        lowerArmRight = world.createBody(bd);
        lowerArmRight.createFixture(bodyFixtureDef).setUserData(new BodyType(BodyType.BodyTypeBiped));

        // elbow joints
        RevoluteJointDef elbowDef = new RevoluteJointDef();
        vec = new Vector2(0.0f, lowerLegHeight + upperLegHeight + lowerTorsoHeight + upperTorsoHeight - 0.1f - upperArmHeight);
        vec.add(position);
        elbowDef.initialize(upperArmLeft, lowerArmLeft, vec);
        elbowDef.enableLimit = true;
        elbowDef.enableMotor = true;
        elbowDef.lowerAngle = 0.0f;
        elbowDef.upperAngle = MathUtils.PI * 3.0f / 4.0f; // 135
        elbowDef.maxMotorTorque = bipedSettings.maxMotorLimit;
        elbowJointLeft = (RevoluteJoint)world.createJoint(elbowDef);

        elbowDef.initialize(upperArmRight, lowerArmRight, vec);
        elbowDef.enableLimit = true;
        elbowDef.enableMotor = true;
        elbowDef.lowerAngle = 0.0f;
        elbowDef.upperAngle = MathUtils.PI * 3.0f / 4.0f; // 135
        elbowDef.maxMotorTorque = bipedSettings.maxMotorLimit;
        elbowJointRight = (RevoluteJoint)world.createJoint(elbowDef);

        // initial command angles
        kneeAngle = 0.0f;
        hipAngle = 0.0f;
        shoulderAngle = 0.0f;
        elbowAngle = 0.0f;

        leftSkiPopped = false;
        rightSkiPopped = false;

        leftPoppedSki = null;
        rightPoppedSki = null;
    }

    public void destroy() throws Throwable {

        if(world == null) {

            return;
        }

        world.destroyBody(lowerLegLeft);
        lowerLegLeft = null;
        world.destroyBody(lowerLegRight);
        lowerLegRight = null;
        world.destroyBody(upperLegLeft);
        upperLegLeft = null;
        world.destroyBody(upperLegRight);
        upperLegRight = null;
        world.destroyBody(torso);
        torso = null;
        world.destroyBody(head);
        head = null;
        world.destroyBody(upperArmLeft);
        upperArmLeft = null;
        world.destroyBody(upperArmRight);
        upperArmRight = null;
        world.destroyBody(lowerArmLeft);
        lowerArmLeft = null;
        world.destroyBody(lowerArmRight);
        lowerArmRight = null;

        if(leftPoppedSki != null) {

            world.destroyBody(leftPoppedSki);
            leftPoppedSki = null;
        }

        if(rightPoppedSki != null) {

            world.destroyBody(rightPoppedSki);
            rightPoppedSki = null;
        }
    }

    // depending where on the screen the touch or mouse is, set joint commanded angles
    public void setCommandedAngles(float x, float y) {

        // in x and y:
        // -1.0f <= vector <= 1.0f
        float yFactor = (1.0f - y) / 2.0f;
        float xFactor = (1.0f - x) / 2.0f;

        kneeAngle = yFactor * kneeJointLeft.getUpperLimit();
        leftKneeController.setSetPoint(kneeAngle);
        rightKneeController.setSetPoint(kneeAngle);

        float hipLower = hipJointLeft.getLowerLimit();
        if(y > 0.0f)
        {
            hipLower = -MathUtils.PI / 2.0f;
        }

        hipAngle = (hipLower - (hipLower - hipJointLeft.getUpperLimit()) * xFactor) - kneeAngle;
        leftHipController.setSetPoint(hipAngle);
        rightHipController.setSetPoint(hipAngle);

        //shoulderAngle = MathUtils.PI - yFactor * shoulderJointLeft.getUpperLimit();
        shoulderAngle = -3.927f * yFactor + MathUtils.PI;
        /*leftShoulderController.setSetPoint(shoulderAngle);
        rightShoulderController.setSetPoint(shoulderAngle);*/

        elbowAngle = yFactor * (MathUtils.PI - elbowJointLeft.getLowerLimit());
        /*leftElbowController.setSetPoint(elbowAngle);
        rightElbowController.setSetPoint(elbowAngle);*/
    }

    // update the motor speeds - uses the commanded angles
    public void updateMotors(float delta) {

        kneeJointLeft.setMotorSpeed(leftKneeController.update(kneeJointLeft.getJointAngle(), delta));
        kneeJointRight.setMotorSpeed(rightKneeController.update(kneeJointRight.getJointAngle(), delta));

        hipJointLeft.setMotorSpeed(leftHipController.update(hipJointLeft.getJointAngle(), delta));
        hipJointRight.setMotorSpeed(rightHipController.update(hipJointRight.getJointAngle(), delta));

        shoulderJointLeft.setMotorSpeed((shoulderAngle - shoulderJointLeft.getJointAngle()) * bipedSettings.shoulderMultiplier);
        shoulderJointRight.setMotorSpeed((shoulderAngle - shoulderJointRight.getJointAngle()) * bipedSettings.shoulderMultiplier);

        /*shoulderJointLeft.setMotorSpeed(leftShoulderController.update(shoulderJointLeft.getJointAngle()));
        shoulderJointRight.setMotorSpeed(rightShoulderController.update(shoulderJointRight.getJointAngle()));*/

        elbowJointLeft.setMotorSpeed((elbowAngle - elbowJointLeft.getJointAngle()) * bipedSettings.elbowMultiplier);
        elbowJointRight.setMotorSpeed((elbowAngle - elbowJointRight.getJointAngle()) * bipedSettings.elbowMultiplier);

        /*elbowJointLeft.setMotorSpeed(leftElbowController.update(elbowJointLeft.getJointAngle()));
        elbowJointRight.setMotorSpeed(rightElbowController.update(elbowJointRight.getJointAngle()));*/
    }

    // get the biped's mass
    public float getMass() {

        return lowerLegLeft.getMass() * 2.0f +
            upperLegLeft.getMass() * 2.0f +
            torso.getMass() +
            head.getMass() +
            upperArmLeft.getMass() * 2.0f +
            lowerArmLeft.getMass() * 2.0f;
    }

    // should only be called when skier is first created, otherwise it won't work...
    public float getHeight() {

        Vector2 bottom = leftSki.getBody().getLocalCenter();
        Vector2 top = head.getLocalCenter();
        top.y += 0.15f;

        return (top.y - bottom.y);
    }

    // enable/disables the bipeds motors
    public void enableMotors(boolean enable) {

        kneeJointLeft.enableMotor(enable);
        kneeJointRight.enableMotor(enable);

        hipJointLeft.enableMotor(enable);
        hipJointRight.enableMotor(enable);

        neckJoint.enableMotor(enable);

        shoulderJointLeft.enableMotor(enable);
        shoulderJointRight.enableMotor(enable);

        elbowJointLeft.enableMotor(enable);
        elbowJointRight.enableMotor(enable);
    }

    public void ragDoll() {

        enableMotors(false);
    }

    public boolean detectSkiPop(Sound bindingsSound, float volume, boolean muted) {

        boolean retVal = false;
        if(!leftSkiPopped && (doPopLeftSki || getLeftKneeAngle() < skiSettings.kneeSkiPopAngle)) {

            popLeftSki();
            ragDoll();
            retVal = true;

            if(!muted) {

                bindingsSound.play(volume);
            }
        }

        if(!rightSkiPopped && (doPopRightSki || getRightKneeAngle() < skiSettings.kneeSkiPopAngle)) {

            popRightSki();
            ragDoll();
            retVal = true;

            if(!muted) {

                bindingsSound.play(volume);
            }
        }

        return retVal;
    }

    // pop the left ski
    public void popLeftSki() {

        if(leftSkiPopped) {

            return;
        }

        if(leftSki != null) {

            lowerLegLeft.destroyFixture(leftSki);
        }
        if(leftSkiTip1 != null) {

            lowerLegLeft.destroyFixture(leftSkiTip1);
        }
        if(leftSkiTip2 != null) {

            lowerLegLeft.destroyFixture(leftSkiTip2);
        }

        leftSki = null;
        leftSkiTip1 = null;
        leftSkiTip2 = null;

        leftSkiPopped = true;

        // re-create the ski...
        createSki(lowerLegLeft, lowerLegLeft.getLinearVelocity(), lowerLegLeft.getAngularVelocity(), true);
    }

    // pop the right ski
    public void popRightSki() {

        if(rightSkiPopped) {

            return;
        }

        if(rightSki != null) {

            lowerLegRight.destroyFixture(rightSki);
        }
        if(rightSkiTip1 != null) {

            lowerLegRight.destroyFixture(rightSkiTip1);
        }
        if(rightSkiTip2 != null) {

            lowerLegRight.destroyFixture(rightSkiTip2);
        }

        rightSki = null;
        rightSkiTip1 = null;
        rightSkiTip2 = null;

        rightSkiPopped = true;

        // re-create the ski...
        createSki(lowerLegRight, lowerLegRight.getLinearVelocity(), lowerLegRight.getAngularVelocity(), false);
    }

    /**
     * This function is meant to be called during contact and we cannot destroy fixtures until all calcs are done
     * @param body the body that triggered the ski pop
     */
    public void popSki(Body body) {

        if(body.equals(lowerLegLeft) && !leftSkiPopped) {

            doPopLeftSki = true;
        }

        if(body.equals(lowerLegRight) && !rightSkiPopped) {

            doPopRightSki = true;
        }
    }

    public float getLeftKneeAngle() {

        return kneeJointLeft.getJointAngle();
    }

    public float getRightKneeAngle() {

        return kneeJointRight.getJointAngle();
    }

    public boolean leftSkiPopped() {

        return leftSkiPopped;
    }

    public boolean rightSkiPopped() {

        return rightSkiPopped;
    }

    public Body getUpperBody() {

        return torso;
    }

    // average out the velocity so that when the biped is flipping fast, it's slightly more accurate
    public float getVelocity() {

        return (torso.getLinearVelocity().len() + lowerLegLeft.getLinearVelocity().len()) * 0.5f;
    }

    // private functions
    private void createSki(final Body body, final Vector2 velocity, float angularVelocity, boolean isLeft) {

        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.DynamicBody;
        bd.position.set(body.getPosition());
        bd.angle = body.getAngle();
        //bd.bullet = true;

        Body ski;

        if(isLeft && leftPoppedSki != null) {

            world.destroyBody(leftPoppedSki);
            leftPoppedSki = null;
        } else if(!isLeft && rightPoppedSki != null) {

            world.destroyBody(rightPoppedSki);
            rightPoppedSki = null;
        }

        ski = world.createBody(bd);

        FixtureDef skiDef = new FixtureDef();
        skiDef.filter.groupIndex = skiSettings.groupIndex;
        skiDef.density = skiSettings.density;
        skiDef.restitution = skiSettings.restitution;

        float halfSkiLength = skiSettings.length / 2.0f;

        PolygonShape skiShape = new PolygonShape();
        skiShape.setAsBox(halfSkiLength, skiSettings.thickness);
        skiDef.shape = skiShape;
        ski.createFixture(skiDef).setUserData(new BodyType(BodyType.BodyTypeSki));

        createSkiTips(halfSkiLength, skiDef, ski);

        // Compute consistent velocities for ski
        Vector2 centerDiff = body.getWorldCenter().cpy().sub(ski.getWorldCenter());
        ski.setAngularVelocity(angularVelocity);
        ski.setLinearVelocity(velocity.cpy().add(new Vector2(-angularVelocity * centerDiff.y, angularVelocity * centerDiff.x)));

        if(isLeft) {

            leftPoppedSki = ski;
            leftSkiSpriteStruct.body = leftPoppedSki;
            lowerLegLeftSpriteStruct.sprite = lowerLegSprite;
        } else {

            rightPoppedSki = ski;
            rightSkiSpriteStruct.body = rightPoppedSki;
            lowerLegRightSpriteStruct.sprite = lowerLegSprite;
        }
    }

    private void createSkiTips(float halfSkiLength, FixtureDef skiDef, Body ski) {

        float halfHeight = skiSettings.height / 2f;
        CircleShape bottomTipShape = new CircleShape();
        bottomTipShape.setRadius(halfHeight);
        bottomTipShape.setPosition(new Vector2(halfSkiLength, -halfHeight));
        skiDef.shape = bottomTipShape;
        ski.createFixture(skiDef).setUserData(new BodyType(BodyType.BodyTypeSki));
        bottomTipShape.setPosition(new Vector2(-halfSkiLength, -halfHeight));
        ski.createFixture(skiDef).setUserData(new BodyType(BodyType.BodyTypeSki));

        // the top shape has more friction to simulate a tip
        CircleShape topTipShape = new CircleShape();
        topTipShape.setRadius(halfHeight);
        topTipShape.setPosition(new Vector2(halfSkiLength, halfHeight));
        skiDef.shape = topTipShape;
        skiDef.friction = skiSettings.poppedFriction;
        ski.createFixture(skiDef).setUserData(new BodyType(BodyType.BodyTypeSki));
        topTipShape.setPosition(new Vector2(-halfSkiLength, halfHeight));
        ski.createFixture(skiDef).setUserData(new BodyType(BodyType.BodyTypeSki));
    }

    public float getSkiAngle() {

        try {

            return rightSki.getBody().getAngle();
        } catch(NullPointerException ex) {

            return 0.0f;
        }
    }

    public void drawBipedSprites(final OrthographicCamera camera, final SpriteBatch spriteBatch) {

        // NOTE: sprite's need to be drawn in specific order
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        drawSprite(spriteBatch, headSpriteStruct);

        drawSprite(spriteBatch, lowerLegLeftSpriteStruct);
        drawSprite(spriteBatch, upperLegLeftSpriteStruct);

        drawSprite(spriteBatch, upperArmLeftSpriteStruct);
        drawSprite(spriteBatch, lowerArmLeftSpriteStruct);

        drawSprite(spriteBatch, torsoSpriteStruct);

        drawSprite(spriteBatch, upperArmRightSpriteStruct);
        drawSprite(spriteBatch, lowerArmRightSpriteStruct);

        drawSprite(spriteBatch, lowerLegRightSpriteStruct);
        drawSprite(spriteBatch, upperLegRightSpriteStruct);

        if(leftSkiPopped && leftPoppedSki != null) {

            drawSprite(spriteBatch, leftSkiSpriteStruct);
        }

        if(rightSkiPopped && rightPoppedSki != null) {

            drawSprite(spriteBatch, rightSkiSpriteStruct);
        }

        spriteBatch.end();
    }

    private void drawSprite(final SpriteBatch spriteBatch, final SpriteStruct spriteStruct) {

        spriteStruct.sprite.setRotation(MathUtils.radiansToDegrees * spriteStruct.body.getAngle());
        spriteStruct.sprite.setBounds(spriteStruct.body.getWorldCenter().x + spriteStruct.rect.x,
                spriteStruct.body.getWorldCenter().y + spriteStruct.rect.y,
                spriteStruct.rect.width,
                spriteStruct.rect.height);
        spriteStruct.sprite.draw(spriteBatch);
    }

    public void drawBipedDebug(final ShapeRenderer shapeRenderer, final DrawSettings drawSettings) {

        shapeRenderer.setColor(Color.WHITE);
        SimpleRenderer.render(lowerLegLeft, shapeRenderer, drawSettings);
        SimpleRenderer.render(lowerLegRight, shapeRenderer, drawSettings);
        SimpleRenderer.render(upperLegLeft, shapeRenderer, drawSettings);
        SimpleRenderer.render(upperLegRight, shapeRenderer, drawSettings);
        SimpleRenderer.render(torso, shapeRenderer, drawSettings);
        SimpleRenderer.render(head, shapeRenderer, drawSettings);
        SimpleRenderer.render(upperArmLeft, shapeRenderer, drawSettings);
        SimpleRenderer.render(upperArmRight, shapeRenderer, drawSettings);
        SimpleRenderer.render(lowerArmLeft, shapeRenderer, drawSettings);
        SimpleRenderer.render(lowerArmRight, shapeRenderer, drawSettings);
        if(leftPoppedSki != null) {

            SimpleRenderer.render(leftPoppedSki, shapeRenderer, drawSettings);
        }
        if(rightPoppedSki != null) {

            SimpleRenderer.render(rightPoppedSki, shapeRenderer, drawSettings);
        }
    }
}
