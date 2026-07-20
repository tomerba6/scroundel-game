package com.tomer.scoundrel.achievements;

import com.tomer.scoundrel.runs.RunRecord;
import com.tomer.scoundrel.runs.RunTotals;

import java.util.List;

/**
 * Everything an achievement rule may read: the just-finished run's rich
 * {@link RunSummary}, and the full run history including that run as its last
 * element, so milestone rules can sum lifetime totals. Progress is derived
 * here, never stored — see {@link RunTotals}.
 */
public record AchievementContext(RunSummary run, List<RunRecord> history) {

    public AchievementContext {
        history = List.copyOf(history);
    }

    public RunTotals totals() {
        return RunTotals.of(history);
    }
}
