package com.deaddropgames.stuntmountain.util;

public class PIDController {

    private float setPoint;
    private float lastError;
    private float errorSum;

    private float pGain;
    private float iGain;
    private float dGain;

    public PIDController(float pGain, float iGain, float dGain) {

        setPoint = 0.0f;
        lastError = 0.0f;
        errorSum = 0.0f;

        this.pGain = pGain;
        this.iGain = iGain;
        this.dGain = dGain;
    }

    public void setSetPoint(float setPoint) {
        
        if(this.setPoint == setPoint) {

            return;
        }

        errorSum = 0.0f;
        this.setPoint = setPoint;
        lastError = 0.0f;
    }

    public float update(float in, float delta) {

        float currError = setPoint - in;
        errorSum += currError * delta;

        float out = (pGain * currError) + (iGain * errorSum) + dGain * (currError - lastError);
        lastError = currError;
        return out;
    }
}
