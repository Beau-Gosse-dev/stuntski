package com.deaddropgames.stuntmountain.level;

import java.util.List;

public class FileLevelList implements ILevelList {

    private List<Level> levels;

    public FileLevelList(final List<Level> levels) {

        this.levels = levels;
    }

    @Override
    public Level getNextLevel(Level currentLevel) {

        Level nextLevel = null;
        if(levels != null) {

            int idx = levels.indexOf(currentLevel);
            if(idx >= 0 && idx < (levels.size() - 1)) {

                nextLevel = levels.get(idx + 1);
            }
        }

        return nextLevel;
    }

    @Override
    public boolean hasNextLevel(Level currentLevel) {

        int idx = levels.indexOf(currentLevel);
        return levels != null && idx >= 0 && idx < (levels.size() - 1);
    }
}
