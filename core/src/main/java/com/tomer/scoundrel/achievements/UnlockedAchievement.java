package com.tomer.scoundrel.achievements;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * One earned achievement, as persisted: the achievement's {@code id} and when
 * it was first earned. Encoded like {@link com.tomer.scoundrel.runs.RunRecord}
 * as a tab-separated {@code key=value} line led by the schema version, and
 * parsed just as tolerantly — an unreadable or future-version line yields empty
 * rather than throwing, so the latch can never take the game down.
 */
public record UnlockedAchievement(String id, Instant earnedAt) {

    static final int VERSION = 1;

    public UnlockedAchievement {
        if (id == null || !id.matches("\\S+")) {
            throw new IllegalArgumentException("id must be non-blank without whitespace, got '" + id + "'");
        }
        if (earnedAt == null) {
            throw new IllegalArgumentException("earnedAt is required");
        }
    }

    /** The single persisted line (no trailing newline). */
    public String toLine() {
        return "v=" + VERSION + "\tid=" + id + "\tearned=" + earnedAt;
    }

    /** Empty when the line is malformed or from an unknown schema version. */
    public static Optional<UnlockedAchievement> parse(String line) {
        try {
            Map<String, String> kv = new HashMap<>();
            for (String token : line.trim().split("\t")) {
                int eq = token.indexOf('=');
                if (eq > 0) {
                    kv.put(token.substring(0, eq), token.substring(eq + 1));
                }
            }
            if (Integer.parseInt(kv.get("v")) != VERSION) {
                return Optional.empty();
            }
            return Optional.of(new UnlockedAchievement(kv.get("id"), Instant.parse(kv.get("earned"))));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }
}
