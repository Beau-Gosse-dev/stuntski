package com.deaddropgames.stuntmountain.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.deaddropgames.stuntmountain.sim.ControlArea;
import com.deaddropgames.stuntmountain.StuntMountain;

public class SettingsScreen extends AbstractScreen {

    private static final String LOG = "SettingsScreen";

    SettingsScreen(StuntMountain game) {

        super(game);

        Gdx.app.debug(LOG, "SettingsScreen()");
    }

    @Override
    public void show() {

        Gdx.app.debug(LOG, "show()");
        super.show();

        Skin skin = super.getSkin();

        Table table = new Table(skin);
        table.setWidth(stage.getWidth());
        table.setHeight(stage.getHeight());
        
        table.add(createTitleLabel("Game Settings")).spaceBottom(20 * game.scaleFactor);
        table.row();
        
        final String rightStr = "Right";
        final String leftStr = "Left";
        //final String fullStr = "Full";

        Table joystickTable = new Table(skin);
        TextButton joystickRightBtn = new TextButton(rightStr, skin);
        joystickRightBtn.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {

                game.levelSettings.defaultControlAreaType = ControlArea.RIGHTBOTTOM;
                game.preferences.putInteger("controlAreaType", ControlArea.RIGHTBOTTOM);

            }
        });
        TextButton joystickLeftBtn = new TextButton(leftStr, skin);
        joystickLeftBtn.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {

                game.levelSettings.defaultControlAreaType = ControlArea.LEFTBOTTOM;
                game.preferences.putInteger("controlAreaType", ControlArea.LEFTBOTTOM);
            }
        });
        
        /*TextButton fullScreenBtn = new TextButton(fullStr, skin);
        fullScreenBtn.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {

                game.levelSettings.defaultControlAreaType = ControlArea.FULLSCREEN;
                game.preferences.putInteger("controlAreaType", ControlArea.FULLSCREEN);
            }
        });*/

        ButtonGroup<TextButton> joystickRadio = new ButtonGroup<TextButton>(joystickRightBtn, joystickLeftBtn);
        
        // make our 'checked' buttons stand out
        joystickRightBtn.getStyle().checkedFontColor = Color.WHITE;
        
        int controlAreaType = game.preferences.getInteger("controlAreaType", game.levelSettings.defaultControlAreaType);
        if(controlAreaType == ControlArea.RIGHTBOTTOM) {

            joystickRadio.setChecked(rightStr);
        } else if(controlAreaType == ControlArea.LEFTBOTTOM) {

            joystickRadio.setChecked(leftStr);
        } else {

            joystickRadio.setChecked(rightStr);
        }
        
        joystickTable.add(createButtonLabel("Joystick Position:")).size(150 * game.scaleFactor, 60 * game.scaleFactor).spaceRight(5 * game.scaleFactor);
        joystickTable.add(joystickLeftBtn).size(100 * game.scaleFactor, 60 * game.scaleFactor);
        joystickTable.add(joystickRightBtn).size(100 * game.scaleFactor, 60 * game.scaleFactor);
        //joystickTable.add(fullScreenBtn).size(100 * game.scaleFactor, 60 * game.scaleFactor);

        table.add(joystickTable).spaceBottom(10 * game.scaleFactor);
        table.row();
        
        // Units
        Table unitsTable = new Table(skin);
        
        TextButton metricBtn = new TextButton("Metric", skin);
        metricBtn.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {

                game.preferences.putBoolean("metric", true);

            }
        });
        TextButton imperialBtn = new TextButton("Imperial", skin);
        imperialBtn.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {

                game.preferences.putBoolean("metric", false);
            }
        });
        
        ButtonGroup<TextButton> unitsRadio = new ButtonGroup<TextButton>(metricBtn, imperialBtn);
        if(game.preferences.getBoolean("metric", true)) {

            unitsRadio.setChecked("Metric");
        } else {

            unitsRadio.setChecked("Imperial");
        }
        
        unitsTable.add(createButtonLabel("Units:")).size(150 * game.scaleFactor, 60 * game.scaleFactor).spaceRight(5 * game.scaleFactor);
        unitsTable.add(metricBtn).size(100 * game.scaleFactor, 60 * game.scaleFactor);
        unitsTable.add(imperialBtn).size(100 * game.scaleFactor, 60 * game.scaleFactor);
        
        table.add(unitsTable).spaceBottom(10 * game.scaleFactor);
        table.row();
        
        // audio
        Table audioTable = new Table(skin);
        
        Slider fxSlider = new Slider(0f, 1.0f, 0.1f, false, skin);
        fxSlider.setValue(game.preferences.getFloat("fxVolume", 0.5f));
        fxSlider.addCaptureListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {

                game.preferences.putFloat("fxVolume", ((Slider)actor).getValue());
            }
        });
        
        audioTable.add(createButtonLabel("FX Volume:")).size(150 * game.scaleFactor, 60 * game.scaleFactor).spaceRight(5 * game.scaleFactor);
        audioTable.add(fxSlider).size(200 * game.scaleFactor, 60 * game.scaleFactor);
        audioTable.row();
        
//        Slider musicSlider = new Slider(0f, 1.0f, 0.1f, false, skin);
//        musicSlider.setValue(game.preferences.getFloat("musicVolume", 0.5f));
//        musicSlider.addCaptureListener(new ChangeListener() {
//
//            @Override
//            public void changed(ChangeEvent event, Actor actor) {
//
//                game.preferences.putFloat("musicVolume", ((Slider)actor).getValue());
//            }
//        });
//
//        audioTable.add("Music Volume:").size(150 * game.scaleFactor, 60 * game.scaleFactor).spaceRight(5 * game.scaleFactor);
//        audioTable.add(musicSlider).size(200 * game.scaleFactor, 60 * game.scaleFactor);

//        CheckBox muteSoundCheckBox = new CheckBox("    Mute all sound", skin);
//        muteSoundCheckBox.setChecked(game.preferences.getBoolean("mute"));
//        muteSoundCheckBox.addCaptureListener(new ChangeListener() {
//
//            @Override
//            public void changed(ChangeEvent event, Actor actor) {
//
//                game.preferences.putBoolean("mute", ((CheckBox)actor).isChecked());
//            }
//        });
//        audioTable.add(muteSoundCheckBox).colspan(2);
//
//        table.add(audioTable).spaceBottom(10 * game.scaleFactor);
//        table.row();
        
        ScrollPane scrollPane = new ScrollPane(table, skin);
        scrollPane.setWidth(stage.getWidth());
        scrollPane.setHeight(stage.getHeight());
        stage.addActor(scrollPane);

        createBackButton();
        backButton.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {

                game.preferences.flush();
                game.setScreen(new MenuScreen(game));
            }
        });
    }

    @Override
    public boolean keyUp(int keycode) {

        Gdx.app.debug(LOG, "keyUp(" + keycode + ")");
        switch(keycode) {

        case Keys.ENTER:
        case Keys.BACK: { // enter key

            game.preferences.flush();
            game.setScreen(new MenuScreen(game));
            break;
        }

        default:
            break;
        }

        return super.keyUp(keycode);
    }
}
