package com.deaddropgames.stuntmountain.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.deaddropgames.stuntmountain.web.LevelRepository;

import java.io.IOException;
import java.net.URISyntaxException;

public class LoginDialog extends BaseDialog {

    private static final String LOG = "LoginDialog";

    private TextField usernameTxt;
    private TextField passwordTxt;
    private Label msgLabel;

    public LoginDialog(Skin skin, TextureAtlas uiAtlas, float scaleFactor) {

        this("Please Login", skin, uiAtlas, scaleFactor);
    }

    private LoginDialog(String title, Skin skin, TextureAtlas uiAtlas, float scaleFactor) {

        super(title, skin, uiAtlas, scaleFactor);

        Table table  = new Table(skin);

        table.add(createLabel("Username:", skin)).space(20).right();
        table.add(usernameTxt = new TextField("", skin)).space(20).size(300*scaleFactor, 60*scaleFactor);
        table.row();

        table.add(createLabel("Password:", skin)).space(20).right();
        table.add(passwordTxt = new TextField("", skin)).space(20).size(300*scaleFactor, 60*scaleFactor);
        table.row();

        TextField.TextFieldStyle style = usernameTxt.getStyle();
        style.font = skin.getFont("button");
        style.fontColor = skin.getColor("black");
        usernameTxt.setStyle(style);
        passwordTxt.setStyle(style);

        msgLabel = createLabel("", skin);

        table.add(msgLabel).space(20).center().expandX().colspan(2);
        table.row();

        table.add(createLabel("New user?", skin));
        TextButton signUpButton = new TextButton("Sign up", skin);
        signUpButton.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {

                Gdx.net.openURI("https://deaddropgames.com/accounts/signup/");
            }
        });
        table.add(signUpButton).size(buttonWidth/2*scaleFactor, buttonHeight/2*scaleFactor).space(10).fill();
        table.row();

        table.add("").space(20).center().expandX().colspan(2);
        table.row();

        passwordTxt.setPasswordMode(true);
        passwordTxt.setPasswordCharacter('*');

        getContentTable().add(table);

        // add buttons
        TextButton cancelButton = new TextButton("Cancel", skin);
        button(cancelButton, Boolean.FALSE);

        TextButton loginButton = new TextButton("Login", skin);
        button(loginButton, Boolean.TRUE);
    }

    @Override
    protected void result(Object object) {

        // if they canceled, close window
        if(!(Boolean)object) {

            removeBackdrop();
            setVisible(false);
            return;
        }

        // attempt to login
        boolean success = false;
        try {

            success = LevelRepository.initToken(usernameTxt.getText(), passwordTxt.getText());
        } catch (IOException e) {

            Gdx.app.error(LOG, "Failed to initialize token", e);
        } catch (URISyntaxException e) {

            Gdx.app.error(LOG, "Failed to initialize token", e);
        }

        // if it failed - show an error message on the dialog
        if(!success) {

            msgLabel.setText("Invalid username or password");
            cancel();
            return;
        }

        removeBackdrop();
        setVisible(false);
    }
}
