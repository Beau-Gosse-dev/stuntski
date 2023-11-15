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

    public LevelStats() {

        points = 0;
        numBackFlips = 0;
        numFrontFlips = 0;
        airTimeSeconds = 0f;
        airDistanceMetres = 0f;
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

    private float getAirTimeSeconds() {

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

        table.add(createButtonLabel("Total Air Time:", skin)).right().padRight(padding);
        table.add(createButtonLabel(String.format(Locale.getDefault(), "%.1f seconds", airTimeSeconds), skin)).left().padRight(padding);
        table.row();

        float distance = isMetric ? airDistanceMetres : airDistanceMetres * metresToFeet;
        String units = isMetric ? "metres" : "feet";

        table.add(createButtonLabel("Total Air Distance:", skin)).right().padRight(padding);
        table.add(createButtonLabel(String.format(Locale.getDefault(), "%.1f %s", distance, units), skin)).left().padRight(padding);
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
    }

    private Label createButtonLabel(final String text, final Skin skin) {

        return new Label(text, new Label.LabelStyle(skin.getFont("button"), skin.getColor("white")));
    }
}
