package com.tomer.scoundrel.rules;

import com.tomer.scoundrel.model.Card;

/** Test shorthand for standard cards: monsters are clubs (spades via monster2). */
final class Cards {

    static final DeckDefinition DECK = new StandardDeck();

    private Cards() {
    }

    static Card card(String id) {
        return DECK.definition(id).card();
    }

    static Card monster(int value) {
        return card(rank(value) + "C");
    }

    static Card monster2(int value) {
        return card(rank(value) + "S");
    }

    static Card weapon(int value) {
        return card(value + "D");
    }

    static Card potion(int value) {
        return card(value + "H");
    }

    private static String rank(int value) {
        return switch (value) {
            case 11 -> "J";
            case 12 -> "Q";
            case 13 -> "K";
            case 14 -> "A";
            default -> String.valueOf(value);
        };
    }
}
