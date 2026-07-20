package com.tomer.scoundrel.achievements;

import com.tomer.scoundrel.model.Status;
import com.tomer.scoundrel.rules.GameEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.tomer.scoundrel.achievements.AchievementsTestData.FULL_ROOM;
import static com.tomer.scoundrel.achievements.AchievementsTestData.monster;
import static com.tomer.scoundrel.achievements.AchievementsTestData.potion;
import static com.tomer.scoundrel.achievements.AchievementsTestData.result;
import static com.tomer.scoundrel.achievements.AchievementsTestData.weapon;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AchievementTrackerTest {

    @Test
    void capturesOutcomeScoreHealthAndSeconds() {
        AchievementTracker tracker = new AchievementTracker(FULL_ROOM);
        tracker.observe(result(12, new GameEvent.GameWon(12)));

        assertTrue(tracker.isFinished());
        RunSummary summary = tracker.toSummary(87);
        assertEquals(Status.WON, summary.outcome());
        assertEquals(12, summary.score());
        assertEquals(12, summary.finalHealth());
        assertEquals(87, summary.seconds());
    }

    @Test
    void finalHealthIsTheLastObservedState() {
        AchievementTracker tracker = new AchievementTracker(FULL_ROOM);
        tracker.observe(result(20, new GameEvent.MonsterDefeated(monster("6C", 6), false, 6)));
        tracker.observe(result(14, new GameEvent.MonsterDefeated(monster("2S", 2), false, 2)));
        tracker.observe(result(1, new GameEvent.GameWon(1)));

        assertEquals(1, tracker.toSummary(30).finalHealth());
    }

    @Test
    void tracksHighestAndCountOfBareHandedKillsOnly() {
        AchievementTracker tracker = new AchievementTracker(FULL_ROOM);
        tracker.observe(result(7, new GameEvent.MonsterDefeated(monster("KC", 13), false, 13)));
        // A weapon kill must not count toward the bare-handed tallies.
        tracker.observe(result(3,
                new GameEvent.MonsterDefeated(monster("AS", 14), true, 4),
                new GameEvent.WeaponDegraded(weapon(10), 14)));
        tracker.observe(result(0,
                new GameEvent.MonsterDefeated(monster("3C", 3), false, 3),
                new GameEvent.GameLost(-50)));

        RunSummary summary = tracker.toSummary(60);
        assertEquals(13, summary.highestBarehandedKill());
        assertEquals(2, summary.barehandedKillCount());
    }

    @Test
    void aFullRoomResolvedWithoutDamageIsFlawless() {
        AchievementTracker tracker = new AchievementTracker(FULL_ROOM);
        tracker.observe(result(20, new GameEvent.WeaponEquipped(weapon(10))));
        tracker.observe(result(20,
                new GameEvent.MonsterDefeated(monster("5C", 5), true, 0),
                new GameEvent.WeaponDegraded(weapon(10), 5)));
        tracker.observe(result(20,
                new GameEvent.PotionUsed(potion(4), 0),
                new GameEvent.RoomDealt(List.of())));
        tracker.observe(result(20, new GameEvent.GameWon(20)));

        assertTrue(tracker.toSummary(40).flawlessRoom());
    }

    @Test
    void aRoomInWhichAnyDamageWasTakenIsNotFlawless() {
        AchievementTracker tracker = new AchievementTracker(FULL_ROOM);
        tracker.observe(result(14, new GameEvent.MonsterDefeated(monster("6C", 6), false, 6)));
        tracker.observe(result(14, new GameEvent.WeaponEquipped(weapon(8))));
        tracker.observe(result(14,
                new GameEvent.PotionUsed(potion(3), 0),
                new GameEvent.RoomDealt(List.of())));
        tracker.observe(result(14, new GameEvent.GameWon(14)));

        assertFalse(tracker.toSummary(40).flawlessRoom());
    }

    @Test
    void anAvoidedRoomIsNotFlawlessDespiteTakingNoDamage() {
        AchievementTracker tracker = new AchievementTracker(FULL_ROOM);
        tracker.observe(result(20,
                new GameEvent.RoomAvoided(List.of()),
                new GameEvent.RoomDealt(List.of())));
        tracker.observe(result(20, new GameEvent.GameWon(20)));

        assertFalse(tracker.toSummary(40).flawlessRoom());
    }

    @Test
    void aDamagelessRoomWithNoMonsterDoesNotCount() {
        AchievementTracker tracker = new AchievementTracker(FULL_ROOM);
        tracker.observe(result(20, new GameEvent.WeaponEquipped(weapon(5))));
        tracker.observe(result(20, new GameEvent.WeaponEquipped(weapon(6))));
        tracker.observe(result(20,
                new GameEvent.PotionUsed(potion(4), 4),
                new GameEvent.RoomDealt(List.of())));
        tracker.observe(result(20, new GameEvent.GameWon(20)));

        assertFalse(tracker.toSummary(40).flawlessRoom());
    }

    @Test
    void toSummaryBeforeTheGameEndsThrows() {
        AchievementTracker tracker = new AchievementTracker(FULL_ROOM);
        tracker.observe(result(20, new GameEvent.PotionUsed(potion(5), 5)));

        assertFalse(tracker.isFinished());
        assertThrows(IllegalStateException.class, () -> tracker.toSummary(10));
    }
}
