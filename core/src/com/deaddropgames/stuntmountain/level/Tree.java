package com.deaddropgames.stuntmountain.level;

public class Tree {

    public float width;
    public float height;
    public float trunkHeight;
    public int levels;
    public Point location;

    public Tree() {

        width = 3.0f;
        height = 5.0f;
        trunkHeight = 0.15f * height;
        levels = 5;
        location = new Point();
    }
}
