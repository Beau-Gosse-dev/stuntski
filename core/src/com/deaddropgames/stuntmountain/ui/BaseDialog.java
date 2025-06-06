package com.deaddropgames.stuntmountain.ui;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;


import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

class BaseDialog extends Dialog {

    final static int buttonWidth = 300;
    final static int buttonHeight = 80;

    private TextureAtlas uiAtlas;
    private Table backDrop;
    private Stage stage;
    protected float scaleFactor;

    BaseDialog(String title, Skin skin, TextureAtlas uiAtlas, float scaleFactor) {

        super(title, skin);

        this.uiAtlas = uiAtlas;
        this.scaleFactor = scaleFactor;

        Label.LabelStyle titleStyle = getTitleLabel().getStyle();
        titleStyle.font = skin.getFont("button");
        getTitleLabel().setStyle(titleStyle);
    }

    Label createLabel(String text, Skin skin) {

        return new Label(text, new Label.LabelStyle(skin.getFont("button"), skin.getColor("black")));
    }

    @Override
    public Dialog button (Button button, Object object) {

        getButtonTable().add(button).size(buttonWidth*scaleFactor, buttonHeight*scaleFactor).space(10);
        setObject(button, object);
        return this;
    }

    @Override
    public Dialog show (Stage stage) {

        this.stage = stage;

        removeBackdrop();
        backDrop = new Table(getSkin());
        backDrop.setBackground(new NinePatchDrawable(uiAtlas.createPatch("background")));
        backDrop.setSize(stage.getWidth(), stage.getHeight());
        backDrop.center();
        stage.addActor(backDrop);

        show(stage, sequence(Actions.alpha(0), Actions.fadeIn(0.4f, Interpolation.fade)));

        // the main reason to override is to show the dialog closer to the top so the keyboard doesn't cover it
        setPosition(Math.round((stage.getWidth() - getWidth()) / 2), stage.getHeight() - getHeight() - 50);
        return this;
    }

    void removeBackdrop() {

        if(backDrop != null) {

            stage.getActors().removeValue(backDrop, true);
            backDrop = null;
        }
    }
}
