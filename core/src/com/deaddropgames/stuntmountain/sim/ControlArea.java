package com.deaddropgames.stuntmountain.sim;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class ControlArea extends Rectangle {

    // textures and sprites
    private Sprite knobSprite;
    private float knobRadius;

    public final static int FULLSCREEN = 0;
    public final static int LEFTBOTTOM = 1;
    public final static int RIGHTBOTTOM = 2;

    private float halfWidth;
    private float halfHeight;
    private int type;
    private float touchX;
    private float touchY;

    public float xRatio;
    public float yRatio;

    public ControlArea(float width, float height, int type, Sprite knobSprite) {

        x = 0.0f;
        y = 0.0f;
        touchX = 0.0f;
        touchY = 0.0f;
        float touchBallRadius;

        // if we aren't full screen, we will push the area flush to the side and make it a
        //  perfect square
        if(type != ControlArea.FULLSCREEN) {

            // the control area will be about one quarter...
            width /= 2.0f;
            height /= 2.0f;
            y = height;

            // this game is played in landscape, so the delta x > delta y (should be!)
            float diff = width - height;
            if(type == ControlArea.LEFTBOTTOM) {

                width -= diff;
            } else { // type == ControlArea.RIGHTBOTTOM

                x = width + diff;
                width -= diff;
            }

            this.width = width;
            touchBallRadius = width / 4.0f;
            touchX = x + touchBallRadius * 2.0f;
            touchY = y + touchBallRadius * 2.0f;
        }

        halfWidth = width / 2.0f;
        halfHeight = height / 2.0f;
        this.type = type;
        this.width = width;
        this.height = height;

        this.knobSprite = new Sprite(knobSprite);
        knobRadius = knobSprite.getHeight() / 2.0f;
    }

    public void calcRatios(int x, int y) {

        touchX = x;
        touchY = y;

        x -= super.x;
        y -= super.y;

        xRatio = ((float)x - halfWidth)/halfWidth;
        yRatio = (halfHeight - (float)y)/halfHeight;
    }

    public void drawShapes(OrthographicCamera camera, ShapeRenderer shapeRenderer) {

        if(type == ControlArea.FULLSCREEN) {

            return;
        }

        Vector3 vecStart = new Vector3(x, y, 0);
        camera.unproject(vecStart);
        Vector3 vecEnd = new Vector3(x + width, y + height, 0);
        camera.unproject(vecEnd);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setColor(0.53725f, 0.53725f, 0.57647f, 0.3f);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.circle((vecStart.x + vecEnd.x) / 2.0f, (vecStart.y + vecEnd.y) / 2.0f, (vecEnd.y - vecStart.y) / 2.0f, 32);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    public void drawSprites(OrthographicCamera camera, SpriteBatch spriteBatch) {

        if(type == ControlArea.FULLSCREEN) {

            return;
        }

        Vector3 textureTopLeft = new Vector3(touchX - knobRadius, touchY + knobRadius, 0);
        camera.unproject(textureTopLeft);
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        knobSprite.setPosition(textureTopLeft.x, textureTopLeft.y);
        knobSprite.draw(spriteBatch);
        spriteBatch.end();
    }

    public void reset() {

        touchX = x + width / 2.0f;
        touchY = y + height / 2.0f;
    }
}
