package com.tomer.scoundrel.rules;

import com.tomer.scoundrel.model.Card;

/**
 * A player intention, reified as data (Command pattern). Sealed so the engine
 * can switch exhaustively; new special cards normally reuse an existing move.
 */
public sealed interface Move {

    /** Scoop the whole room to the bottom of the dungeon; the only non-card move. */
    record AvoidRoom() implements Move {
    }

    /** A move that resolves one specific card in the current room. */
    sealed interface CardMove extends Move {
        Card targetCard();
    }

    record TakeWeapon(Card targetCard) implements CardMove {
    }

    record TakePotion(Card targetCard) implements CardMove {
    }

    record FightBarehanded(Card targetCard) implements CardMove {
    }

    record FightWithWeapon(Card targetCard) implements CardMove {
    }
}
