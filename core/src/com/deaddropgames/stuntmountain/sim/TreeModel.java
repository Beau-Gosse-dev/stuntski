package com.deaddropgames.stuntmountain.sim;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Transform;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.deaddropgames.stuntmountain.level.Tree;
import com.deaddropgames.stuntmountain.settings.DrawSettings;
import com.deaddropgames.stuntmountain.settings.TreeSettings;
import com.deaddropgames.stuntmountain.util.PIDController;

public class TreeModel {

    public static short groupIndex = 0;
    private static Color trunkColor = null;
    private static Color branchColor = null;

    private World world;
    private Tree params;
    private TreeSettings settings;
    private Body trunk;
    private Body [] levels;
    private RevoluteJoint [] joints;
    private PIDController [] controllers;
    private Rectangle boundingBox;
    private Rectangle trunkRect;

    public TreeModel(World world, final Tree params, final TreeSettings settings) {

        this.world = world;
        this.params = params;
        this.settings = settings;

        // only initialize this if it's the first instance...
        if(TreeModel.groupIndex == 0) {

            TreeModel.groupIndex = settings.groupIndex;
        }

        create(new Vector2(params.location.x, params.location.y));
    }

    private void create(final Vector2 position) {

        // create the trunk
        BodyDef trunkBd = new BodyDef();
        trunkBd.type = BodyDef.BodyType.StaticBody;
        trunkBd.position.set(position);

        PolygonShape trunkShape = new PolygonShape();
        float trunkWidth = 0.3f * params.width;
        float trunkHeight = params.trunkHeight;
        trunkShape.setAsBox(0.5f * trunkWidth, 0.5f * trunkHeight, new Vector2(0.0f, 0.5f * trunkHeight), 0.0f);

        trunk = world.createBody(trunkBd);
        trunk.createFixture(trunkShape, 0.0f);
        trunk.setUserData(new BodyType(BodyType.BodyTypeTree));

        boundingBox = new Rectangle(position.x - 0.5f * trunkWidth,
                position.y,
                trunkWidth,
                trunkHeight);

        trunkRect = new Rectangle(boundingBox);

        // create the pine triangle sections
        BodyDef pineBd = new BodyDef();
        pineBd.type = BodyDef.BodyType.DynamicBody;
        pineBd.position.set(position);

        FixtureDef bodyFixtureDef = new FixtureDef();
        bodyFixtureDef.filter.groupIndex = TreeModel.groupIndex--;
        bodyFixtureDef.density = settings.density;
        bodyFixtureDef.restitution = settings.restitution;

        levels = new Body[params.levels];
        float levelHeight = (params.height - trunkHeight) / (float)levels.length;
        float startWidth = params.width * 0.5f;
        float widthDec = (params.width - trunkWidth) / (float)levels.length * 0.65f;
        for(int ii = 0; ii < levels.length; ii++) {

            levels[ii] = world.createBody(pineBd);

            float lower = trunkHeight + ii * levelHeight;
            float upper = lower + levelHeight;
            // adjust lower so it overlaps previous level
            if(ii > 0) {
                lower -= 0.25f * levelHeight;
            }
            float halfWidth = startWidth - ii * widthDec;
            PolygonShape pine = new PolygonShape();
            Vector2[] vertices = new Vector2[3];
            vertices[0] = new Vector2(halfWidth, lower);
            vertices[1] = new Vector2(-halfWidth, lower);
            vertices[2] = new Vector2(0.0f, upper);
            pine.set(vertices);

            boundingBox.merge(new Rectangle(position.x - halfWidth,
                    position.y + lower,
                    halfWidth*2.0f,
                    upper-lower));

            bodyFixtureDef.shape = pine;
            levels[ii].createFixture(bodyFixtureDef);
            levels[ii].setUserData(new BodyType(BodyType.BodyTypeTree));
        }

        // create the joints that hold it all together
        RevoluteJointDef jointDef = new RevoluteJointDef();
        joints = new RevoluteJoint[levels.length];
        controllers = new PIDController[levels.length];
        float currMass = 0;
        // max joint angles depend on number of
        float maxAngle = 0f;
        if(levels.length > 0) {

            maxAngle = MathUtils.PI / 2.0f / levels.length;
        }
        for(int ii = levels.length - 1; ii >= 0; ii--) {

            Body bodyA, bodyB;
            Vector2 vec;
            if(ii == 0) {

                bodyA = trunk;
                bodyB = levels[ii];
                vec = new Vector2(0.0f, trunkHeight);
            } else {

                bodyA = levels[ii-1];
                bodyB = levels[ii];
                vec = new Vector2(0.0f, trunkHeight + ii * levelHeight);
                currMass += bodyB.getMass();
            }
            vec.add(position);
            jointDef.initialize(bodyA, bodyB, vec);
            jointDef.enableLimit = true;
            jointDef.enableMotor = true;
            jointDef.lowerAngle = -maxAngle;
            jointDef.upperAngle = -jointDef.lowerAngle;
            // the more mass the current motor is holding, the more torque it will be allowed to apply
            jointDef.maxMotorTorque = currMass * settings.torquePerMass;
            jointDef.motorSpeed = 0.0f;
            joints[ii] = (RevoluteJoint)world.createJoint(jointDef);
            controllers[ii] = new PIDController(settings.multiplierP, settings.multiplierI, settings.multiplierD);
        }
    }

    public void updateMotors(float delta) {

        for(int ii = 0; ii < joints.length; ii++) {

            RevoluteJoint joint = joints[ii];
            PIDController controller = controllers[ii];

            if(joint.isMotorEnabled()) {

                joint.setMotorSpeed(controller.update(joint.getJointAngle(), delta));
            }
        }
    }

    public void destroy() throws Throwable {

        if(world == null) {

            return;
        }

        // NOTE: destroying bodies will destroy joints once connected bodies are destroyed!
        for(int ii = 0; ii < levels.length; ii++) {

            world.destroyBody(levels[ii]);
            levels[ii] = null;
        }
        levels = null;
        joints = null; // see note above on why we don't destroy these manually
        controllers = null;

        world.destroyBody(trunk);
        trunk = null;
    }

    public boolean isInViewport(final Rectangle viewport) {

        return boundingBox.overlaps(viewport);
    }

    @SuppressWarnings("unused")
    public void drawDebug(final ShapeRenderer shapeRenderer, final DrawSettings drawSettings) {

        if(trunkColor == null) {

            trunkColor = new Color(0.361f, 0.122f, 0.118f, 1.0f);
            branchColor = new Color(0.133f, 0.424f, 0.008f, 1.0f);
        }

        Transform transform;
        PolygonShape shape;

        // draw the stump
        shapeRenderer.setColor(trunkColor);
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.rect(trunkRect.x, trunkRect.y, trunkRect.width, trunkRect.height);

        // draw the stump up to the peak of the first level
        if(levels.length > 0 && levels[0].getFixtureList().size > 0) {

            shape = (PolygonShape)levels[0].getFixtureList().get(0).getShape();
            Vector2 peak = new Vector2();
            shape.getVertex(1, peak);
            transform = levels[0].getTransform();
            transform.mul(peak);
            shapeRenderer.triangle(trunkRect.x, trunkRect.y + trunkRect.height,
                    trunkRect.x + trunkRect.width, trunkRect.y + trunkRect.height,
                    peak.x, peak.y);
        }

        shapeRenderer.end();

        shapeRenderer.setColor(branchColor);

        // draw each level
        Vector2 first = new Vector2(), second = new Vector2(), third = new Vector2();
        for(Body body : levels) {

            transform = body.getTransform();
            for(Fixture fixture : body.getFixtureList()) {

                shape = (PolygonShape)fixture.getShape();
                shape.getVertex(0, first);
                shape.getVertex(1, second);
                shape.getVertex(2, third);

                transform.mul(first);
                transform.mul(second);
                transform.mul(third);

                shapeRenderer.begin(ShapeType.Filled);
                shapeRenderer.triangle(first.x, first.y, second.x, second.y, third.x, third.y);
                shapeRenderer.end();
            }
        }
    }
}
