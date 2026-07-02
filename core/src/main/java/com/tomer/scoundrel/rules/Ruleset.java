package com.tomer.scoundrel.rules;

/**
 * Everything configurable about a game: constants plus the swappable
 * strategies. Variations and difficulty are different instances of this
 * record, not new engine code.
 */
public record Ruleset(
        int startingHealth,
        int healthCap,
        int roomSize,
        int cardsResolvedPerTurn,
        int potionsPerTurn,
        AvoidRule avoidRule,
        ScoringStrategy scoring,
        DeckDefinition deck) {
}
