package com.tomer.scoundrel.rules;

import com.tomer.scoundrel.model.GameState;

/** Computes the final score for a terminal state (Strategy — variants may replace it). */
public interface ScoringStrategy {

    /** Called with {@code status} already set to WON or LOST, {@code score} still null. */
    int score(GameState terminalState, Ruleset rules);
}
