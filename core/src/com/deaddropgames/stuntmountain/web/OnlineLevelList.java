package com.deaddropgames.stuntmountain.web;


import com.badlogic.gdx.Gdx;
import com.deaddropgames.stuntmountain.level.ILevelList;
import com.deaddropgames.stuntmountain.level.Level;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Locale;

public class OnlineLevelList implements ILevelList {

    private static final String LOG = "OnlineLevelList";

    private LevelSummaryResult[] results;
    private long currentId;

    public OnlineLevelList(final LevelSummaryResult[] results, long currentId) {

        this.results = results;
        this.currentId = currentId;
    }

    @Override
    public Level getNextLevel(Level currentLevel) {

        Level nextLevel = null;
        if (results != null) {

            int idx = findIndexById();
            if(idx >= 0 && idx < (results.length - 1)) {

                // get the next level in the list
                currentId = results[idx + 1].id;
                try {

                    nextLevel = LevelRepository.getLevel(currentId);
                    nextLevel.id = currentId;
                } catch (IOException e) {

                    Gdx.app.error(LOG, String.format(Locale.getDefault(), "Failed to fetch level %d",
                            currentId), e);
                } catch (URISyntaxException e) {

                    Gdx.app.error(LOG, String.format(Locale.getDefault(), "Failed to fetch level %d",
                            currentId), e);
                }
            }
        }

        return nextLevel;
    }

    @Override
    public boolean hasNextLevel(Level currentLevel) {

        int idx = findIndexById();
        return results != null && idx >= 0 && idx < (results.length - 1);
    }

    private int findIndexById() {

        for (int ii = 0; ii < results.length; ii++) {

            if (results[ii].id == currentId) {

                return ii;
            }
        }

        return -1;
    }
}
