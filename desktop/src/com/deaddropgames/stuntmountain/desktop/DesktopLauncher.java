package com.deaddropgames.stuntmountain.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.deaddropgames.stuntmountain.StuntMountain;

public class DesktopLauncher {
    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "StuntSki Lite";
        config.width = 1280;
        config.height = 720;
        new LwjglApplication(new StuntMountain(arg), config);
    }
}
