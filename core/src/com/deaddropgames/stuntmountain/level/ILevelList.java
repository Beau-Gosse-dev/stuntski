package com.deaddropgames.stuntmountain.level;


public interface ILevelList {

    Level getNextLevel(Level currentLevel);

    boolean hasNextLevel(Level currentLevel);
}
