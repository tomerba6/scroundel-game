package com.tomer.scoundrel.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Cards resolve on press. Scene2D's ClickListener only fires when the release
 * lands back on the same actor, so clicks made while the mouse is already
 * travelling to the next card were being dropped — which is what fast play
 * looks like.
 */
class PressListenerTest {

    @Test
    void aPressActsImmediatelyAndConsumesTheEvent() {
        AtomicInteger presses = new AtomicInteger();
        InputListener listener = Widgets.pressListener(presses::incrementAndGet);
        assertTrue(listener.touchDown(new InputEvent(), 5, 5, 0, Input.Buttons.LEFT));
        assertEquals(1, presses.get());
    }

    @Test
    void aReleaseThatDriftsOffTheCardChangesNothing() {
        AtomicInteger presses = new AtomicInteger();
        InputListener listener = Widgets.pressListener(presses::incrementAndGet);
        InputEvent event = new InputEvent();
        listener.touchDown(event, 5, 5, 0, Input.Buttons.LEFT);
        listener.touchUp(event, 9999, -9999, 0, Input.Buttons.LEFT);
        assertEquals(1, presses.get(), "the press had already resolved the card");
    }

    @Test
    void everyPressActsEvenInRapidSuccession() {
        AtomicInteger presses = new AtomicInteger();
        InputListener listener = Widgets.pressListener(presses::incrementAndGet);
        for (int i = 0; i < 5; i++) {
            listener.touchDown(new InputEvent(), 5, 5, 0, Input.Buttons.LEFT);
        }
        assertEquals(5, presses.get());
    }

    @Test
    void onlyTheLeftButtonActs() {
        AtomicInteger presses = new AtomicInteger();
        InputListener listener = Widgets.pressListener(presses::incrementAndGet);
        assertFalse(listener.touchDown(new InputEvent(), 5, 5, 0, Input.Buttons.RIGHT));
        assertFalse(listener.touchDown(new InputEvent(), 5, 5, 0, Input.Buttons.MIDDLE));
        assertEquals(0, presses.get());
    }
}
