package com.tomer.scoundrel.runs;

import java.util.Comparator;
import java.util.List;
import java.util.OptionalInt;

/** Pure views over the run history; no I/O. */
public final class HighScores {

    private HighScores() {
    }

    /** Highest score first; ties go to the run that reached it first. */
    public static List<RunRecord> top(List<RunRecord> runs, int limit) {
        return runs.stream()
                .sorted(Comparator.comparingInt(RunRecord::score).reversed()
                        .thenComparing(RunRecord::endedAt))
                .limit(limit)
                .toList();
    }

    /** The best score on record; empty when no runs exist. */
    public static OptionalInt best(List<RunRecord> runs) {
        return runs.stream().mapToInt(RunRecord::score).max();
    }
}
