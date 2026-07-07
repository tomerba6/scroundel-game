package com.tomer.scoundrel.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

/** Tiny shared widget helpers for the screens package. */
final class Widgets {

    private Widgets() {
    }

    static Label label(String text, BitmapFont font, Color color) {
        return new Label(text, new Label.LabelStyle(font, color));
    }

    static Color dim(Color color, float alpha) {
        Color dimmed = new Color(color);
        dimmed.a = alpha;
        return dimmed;
    }
}
