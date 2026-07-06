package com.tomer.scoundrel.runs;

import com.tomer.scoundrel.model.Status;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HighScoresTest {

    private static RunRecord run(int score, String endedAt) {
        return new RunRecord(null, "standard", score >= 0 ? Status.WON : Status.LOST,
                score, Instant.parse(endedAt), 60, 0, 0, 0, 0, 0, 0, 0);
    }

    @Test
    void topSortsByScoreDescendingAndLimits() {
        RunRecord low = run(-24, "2026-07-06T10:00:00Z");
        RunRecord mid = run(11, "2026-07-06T11:00:00Z");
        RunRecord high = run(23, "2026-07-06T12:00:00Z");
        assertEquals(List.of(high, mid), HighScores.top(List.of(low, mid, high), 2));
    }

    @Test
    void tiesGoToTheEarlierRun() {
        RunRecord later = run(20, "2026-07-06T12:00:00Z");
        RunRecord earlier = run(20, "2026-07-06T10:00:00Z");
        assertEquals(List.of(earlier, later), HighScores.top(List.of(later, earlier), 5));
    }

    @Test
    void bestIsEmptyWithoutRunsAndMaxWithThem() {
        assertEquals(OptionalInt.empty(), HighScores.best(List.of()));
        assertEquals(OptionalInt.of(23),
                HighScores.best(List.of(run(-24, "2026-07-06T10:00:00Z"), run(23, "2026-07-06T11:00:00Z"))));
    }
}
