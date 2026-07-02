package com.tomer.scoundrel.rules;

/** Factory for the shipped rulesets. Base Scoundrel is the only one for now. */
public final class Rulesets {

    private Rulesets() {
    }

    public static Ruleset standard() {
        return new Ruleset(20, 20, 4, 3, 1,
                new StandardAvoidRule(), new StandardScoring(), new StandardDeck());
    }
}
