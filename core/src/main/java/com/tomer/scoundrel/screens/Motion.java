package com.tomer.scoundrel.screens;

/** Pure motion arithmetic, kept free of Scene2D so it can be unit tested. */
final class Motion {

    private Motion() {
    }

    /**
     * Seconds from the start of a deal until the last card lands — the exact
     * span the input gate stays up. Each card after the first waits one more
     * stagger before flying, and every card flies for {@code flight}.
     */
    static float dealWindow(int cardCount, float baseDelay, float stagger, float flight) {
        if (cardCount <= 0) {
            return baseDelay;
        }
        return baseDelay + (cardCount - 1) * stagger + flight;
    }
}
