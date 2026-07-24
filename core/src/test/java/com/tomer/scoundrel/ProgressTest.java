package com.tomer.scoundrel;

import com.tomer.scoundrel.achievements.AchievementStore;
import com.tomer.scoundrel.achievements.UnlockedAchievement;
import com.tomer.scoundrel.model.Status;
import com.tomer.scoundrel.runs.RunLog;
import com.tomer.scoundrel.runs.RunRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProgressTest {

    @TempDir
    Path dir;

    private RunLog runLog() {
        return new RunLog(dir.resolve("runs.log"));
    }

    private AchievementStore achievements() {
        return new AchievementStore(dir.resolve("achievements.log"));
    }

    @Test
    void eraseAllWipesBothStoresAndLeavesEachRecoverable() {
        RunLog runLog = runLog();
        AchievementStore achievements = achievements();
        RunRecord record = new RunRecord(1L, "standard", Status.WON, 20,
                Instant.parse("2026-07-06T10:00:00Z"), 60, 5, 4, 3, 2, 1, 1, 0);
        UnlockedAchievement unlocked = new UnlockedAchievement("first_blood",
                Instant.parse("2026-07-06T10:00:00Z"));
        runLog.append(record);
        achievements.append(unlocked);

        Progress.eraseAll(runLog, achievements);

        // Both stores are emptied together...
        assertEquals(List.of(), runLog.readAll());
        assertEquals(Set.of(), achievements.unlockedIds());
        // ...and both remain recoverable from their .bak siblings.
        assertEquals(List.of(record), new RunLog(dir.resolve("runs.log.bak")).readAll());
        assertEquals(List.of(unlocked),
                new AchievementStore(dir.resolve("achievements.log.bak")).readAll());
    }

    @Test
    void eraseAllOnEmptyStoresIsANoOp() {
        RunLog runLog = runLog();
        AchievementStore achievements = achievements();
        Progress.eraseAll(runLog, achievements);
        assertEquals(List.of(), runLog.readAll());
        assertEquals(Set.of(), achievements.unlockedIds());
    }
}
