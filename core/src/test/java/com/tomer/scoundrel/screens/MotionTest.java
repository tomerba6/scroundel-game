package com.tomer.scoundrel.screens;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The deal window is exactly how long the input gate covers the board, so it
 * is worth pinning: a long window is what made clicks feel dropped.
 */
class MotionTest {

    private static final float EPS = 1e-6f;

    @Test
    void oneCardLandsAfterASingleFlight() {
        assertEquals(0.18f, Motion.dealWindow(1, 0f, 0.04f, 0.18f), EPS);
    }

    @Test
    void eachCardAfterTheFirstAddsOneStagger() {
        assertEquals(0.22f, Motion.dealWindow(2, 0f, 0.04f, 0.18f), EPS);
        assertEquals(0.30f, Motion.dealWindow(4, 0f, 0.04f, 0.18f), EPS);
    }

    @Test
    void aBaseDelayShiftsTheWholeWindow() {
        // Avoiding sweeps the old room out first, then deals the new one in.
        assertEquals(0.50f, Motion.dealWindow(4, 0.20f, 0.04f, 0.18f), EPS);
    }

    @Test
    void anEmptyRoomJustWaitsOutTheBaseDelay() {
        assertEquals(0.20f, Motion.dealWindow(0, 0.20f, 0.04f, 0.18f), EPS);
        assertEquals(0f, Motion.dealWindow(0, 0f, 0.04f, 0.18f), EPS);
    }

    @Test
    void theShippedTokensKeepTheGateShort() {
        float deal = Motion.dealWindow(4, 0f, Theme.DEAL_STAGGER, Theme.DEAL_DURATION);
        float avoid = Motion.dealWindow(4, Theme.SWEEP_DURATION,
                Theme.DEAL_STAGGER, Theme.DEAL_DURATION);
        assertTrue(deal <= 0.30f, "deal gate was " + deal + "s");
        assertTrue(avoid <= 0.50f, "avoid gate was " + avoid + "s");
    }
}
