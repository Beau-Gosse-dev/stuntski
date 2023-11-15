package com.deaddropgames.stuntmountain.settings;

public class TreeSettings {

    public short groupIndex;
    public float density;
    public float restitution;
    public float torquePerMass;
    public float multiplierP;
    public float multiplierI;
    public float multiplierD;

    public TreeSettings() {

        groupIndex = -10;
        density = 60.0f;
        restitution = 0.3f;
        torquePerMass = 25.0f;
        multiplierP = 20.0f;
        multiplierI = 0.0f;
        multiplierD = 0.0f;
    }
}
