package com.tomer.scoundrel.achievements;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UnlockedAchievementTest {

    private static final Instant EARNED = Instant.parse("2026-07-10T09:30:00Z");

    @Test
    void encodeParseRoundTrips() {
        UnlockedAchievement original = new UnlockedAchievement("giant_slayer", EARNED);
        assertEquals(Optional.of(original), UnlockedAchievement.parse(original.toLine()));
    }

    @Test
    void unknownKeysAreIgnored() {
        String line = new UnlockedAchievement("first_blood", EARNED).toLine() + "\tnote=hello";
        assertEquals(Optional.of(new UnlockedAchievement("first_blood", EARNED)),
                UnlockedAchievement.parse(line));
    }

    @Test
    void unknownVersionsAndGarbageAreRejected() {
        String good = new UnlockedAchievement("speedrunner", EARNED).toLine();
        assertEquals(Optional.empty(), UnlockedAchievement.parse(good.replace("v=1", "v=2")));
        assertEquals(Optional.empty(), UnlockedAchievement.parse("### not a record ###"));
        assertEquals(Optional.empty(), UnlockedAchievement.parse(""));
        assertEquals(Optional.empty(), UnlockedAchievement.parse(good.replace("\tid=speedrunner", "")));
        assertEquals(Optional.empty(), UnlockedAchievement.parse(good.replace("\tearned=" + EARNED, "")));
    }

    @Test
    void constructorRejectsInvalidRecords() {
        assertThrows(IllegalArgumentException.class, () -> new UnlockedAchievement(null, EARNED));
        assertThrows(IllegalArgumentException.class, () -> new UnlockedAchievement("has space", EARNED));
        assertThrows(IllegalArgumentException.class, () -> new UnlockedAchievement("first_blood", null));
    }
}
