package com.deaddropgames.stuntmountain.level;

import java.util.ArrayList;

public class LevelPack {

    public String name;
    public String description;
    public int difficulty;
    public ArrayList<String> levelFilenames;

    public LevelPack() {

        name = "";
        description = "";
        difficulty = 0;
        levelFilenames = new ArrayList<String>();
    }
}
