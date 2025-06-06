package com.deaddropgames.stuntmountain.settings;

public class LevelSettings {

    public float gravity;
    public boolean doSleep;
    public int velocityIterations;
    public int positionIterations;
    public float bailForce; // the force required to knock out our biped
    public float surfaceFriction;
    public int defaultControlAreaType; // 0 is full, 1 is left, 2 is right

    public LevelSettings() {

        gravity = -10.0f;
        doSleep = true;
        velocityIterations = 10;
        positionIterations = 10;
        bailForce = 80.0f;
        surfaceFriction = 0.03f;
        defaultControlAreaType = 0;
    }
}
