package com.deaddropgames.stuntmountain.util;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;

public class SpriteStruct {

    public Body body;
    public Sprite sprite;
    public Rectangle rect;

    public SpriteStruct(final Body body, final Sprite sprite, final Rectangle rect) {

        this.body = body;
        this.sprite = sprite;
        this.rect = rect;
    }
}
