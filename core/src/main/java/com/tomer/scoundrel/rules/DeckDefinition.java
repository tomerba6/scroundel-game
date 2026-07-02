package com.tomer.scoundrel.rules;

import java.util.List;

/** The set of card definitions a game is played with (Factory for the dungeon). */
public interface DeckDefinition {

    /** All definitions, in canonical (unshuffled) order. */
    List<CardDefinition> cards();

    /** Looks up behavior for an in-play card; ids must be unique within a deck. */
    default CardDefinition definition(String cardId) {
        return cards().stream()
                .filter(def -> def.id().equals(cardId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown card id: " + cardId));
    }
}
