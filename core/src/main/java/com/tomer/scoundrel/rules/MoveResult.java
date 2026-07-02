package com.tomer.scoundrel.rules;

import com.tomer.scoundrel.model.GameState;

import java.util.List;

/**
 * The outcome of applying one move: the next state plus everything that
 * happened, in order. Observers (achievements, stats, UI) read the events;
 * they are never pushed from inside core.
 */
public record MoveResult(GameState state, List<GameEvent> events) {

    public MoveResult {
        events = List.copyOf(events);
    }
}
