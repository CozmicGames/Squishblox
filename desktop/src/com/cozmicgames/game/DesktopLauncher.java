package com.cozmicgames.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;

import java.io.*;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		GameSettings gameSettings = new GameSettings();

		try (BufferedReader reader = new BufferedReader(new FileReader("settings.json"))) {
			StringBuilder builder = new StringBuilder();
			String line = reader.readLine();

			while (line != null) {
				builder.append(line);
				builder.append(System.lineSeparator());
				line = reader.readLine();
			}

			gameSettings.read(builder.toString());
		} catch (IOException e) {
		}

		try {
			Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
			config.setForegroundFPS(60);
			config.setTitle("Squishblox");
			config.setInitialBackgroundColor(Color.BLACK);
			config.setWindowIcon("branding/icon.png");

			if (gameSettings.isFullscreen())
				config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
			else
				config.setWindowedMode(gameSettings.getWidth(), gameSettings.getHeight());

			config.useVsync(gameSettings.getVsync());
			config.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.GL30, 3, 2);
			new Lwjgl3Application(new Game(gameSettings), config);
		} finally {
			try (Writer writer = new BufferedWriter(new FileWriter("settings.json", false))) {
				writer.write(gameSettings.write(true));
			} catch (IOException e) {
			}
		}
	}
}
