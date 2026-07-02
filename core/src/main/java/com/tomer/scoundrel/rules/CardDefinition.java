package com.tomer.scoundrel.rules;

import com.tomer.scoundrel.model.Card;
import com.tomer.scoundrel.model.CardType;

/**
 * The data-driven description of a card: identity, stamped stats, and the
 * effect that gives it behavior. Adding a new card is a new definition plus
 * effect — never a change to the engine's turn loop.
 */
public record CardDefinition(String id, CardType type, int value, CardEffect effect) {

    /** The plain, serializable in-play card for this definition. */
    public Card card() {
        return new Card(id, type, value);
    }
}
