package com.deaddropgames.stuntmountain.util;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

import java.util.Locale;

public class LevelStats {

    static final transient private float metresToFeet = 3.28084f;

    private int points;
    private int numBackFlips;
    private int numFrontFlips;
    private float airTimeSeconds;
    private float airDistanceMetres;
    private float completionTimeSeconds;
    private float longestJumpAirTimeSeconds;
    private float longestJumpDistanceMetres;

    public LevelStats() {

        points = 0;
        numBackFlips = 0;
        numFrontFlips = 0;
        airTimeSeconds = 0f;
        airDistanceMetres = 0f;
        completionTimeSeconds = 0f;
        longestJumpAirTimeSeconds = 0f;
        longestJumpDistanceMetres = 0f;
    }

    public int getPoints() {

        return points;
    }

    public void setPoints(int points) {

        this.points = points;
    }

    private int getNumBackFlips() {

        return numBackFlips;
    }

    public void addBackFlips(int numBackFlips) {

        this.numBackFlips += numBackFlips;
    }

    private int getNumFrontFlips() {

        return numFrontFlips;
    }

    public void addFrontFlips(int numFrontFlips) {

        this.numFrontFlips += numFrontFlips;
    }

    public float getAirTimeSeconds() {

        return airTimeSeconds;
    }

    public void addAirTimeSeconds(float airTimeSeconds) {

        this.airTimeSeconds += airTimeSeconds;
    }

    private float getAirDistanceMetres() {

        return airDistanceMetres;
    }

    public void addAirDistanceMetres(float airDistanceMetres) {

        this.airDistanceMetres += airDistanceMetres;
    }

    public float getCompletionTimeSeconds() {

        return completionTimeSeconds;
    }

    public void setCompletionTimeSeconds(float completionTimeSeconds) {

        this.completionTimeSeconds = completionTimeSeconds;
    }

    public float getLongestJumpAirTimeSeconds() {

        return longestJumpAirTimeSeconds;
    }

    public void updateLongestJumpAirTime(float jumpAirTimeSeconds) {

        if (jumpAirTimeSeconds > longestJumpAirTimeSeconds) {
            longestJumpAirTimeSeconds = jumpAirTimeSeconds;
        }
    }

    public float getLongestJumpDistanceMetres() {

        return longestJumpDistanceMetres;
    }

    public void updateLongestJumpDistance(float jumpDistanceMetres) {

        if (jumpDistanceMetres > longestJumpDistanceMetres) {
            longestJumpDistanceMetres = jumpDistanceMetres;
        }
    }

    public Table getSummaryTable(boolean isMetric, Skin skin, NinePatchDrawable patch) {

        Table table = new Table(skin);
        table.setBackground(patch);

        int padding = 10;

        table.add(createButtonLabel("Level Complete", skin)).colspan(2).center();
        table.row();

        table.add(createButtonLabel("Total Points:", skin)).right().padRight(padding);
        table.add(createButtonLabel(points + "", skin)).left();
        table.row();

        table.add(createButtonLabel("Back Flips:", skin)).right().padRight(padding);
        table.add(createButtonLabel(numBackFlips + "", skin)).left().padRight(padding);
        table.row();

        table.add(createButtonLabel("Front Flips:", skin)).right().padRight(padding);
        table.add(createButtonLabel(numFrontFlips + "", skin)).left().padRight(padding);
        table.row();

        table.add(createButtonLabel("Longest Jump Time:", skin)).right().padRight(padding);
        table.add(createButtonLabel(String.format(Locale.getDefault(), "%.1f seconds", longestJumpAirTimeSeconds), skin)).left().padRight(padding);
        table.row();

        float distance = isMetric ? longestJumpDistanceMetres : longestJumpDistanceMetres * metresToFeet;
        String units = isMetric ? "metres" : "feet";

        table.add(createButtonLabel("Longest Jump Distance:", skin)).right().padRight(padding);
        table.add(createButtonLabel(String.format(Locale.getDefault(), "%d %s", Math.round(distance), units), skin)).left().padRight(padding);
        table.row();

        return table;
    }

    /**
     * Compares this object's stats with another stats object and keeps the high scores
     * @param otherStats another level stats object to merge into this one
     */
    public void merge(final LevelStats otherStats) {

        if(otherStats.getPoints() > points) {

            points = otherStats.getPoints();
        }

        if(otherStats.getNumBackFlips() > numBackFlips) {

            numBackFlips = otherStats.getNumBackFlips();
        }

        if(otherStats.getNumFrontFlips() > numFrontFlips) {

            numFrontFlips = otherStats.getNumFrontFlips();
        }

        if(otherStats.getAirTimeSeconds() > airTimeSeconds) {

            airTimeSeconds = otherStats.getAirTimeSeconds();
        }

        if(otherStats.getAirDistanceMetres() > airDistanceMetres) {

            airDistanceMetres = otherStats.getAirDistanceMetres();
        }

        // For completion time, we want the LOWEST (best) time, not highest
        // But only if both times are greater than 0 (valid times)
        if(otherStats.getCompletionTimeSeconds() > 0) {
            if(completionTimeSeconds == 0 || otherStats.getCompletionTimeSeconds() < completionTimeSeconds) {
                completionTimeSeconds = otherStats.getCompletionTimeSeconds();
            }
        }

        if(otherStats.getLongestJumpAirTimeSeconds() > longestJumpAirTimeSeconds) {
            longestJumpAirTimeSeconds = otherStats.getLongestJumpAirTimeSeconds();
        }

        if(otherStats.getLongestJumpDistanceMetres() > longestJumpDistanceMetres) {
            longestJumpDistanceMetres = otherStats.getLongestJumpDistanceMetres();
        }
    }

    private Label createButtonLabel(final String text, final Skin skin) {

        return new Label(text, new Label.LabelStyle(skin.getFont("button"), skin.getColor("white")));
    }
}
