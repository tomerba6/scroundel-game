package com.tomer.scoundrel.achievements;

import com.tomer.scoundrel.model.Status;
import com.tomer.scoundrel.runs.RunRecord;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.tomer.scoundrel.achievements.AchievementsTestData.context;
import static com.tomer.scoundrel.achievements.AchievementsTestData.run;
import static com.tomer.scoundrel.achievements.AchievementsTestData.summary;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AchievementsCatalogTest {

    private static Achievement byId(String id) {
        return Achievements.all().stream()
                .filter(a -> a.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no achievement with id " + id));
    }

    private static boolean earns(String id, AchievementContext context) {
        return byId(id).earnedBy(context);
    }

    @Test
    void catalogHasTenAchievementsWithUniqueIdsAndCopy() {
        List<Achievement> all = Achievements.all();
        assertEquals(10, all.size());
        Set<String> ids = new HashSet<>();
        for (Achievement a : all) {
            assertTrue(ids.add(a.id()), "duplicate id " + a.id());
            assertFalse(a.title().isBlank(), a.id() + " has a blank title");
            assertFalse(a.description().isBlank(), a.id() + " has a blank description");
        }
    }

    @Test
    void onlyRockBottomIsHidden() {
        for (Achievement a : Achievements.all()) {
            assertEquals(a.id().equals("rock_bottom"), a.hidden(), a.id());
        }
    }

    @Test
    void firstBloodOnTheFirstClearOnly() {
        RunSummary won = summary(Status.WON, 15, 15, 200, 0, 3, false);
        assertTrue(earns("first_blood", context(won, run(Status.WON, 15, 20))));
        // A second win does not re-earn it.
        assertFalse(earns("first_blood",
                context(won, run(Status.WON, 10, 18), run(Status.WON, 15, 20))));
        // A loss never earns it, even with a prior win in the history.
        RunSummary lost = summary(Status.LOST, -30, 0, 100, 0, 1, false);
        assertFalse(earns("first_blood",
                context(lost, run(Status.WON, 10, 18), run(Status.LOST, -30, 5))));
    }

    @Test
    void seasonedAtTenFinishedRuns() {
        RunSummary any = summary(Status.LOST, -5, 0, 60, 0, 0, false);
        assertTrue(earns("seasoned", context(any, runsOf(10))));
        assertFalse(earns("seasoned", context(any, runsOf(9))));
    }

    @Test
    void monsterHunterAtHundredLifetimeKills() {
        RunSummary any = summary(Status.WON, 10, 10, 60, 0, 0, false);
        assertTrue(earns("monster_hunter",
                context(any, run(Status.WON, 10, 60), run(Status.LOST, -5, 40))));
        assertFalse(earns("monster_hunter",
                context(any, run(Status.WON, 10, 60), run(Status.LOST, -5, 39))));
    }

    @Test
    void giantSlayerForABareHandedKingOrAceEvenInALoss() {
        assertTrue(earns("giant_slayer", context(summary(Status.LOST, -40, 0, 60, 13, 1, false))));
        assertTrue(earns("giant_slayer", context(summary(Status.WON, 5, 5, 60, 14, 2, false))));
        assertFalse(earns("giant_slayer", context(summary(Status.WON, 5, 5, 60, 12, 1, false))));
    }

    @Test
    void untarnishedForAFullHealthWin() {
        assertTrue(earns("untarnished", context(summary(Status.WON, 20, 20, 60, 0, 0, false))));
        assertFalse(earns("untarnished", context(summary(Status.WON, 19, 19, 60, 0, 0, false))));
    }

    @Test
    void onTheBrinkForAOneHealthWin() {
        assertTrue(earns("on_the_brink", context(summary(Status.WON, 1, 1, 60, 0, 0, false))));
        assertFalse(earns("on_the_brink", context(summary(Status.WON, 2, 2, 60, 0, 0, false))));
    }

    @Test
    void flawlessRoomRegardlessOfOutcome() {
        assertTrue(earns("flawless_room", context(summary(Status.LOST, -10, 0, 60, 0, 0, true))));
        assertFalse(earns("flawless_room", context(summary(Status.WON, 10, 10, 60, 0, 0, false))));
    }

    @Test
    void blademasterForAWinWithNoBareHandedKills() {
        assertTrue(earns("blademaster", context(summary(Status.WON, 8, 8, 120, 6, 0, false))));
        assertFalse(earns("blademaster", context(summary(Status.WON, 8, 8, 120, 6, 1, false))));
        assertFalse(earns("blademaster", context(summary(Status.LOST, -5, 0, 120, 0, 0, false))));
    }

    @Test
    void speedrunnerForAWinStrictlyUnderNinetySeconds() {
        assertTrue(earns("speedrunner", context(summary(Status.WON, 10, 10, 89, 0, 0, false))));
        assertFalse(earns("speedrunner", context(summary(Status.WON, 10, 10, 90, 0, 0, false))));
        assertFalse(earns("speedrunner", context(summary(Status.LOST, -5, 0, 10, 0, 0, false))));
    }

    @Test
    void rockBottomForACatastrophicLoss() {
        assertTrue(earns("rock_bottom", context(summary(Status.LOST, -150, 0, 60, 0, 0, false))));
        assertTrue(earns("rock_bottom", context(summary(Status.LOST, -200, 0, 60, 0, 0, false))));
        assertFalse(earns("rock_bottom", context(summary(Status.LOST, -149, 0, 60, 0, 0, false))));
        assertFalse(earns("rock_bottom", context(summary(Status.WON, 20, 20, 60, 0, 0, false))));
    }

    private static RunRecord[] runsOf(int count) {
        RunRecord[] runs = new RunRecord[count];
        for (int i = 0; i < count; i++) {
            runs[i] = run(Status.LOST, -5, 3);
        }
        return runs;
    }
}
