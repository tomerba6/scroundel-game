package com.tomer.scoundrel;

import com.badlogic.gdx.Game;
import com.tomer.scoundrel.runs.RunLog;
import com.tomer.scoundrel.screens.GameScreen;
import com.tomer.scoundrel.screens.Theme;

import java.nio.file.Path;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class ScoundrelGame extends Game {

    private Theme theme;

    @Override
    public void create() {
        theme = new Theme();
        RunLog runLog = new RunLog(
                Path.of(System.getProperty("user.home"), ".scoundrel", "runs.log"));
        setScreen(new GameScreen(theme, runLog));
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
