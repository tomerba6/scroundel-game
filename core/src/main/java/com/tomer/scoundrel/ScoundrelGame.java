package com.tomer.scoundrel;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.tomer.scoundrel.runs.RunLog;
import com.tomer.scoundrel.screens.GameScreen;
import com.tomer.scoundrel.screens.RecordsScreen;
import com.tomer.scoundrel.screens.Theme;
import com.tomer.scoundrel.screens.TitleScreen;

import java.nio.file.Path;

/**
 * {@link com.badlogic.gdx.ApplicationListener} shared by all platforms, and
 * the app's navigator: screens ask it to switch, it owns the shared Theme
 * and RunLog and disposes whichever screen is being left.
 */
public class ScoundrelGame extends Game {

    private Theme theme;
    private RunLog runLog;

    @Override
    public void create() {
        theme = new Theme();
        runLog = new RunLog(
                Path.of(System.getProperty("user.home"), ".scoundrel", "runs.log"));
        showTitle();
    }

    public void showTitle() {
        switchTo(new TitleScreen(this, theme));
    }

    public void showGame() {
        switchTo(new GameScreen(this, theme, runLog));
    }

    public void showRecords() {
        switchTo(new RecordsScreen(this, theme, runLog));
    }

    /** setScreen only hides the previous screen; it must also be disposed. */
    private void switchTo(Screen next) {
        Screen previous = getScreen();
        setScreen(next);
        if (previous != null) {
            previous.dispose();
        }
    }

    @Override
    public void dispose() {
        super.dispose(); // hides the current screen
        if (getScreen() != null) {
            getScreen().dispose();
        }
        theme.dispose();
    }
}
