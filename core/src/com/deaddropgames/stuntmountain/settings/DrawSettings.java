package com.deaddropgames.stuntmountain.settings;

public class DrawSettings {

    public float minScale;
    public float maxScale;
    public float maxSpeedForMaxScale;
    public float extentsFactor;
    public int circleSeg;
    public float viewportOffsetX;
    public float viewportOffsetY;

    public DrawSettings() {

        minScale = 0.15f; // minimum scaling of zoom
        maxScale = 0.30f; // maximum scaling of zoom
        maxSpeedForMaxScale = 15.0f; // speed at which zoom maxes out
        extentsFactor = 25.0f; // a factor to change the amount of the world viewed in the world
        circleSeg = 16; // the number of segments to render for a circle
        viewportOffsetX = 35.0f; // offset the viewport in the x direction
        viewportOffsetY = 5.0f; // offset the viewport in the y direction
    }
}
