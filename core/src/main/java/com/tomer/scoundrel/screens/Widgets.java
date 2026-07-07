package com.tomer.scoundrel.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

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

    /** The one button style: torchlight when enabled, stone when not. */
    static TextButton torchButton(Theme theme, String text) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = theme.bodyBold;
        style.up = theme.solid(Theme.TORCHLIGHT);
        style.fontColor = Theme.SOOT;
        style.disabled = theme.solid(Theme.STONE);
        style.disabledFontColor = dim(Theme.BONE, 0.35f);
        TextButton button = new TextButton(text, style);
        button.pad(6, 20, 6, 20);
        return button;
    }
}
