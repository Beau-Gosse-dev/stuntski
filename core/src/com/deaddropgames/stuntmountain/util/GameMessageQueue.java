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
    private final long MSG_TIMEOUT = 2000; // 2 seconds

    public GameMessageQueue(final String msg, final Skin skin) {

        msgs = new LinkedList<String>();
        msgLabel = new Label(msg, new Label.LabelStyle(skin.getFont("button"), new Color(.25f, .25f, .25f, 1f)));
        msgLabel.setAlignment(Align.center);
        lastMsgTime = -1;
    }

    public void addDynamicMessage(final String msg, final long timeMs) {

        if(msg.length() == 0) {

            return;
        }

        if(lastMsgTime < 0 || timeMs - lastMsgTime > MSG_TIMEOUT) {

            msgLabel.setText(msg);
            lastMsgTime = timeMs;
        } else {

            msgs.add(msg);
        }
    }

    public void setStaticMessage(final String msg) {

        msgs.clear();
        msgLabel.setText(msg);
        lastMsgTime = -1;
    }

    public void clearMessages() {

        msgs.clear();
        setStaticMessage("");
    }

    public void updateMessages(final long timeMs) {

        // nothing to do...
        if(lastMsgTime < 0) {

            return;
        }

        if(timeMs - lastMsgTime > MSG_TIMEOUT) {

            if(msgs.size() > 0) {

                msgLabel.setText(msgs.remove());
                lastMsgTime = timeMs;
            } else {

                // no messages to display, so clear it out
                msgLabel.setText("");
                lastMsgTime = -1;
            }
        }
    }

    public Label getMsgLabel() {

        return msgLabel;
    }
}
