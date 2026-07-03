package com.tomer.scoundrel;

import com.badlogic.gdx.Game;
import com.tomer.scoundrel.screens.GameScreen;
import com.tomer.scoundrel.screens.Theme;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class ScoundrelGame extends Game {

    private Theme theme;

    @Override
    public void create() {
        theme = new Theme();
        setScreen(new GameScreen(theme));
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
