package com.deaddropgames.stuntmountain.settings;

public class SkiSettings {

    public float length;
    public float height;
    public float thickness;
    public short groupIndex;
    public float density;
    public float restitution;
    public float kneeSkiPopAngle; // max angle, in radians, of the knee joint that triggers the ski to pop off
    public float normalSkiPopForce; // max normal force (away from ski boot) required to pop skis off
    public float poppedFriction; // the friction the skis feel when they have popped off

    public SkiSettings() {

        length = 1.6f;
        height = 0.09f;
        thickness = 0.01f;
        groupIndex = -5;
        density = 120.0f;
        restitution = 0.3f;
        kneeSkiPopAngle = -0.09f;
        normalSkiPopForce = 20.0f;
        poppedFriction = 50.0f;
    }
}
