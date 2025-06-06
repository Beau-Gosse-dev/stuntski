package com.deaddropgames.stuntmountain.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import java.util.HashSet;
import java.util.Set;

public class LevelProgress {
    
    private static final String PREFS_NAME = "stuntmountain_progress";
    private static final String COMPLETED_LEVELS_KEY = "completed_levels";
    private static final String SEPARATOR = ";";
    
    private Preferences prefs;
    private Set<String> completedLevels;
    
    public LevelProgress() {
        prefs = Gdx.app.getPreferences(PREFS_NAME);
        loadCompletedLevels();
    }
    
    private void loadCompletedLevels() {
        completedLevels = new HashSet<>();
        String saved = prefs.getString(COMPLETED_LEVELS_KEY, "");
        if (!saved.isEmpty()) {
            String[] levels = saved.split(SEPARATOR);
            for (String level : levels) {
                if (!level.trim().isEmpty()) {
                    completedLevels.add(level.trim());
                }
            }
        }
    }
    
    private void saveCompletedLevels() {
        StringBuilder sb = new StringBuilder();
        for (String level : completedLevels) {
            if (sb.length() > 0) {
                sb.append(SEPARATOR);
            }
            sb.append(level);
        }
        prefs.putString(COMPLETED_LEVELS_KEY, sb.toString());
        prefs.flush();
    }
    
    public void markLevelCompleted(String levelId) {
        if (levelId != null && !levelId.isEmpty()) {
            completedLevels.add(levelId);
            saveCompletedLevels();
        }
    }
    
    public boolean isLevelCompleted(String levelId) {
        return levelId != null && completedLevels.contains(levelId);
    }
    
    public void clearProgress() {
        completedLevels.clear();
        saveCompletedLevels();
    }
    
    public int getCompletedLevelCount() {
        return completedLevels.size();
    }
}