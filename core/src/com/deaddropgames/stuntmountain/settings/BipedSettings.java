package com.deaddropgames.stuntmountain.settings;

public class BipedSettings {

    public short groupIndex;
    public float density;
    public float restitution;
    public float friction;
    public float maxMotorLimit;

    // motor speed multipliers
    public float kneeMultiplierP;
    public float kneeMultiplierI;
    public float kneeMultiplierD;

    public float hipMultiplierP;
    public float hipMultiplierI;
    public float hipMultiplierD;

    public float shoulderMultiplier;
    public float elbowMultiplier;

    public float startX;
    public float startY;

    public BipedSettings() {

        groupIndex = -1;
        density = 120.0f;
        restitution = 0.3f;
        maxMotorLimit = 750.0f;
        friction = 5.0f;

        // motor multipliers
        kneeMultiplierP = 30.0f;
        kneeMultiplierI = 30.0f;
        kneeMultiplierD = 30.0f;
        hipMultiplierP = 10.0f;
        hipMultiplierI = 0.5f;
        hipMultiplierD = 2.0f;
        shoulderMultiplier = 20.0f;
        elbowMultiplier = 20.0f;

        startX = 1.0f;
        startY = 1.0f;
    }
}
