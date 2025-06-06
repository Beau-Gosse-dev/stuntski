package com.deaddropgames.stuntmountain.level;

import java.util.Locale;

public class Level {

    public transient long id;

    public String name;
    public String description;
    public String author;
    public int difficulty;
    public float endX;
    public PolyLine[] polyLines;
    public Tree[] trees;

    public Level() {

        id = 0;
        name = "";
        description = "";
        author = "";
        difficulty = 0;
        endX = -1f;
    }

    /**
     * Simple ID used to persist level stats
     * @return a string representing a (simple) unique ID for this level
     */
    public String getStatsId() {

        return String.format(Locale.getDefault(), "%s, %s, %d", name, author, difficulty);
    }
}
