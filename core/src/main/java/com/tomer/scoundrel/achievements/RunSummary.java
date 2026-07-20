package com.tomer.scoundrel.achievements;

import com.tomer.scoundrel.model.Status;

/**
 * The notable facts of one finished game, richer than a persisted
 * {@link com.tomer.scoundrel.runs.RunRecord}: it also carries final health and
 * the bare-handed-kill and flawless-room facts the achievement rules test.
 * Built by {@link AchievementTracker} from the event stream and never itself
 * persisted — only the unlocked latch outlives a session.
 */
public record RunSummary(
        Status outcome,
        int score,
        int finalHealth,
        long seconds,
        int highestBarehandedKill,
        int barehandedKillCount,
        boolean flawlessRoom) {
}
