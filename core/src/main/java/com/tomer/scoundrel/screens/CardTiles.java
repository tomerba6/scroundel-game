package com.tomer.scoundrel.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.tomer.scoundrel.model.Card;
import com.tomer.scoundrel.model.CardType;

import static com.tomer.scoundrel.screens.Widgets.dim;
import static com.tomer.scoundrel.screens.Widgets.label;

/**
 * Builds the typed card tiles. One builder feeds both the interactive board
 * tiles and the Choreographer's cosmetic flight proxies, so they can never
 * drift apart visually.
 */
final class CardTiles {

    private CardTiles() {
    }

    static Table build(Theme theme, Card card) {
        Color background = roleColor(card.type());
        Color text = card.type() == CardType.WEAPON ? Theme.SOOT : Theme.BONE;
        Table tile = new Table();
        makeWholeFaceHittable(tile);
        tile.setBackground(theme.solid(background));
        tile.add(label(card.type().name(), theme.small, dim(text, 0.7f))).padTop(12);
        tile.row();
        tile.add(label(String.valueOf(card.value()), theme.display, text)).expand();
        tile.row();
        tile.add(corner(theme, card, text)).right().padRight(12).padBottom(10);
        return tile;
    }

    /**
     * Scene2D's {@link Table} defaults to {@link Touchable#childrenOnly}, so a
     * table is never itself a hit target — only its children are. Left alone, a
     * card would only respond where a label's glyphs happen to cover the pixel,
     * and presses anywhere else on its face would silently vanish.
     */
    static void makeWholeFaceHittable(Actor tile) {
        tile.setTouchable(Touchable.enabled);
    }

    static Color roleColor(CardType type) {
        return switch (type) {
            case MONSTER -> Theme.DRIED_BLOOD;
            case WEAPON -> Theme.IRON;
            case POTION -> Theme.HERBAL;
        };
    }

    /** Rank + suit identity, bottom-right like a playing card index. */
    private static Table corner(Theme theme, Card card, Color text) {
        Table corner = new Table();
        String id = card.id();
        char suit = id.charAt(id.length() - 1);
        if (id.length() >= 2 && "SHDC".indexOf(suit) >= 0) {
            corner.add(label(id.substring(0, id.length() - 1), theme.bodyBold, text)).padRight(4);
            corner.add(new Image(theme.suitIcon(suit, text))).size(14, 14);
        }
        return corner;
    }
}
