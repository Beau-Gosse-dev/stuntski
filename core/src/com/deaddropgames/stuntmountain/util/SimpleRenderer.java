package com.deaddropgames.stuntmountain.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.Transform;
import com.deaddropgames.stuntmountain.settings.DrawSettings;

public class SimpleRenderer {

    private static final String LOG = "BodyRenderer";

    private static Vector2 temp = null;
    private static Vector2 vertex = null;
    private static Vector2 center = null;

    public static void render(final Body body, final ShapeRenderer shapeRenderer, final DrawSettings drawSettings) {

        if(body == null || body.getFixtureList() == null) {

            return;
        }

        // lazy load of private members
        if(temp == null) {

            temp = new Vector2();
            vertex = new Vector2();
            center = new Vector2();
        }

        float radius = 0.0f;
        Transform transform;
        CircleShape circleShape;
        PolygonShape polyShape;
        ChainShape chainShape;
        Shape shape;
        for(Fixture fixture : body.getFixtureList()) {

            shape = fixture.getShape();
            transform = body.getTransform();
            if(fixture.getType() == Shape.Type.Circle) {

                circleShape = (CircleShape)shape;
                center = circleShape.getPosition();
                transform.mul(center);
                radius = circleShape.getRadius();
                shapeRenderer.begin(ShapeType.Line);
                shapeRenderer.circle(center.x, center.y, radius, drawSettings.circleSeg);
                shapeRenderer.end();
            } else if(fixture.getType() == Shape.Type.Polygon) {

                polyShape = (PolygonShape)shape;
                polyShape.getVertex(0, vertex);
                transform.mul(vertex);
                for(int ii = 1; ii < polyShape.getVertexCount(); ii++) {

                    polyShape.getVertex(ii, temp);
                    transform.mul(temp);
                    shapeRenderer.begin(ShapeType.Line);
                    shapeRenderer.line(vertex.x, vertex.y, temp.x, temp.y);
                    shapeRenderer.end();
                    vertex = temp.cpy();
                }

                polyShape.getVertex(0, temp);
                transform.mul(temp);
                shapeRenderer.begin(ShapeType.Line);
                shapeRenderer.line(vertex.x, vertex.y, temp.x, temp.y);
                shapeRenderer.end();
            } else if(fixture.getType() == Shape.Type.Chain) {

                // NOTE: using chain shapes as terrain doesn't seem to work too well...
                chainShape = (ChainShape)shape;
                chainShape.getVertex(0, vertex);
                transform.mul(vertex);
                for(int ii = 1; ii < chainShape.getVertexCount(); ii++) {

                    chainShape.getVertex(ii, temp);
                    transform.mul(temp);
                    shapeRenderer.begin(ShapeType.Line);
                    shapeRenderer.line(vertex.x, vertex.y, temp.x, temp.y);
                    shapeRenderer.end();
                    vertex = temp.cpy();
                }
            } else {

                Gdx.app.error(LOG, "Encountered unsupported shape: " + fixture.getType());
            }
        }
    }
}
