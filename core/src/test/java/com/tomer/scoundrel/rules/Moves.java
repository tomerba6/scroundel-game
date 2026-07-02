package com.tomer.scoundrel.rules;

import com.tomer.scoundrel.model.Card;
import com.tomer.scoundrel.model.GameState;

import java.util.List;

/** Test shorthand for querying the engine's move surface. */
final class Moves {

    private Moves() {
    }

    /** Moves the engine offers for one specific room card. */
    static List<Move> movesFor(ScoundrelEngine engine, GameState state, Card card) {
        return engine.legalMoves(state).stream()
                .filter(m -> m instanceof Move.CardMove cm && cm.targetCard().equals(card))
                .toList();
    }
}
