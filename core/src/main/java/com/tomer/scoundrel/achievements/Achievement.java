package com.tomer.scoundrel.achievements;

import java.util.function.Predicate;

/**
 * One achievement, authored in code as data: a stable {@code id} (the
 * persistence key), a player-facing {@code title} and {@code description},
 * whether it stays {@code hidden} until earned, and the {@code rule} that
 * decides — from a finished run and the full history — whether it is now
 * earned. New achievements are new entries in {@link Achievements}, with no
 * engine changes; the shape mirrors the data-driven card definitions.
 */
public record Achievement(String id, String title, String description, boolean hidden,
                          Predicate<AchievementContext> rule) {

    public boolean earnedBy(AchievementContext context) {
        return rule.test(context);
    }
}
