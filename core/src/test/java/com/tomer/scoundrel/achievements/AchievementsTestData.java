package com.tomer.scoundrel.achievements;

import com.tomer.scoundrel.model.Card;
import com.tomer.scoundrel.model.CardType;
import com.tomer.scoundrel.model.GameState;
import com.tomer.scoundrel.model.Status;
import com.tomer.scoundrel.rules.GameEvent;
import com.tomer.scoundrel.rules.MoveResult;
import com.tomer.scoundrel.runs.RunRecord;

import java.time.Instant;
import java.util.List;

/** Fixtures shared by the achievement tests. */
final class AchievementsTestData {

    /** The standard ruleset's cardsResolvedPerTurn. */
    static final int FULL_ROOM = 3;

    private AchievementsTestData() {
    }

    /** A MoveResult whose only state fact the tracker reads is health. */
    static MoveResult result(int health, GameEvent... events) {
        GameState state = new GameState(
                List.of(), List.of(), null, health, 0, 0, false, null, Status.IN_PROGRESS, null);
        return new MoveResult(state, List.of(events));
    }

    static Card monster(String id, int value) {
        return new Card(id, CardType.MONSTER, value);
    }

    static Card weapon(int value) {
        return new Card(value + "D", CardType.WEAPON, value);
    }

    static Card potion(int value) {
        return new Card(value + "H", CardType.POTION, value);
    }

    static RunSummary summary(Status outcome, int score, int finalHealth, long seconds,
                              int highestBarehandedKill, int barehandedKillCount, boolean flawlessRoom) {
        return new RunSummary(outcome, score, finalHealth, seconds,
                highestBarehandedKill, barehandedKillCount, flawlessRoom);
    }

    static RunRecord run(Status outcome, int score, int monsters) {
        return new RunRecord(1L, "standard", outcome, score,
                Instant.parse("2026-07-06T10:00:00Z"), 120,
                monsters, 0, 0, 0, 0, 0, 0);
    }

    static AchievementContext context(RunSummary run, RunRecord... history) {
        return new AchievementContext(run, List.of(history));
    }
}
