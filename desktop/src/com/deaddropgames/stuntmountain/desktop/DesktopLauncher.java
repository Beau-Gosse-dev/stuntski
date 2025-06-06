package com.deaddropgames.stuntmountain.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.deaddropgames.stuntmountain.StuntMountain;

public class DesktopLauncher {
    public static void main (String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("StuntSki Lite");
        config.setWindowedMode(1280, 720);
        new Lwjgl3Application(new StuntMountain(arg), config);
    }
}
