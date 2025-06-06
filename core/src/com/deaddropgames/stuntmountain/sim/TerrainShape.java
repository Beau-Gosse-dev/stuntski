package com.deaddropgames.stuntmountain.sim;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class TerrainShape {

    private Vector2 start;
    private Vector2 end;
    private boolean isVertical;
    private boolean isFlat;
    private Vector2 third;
    private float minY;
    private float width;
    private float minX;
    private float maxX;

    // NOTE: this assumes that start.x < end.x...it won't work well otherwise.
    public TerrainShape(final Vector2 start, final Vector2 end) {

        this.start = new Vector2(start);
        this.end = new Vector2(end);

        // if it's vertical, we need not do anything...
        isVertical = false;
        if(start.x == end.x) {

            isVertical = true;
            return;
        }

        // check if it's flat - if so we just draw a rectangle, otherwise we need to draw a triangle too
        isFlat = false;
        if(start.y == end.y) {

            isFlat = true;
        } else {

            // otherwise we need the third point for the triangle
            if(end.y > start.y) {

                third = new Vector2(end.x, start.y);
            } else { // end.y < start.y

                third = new Vector2(start.x, end.y);
            }
        }

        // for the rectangle we will draw
        minY = Math.min(start.y, end.y);
        width = Math.abs(end.x - start.x);

        // for checking if the shape is within the viewport
        minX = Math.min(start.x, end.x);
        maxX = Math.max(start.x, end.x);
    }

    public void draw(final ShapeRenderer shapeRenderer, float bottom) {

        // if its vertical, no need to draw, the previous shape would have essentially drawn it
        if(isVertical) {

            return;
        }

        shapeRenderer.setColor(Color.WHITE);

        // if its not flat, draw a triangle first
        if(!isFlat) {

            // draw rectangle
            shapeRenderer.begin(ShapeType.Filled);
            shapeRenderer.triangle(start.x, start.y, end.x, end.y, third.x, third.y);
            shapeRenderer.end();
        }

        // finally draw the bulk of the shape as a rectangle
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.rect(start.x, bottom, width, minY - bottom);
        shapeRenderer.end();
    }

    public boolean isInViewport(final Rectangle viewport) {

        // if either x point is within the range of x values in the viewport OR if both points are outside of the
        // viewport, but on either side, then we draw the polygon
        float viewportEndX = viewport.x + viewport.width;
        return (start.x >= viewport.x && start.x <= viewportEndX || end.x >= viewport.x && end.x <= viewportEndX) ||
                (minX < viewport.x && maxX > viewportEndX);
    }
}
