package com.deaddropgames.stuntmountain.util;

import java.util.LinkedList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;

public class GameMessageQueue {

    private LinkedList<String> msgs;
    private Label msgLabel;
    private long lastMsgTime;
    private final long MSG_TIMEOUT = 4000; // 4 seconds
    private final long FADE_DURATION = 1000; // 1 second fade
    private Color originalColor;
    private boolean isPersistentMessage = false;

    public GameMessageQueue(final String msg, final Skin skin) {

        msgs = new LinkedList<String>();
        originalColor = new Color(.25f, .25f, .25f, 1f);
        msgLabel = new Label(msg, new Label.LabelStyle(skin.getFont("button"), originalColor));
        msgLabel.setAlignment(Align.center);
        lastMsgTime = -1;
    }

    public void addDynamicMessage(final String msg, final long timeMs) {

        if(msg.length() == 0) {

            return;
        }

        if(lastMsgTime < 0 || timeMs - lastMsgTime > MSG_TIMEOUT) {

            msgLabel.setText(msg);
            msgLabel.setColor(originalColor); // Reset to full opacity
            lastMsgTime = timeMs;
            isPersistentMessage = false;
        } else {

            msgs.add(msg);
        }
    }
    
    public void addPersistentMessage(final String msg) {
        
        msgs.clear(); // Clear any queued messages
        msgLabel.setText(msg);
        msgLabel.setColor(originalColor); // Reset to full opacity
        lastMsgTime = -1; // No timeout
        isPersistentMessage = true;
    }

    public void setStaticMessage(final String msg) {

        msgs.clear();
        msgLabel.setText(msg);
        msgLabel.setColor(originalColor); // Reset to full opacity
        lastMsgTime = -1;
        isPersistentMessage = false;
    }

    public void clearMessages() {

        msgs.clear();
        setStaticMessage("");
    }

    public void updateMessages(final long timeMs) {

        // nothing to do...
        if(lastMsgTime < 0 || isPersistentMessage) {

            return;
        }

        long elapsed = timeMs - lastMsgTime;
        
        // Apply fade-out effect in the last second
        if(elapsed > (MSG_TIMEOUT - FADE_DURATION) && elapsed <= MSG_TIMEOUT) {
            float fadeProgress = (float)(elapsed - (MSG_TIMEOUT - FADE_DURATION)) / FADE_DURATION;
            float alpha = 1.0f - fadeProgress;
            msgLabel.setColor(originalColor.r, originalColor.g, originalColor.b, alpha);
        }

        if(elapsed > MSG_TIMEOUT) {

            if(msgs.size() > 0) {

                msgLabel.setText(msgs.remove());
                msgLabel.setColor(originalColor); // Reset to full opacity
                lastMsgTime = timeMs;
                isPersistentMessage = false;
            } else {

                // no messages to display, so clear it out
                msgLabel.setText("");
                msgLabel.setColor(originalColor); // Reset to full opacity
                lastMsgTime = -1;
            }
        }
    }

    public Label getMsgLabel() {

        return msgLabel;
    }
}
