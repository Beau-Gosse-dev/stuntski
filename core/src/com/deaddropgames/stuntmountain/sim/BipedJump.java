package com.deaddropgames.stuntmountain.sim;

import com.badlogic.gdx.math.Vector2;

import java.util.Locale;

public class BipedJump {

    static final private float metresToFeet = 3.28084f;

    private long landTime;
    private String jumpMsg;
    private int points;
    private int numFlips;
    private float airTime;
    private float distanceMetres;

    // constants
    static final private float oneFlip = 2.0f * (float)Math.PI;

    public BipedJump(final long launchTime,
            final long landTime,
            final float rotationRadians,
            final Vector2 launchPos,
            final Vector2 landPos,
            boolean isMetric) {

        this.landTime = landTime;

        numFlips = Math.round(rotationRadians / oneFlip);
        int absNumFlips = Math.abs(numFlips);
        int flipPoints = 0;
        int airTimePoints = 0;
        int distancePoints = 0;
        jumpMsg = "";

        if(absNumFlips > 0) {

            jumpMsg = "Front Flip";
            if(numFlips > 0) {

                jumpMsg = "Back Flip";
            }

            if(absNumFlips > 1) {

                switch(absNumFlips) {

                    case 2:
                        jumpMsg = "Double " + jumpMsg;
                        break;
                    case 3:
                        jumpMsg = "Triple " + jumpMsg;
                        break;
                    case 4:
                        jumpMsg = "Quadruple " + jumpMsg;
                        break;
                    default:
                        jumpMsg = String.format(Locale.getDefault(), "%d %ss", absNumFlips, jumpMsg);
                        break;
                }
            }

            flipPoints = absNumFlips * 100 + (absNumFlips - 1) * 25;
            jumpMsg += String.format(Locale.getDefault(), " [+%d]\n", flipPoints);
        }

        airTime = (landTime - launchTime) / 1e3f;
        if(airTime > 1.0f) {
            airTimePoints = Math.round((airTime - 1.0f) * 25.0f);
        }
        jumpMsg += String.format(Locale.getDefault(), "%d seconds [+%d]\n", Math.round(airTime), airTimePoints);

        if(launchPos != null && landPos != null) {

            float distance = landPos.dst(launchPos);
            distanceMetres = distance;
            if(distance > 15.0f) {
                distancePoints = Math.round((distance - 15.0f) * 10.0f);
            }
            
            if(!isMetric) {
                distance *= metresToFeet;
                jumpMsg += String.format(Locale.getDefault(), "%d feet [+%d]", Math.round(distance), distancePoints);
            } else {
                jumpMsg += String.format(Locale.getDefault(), "%d meters [+%d]", Math.round(distance), distancePoints);
            }
        }

        points = airTimePoints + flipPoints + distancePoints;
    }

    public long getLandTime() {

        return landTime;
    }

    public String getJumpMsg() {

        return jumpMsg;
    }

    public int getPoints() {

        return points;
    }

    public int getNumBackFlips() {

        return numFlips > 0 ? numFlips : 0;
    }

    public int getNumFrontFlips() {

        return numFlips < 0 ? -numFlips : 0;
    }

    public float getAirTime() {

        return airTime;
    }

    public float getDistanceMetres() {

        return distanceMetres;
    }
}
