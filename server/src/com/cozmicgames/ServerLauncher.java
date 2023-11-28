package com.cozmicgames;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.cozmicgames.server.Server;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class ServerLauncher {
    public static void main(String[] arg) {
        new HeadlessApplication(new Server());
    }
}
