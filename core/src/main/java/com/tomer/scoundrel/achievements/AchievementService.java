package com.tomer.scoundrel.achievements;

import java.util.List;
import java.util.Set;

/**
 * Pure end-of-run evaluation: which catalog achievements are now earned but not
 * already unlocked. The caller (the UI) persists the returned ids and shows
 * them; core never pushes. A pure function over the context and the unlocked
 * set — like {@link com.tomer.scoundrel.runs.HighScores} over the run history.
 */
public final class AchievementService {

    private AchievementService() {
    }

    public static List<Achievement> newlyEarned(List<Achievement> catalog,
                                                AchievementContext context,
                                                Set<String> alreadyUnlocked) {
        return catalog.stream()
                .filter(achievement -> !alreadyUnlocked.contains(achievement.id()))
                .filter(achievement -> achievement.earnedBy(context))
                .toList();
    }
}
