package com.tomer.scoundrel.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * A card must be clickable across its whole face. Scene2D's Table — which a
 * card tile is — defaults to {@code childrenOnly}, meaning the tile itself is
 * never a hit target and only its label glyphs are clickable; a press on the
 * blank part of a card silently vanished. These assertions run on
 * {@code Actor.hit}, the very method Table inherits to answer that question.
 */
class CardTileHitAreaTest {

    private static Actor cardSized() {
        Actor actor = new Actor();
        actor.setSize(Theme.CARD_WIDTH, Theme.CARD_HEIGHT);
        return actor;
    }

    @Test
    void aChildrenOnlyTileIsInvisibleToHitTesting() {
        Actor tile = cardSized();
        tile.setTouchable(Touchable.childrenOnly); // what Table gives you for free
        assertNull(tile.hit(85, 120, true), "dead centre of the card hits nothing");
        assertNull(tile.hit(2, 2, true));
    }

    @Test
    void aCardTileIsHittableEverywhereOnItsFace() {
        Actor tile = cardSized();
        tile.setTouchable(Touchable.childrenOnly);
        CardTiles.makeWholeFaceHittable(tile);

        assertEquals(Touchable.enabled, tile.getTouchable());
        assertSame(tile, tile.hit(85, 120, true), "centre");
        assertSame(tile, tile.hit(2, 2, true), "bottom-left, far from any glyph");
        assertSame(tile, tile.hit(168, 238, true), "top-right, far from any glyph");
    }

    @Test
    void pointsOutsideTheCardStillMissIt() {
        Actor tile = cardSized();
        CardTiles.makeWholeFaceHittable(tile);
        assertNull(tile.hit(-1, 120, true));
        assertNull(tile.hit(85, Theme.CARD_HEIGHT + 1, true));
    }
}
