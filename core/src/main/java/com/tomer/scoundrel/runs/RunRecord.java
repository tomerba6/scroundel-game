package com.tomer.scoundrel.runs;

import com.tomer.scoundrel.model.Status;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * One finished game, as persisted. Encoded as a single line of tab-separated
 * {@code key=value} pairs, first key {@code v} (the schema version). The
 * parser is deliberately tolerant — unknown keys are ignored, missing
 * counters default to 0, and a line it cannot understand yields empty rather
 * than throwing — so a corrupt or future-version line can never take the
 * game down; adding a field later is just a new key.
 */
public record RunRecord(
        Long seed,
        String rulesetId,
        Status outcome,
        int score,
        Instant endedAt,
        long seconds,
        int monstersDefeated,
        int damageTaken,
        int healthHealed,
        int potionsDrunk,
        int potionsWasted,
        int weaponsEquipped,
        int roomsAvoided) {

    static final int VERSION = 1;

    public RunRecord {
        if (outcome != Status.WON && outcome != Status.LOST) {
            throw new IllegalArgumentException("a recorded run must be WON or LOST, got " + outcome);
        }
        if (rulesetId == null || !rulesetId.matches("\\S+")) {
            throw new IllegalArgumentException("rulesetId must be non-blank without whitespace, got '" + rulesetId + "'");
        }
        if (endedAt == null) {
            throw new IllegalArgumentException("endedAt is required");
        }
    }

    /** The single persisted line (no trailing newline). */
    public String toLine() {
        StringBuilder sb = new StringBuilder();
        sb.append("v=").append(VERSION);
        if (seed != null) {
            sb.append("\tseed=").append(seed);
        }
        sb.append("\truleset=").append(rulesetId);
        sb.append("\toutcome=").append(outcome.name());
        sb.append("\tscore=").append(score);
        sb.append("\tended=").append(endedAt);
        sb.append("\tseconds=").append(seconds);
        sb.append("\tmonsters=").append(monstersDefeated);
        sb.append("\tdamage=").append(damageTaken);
        sb.append("\thealed=").append(healthHealed);
        sb.append("\tdrunk=").append(potionsDrunk);
        sb.append("\twasted=").append(potionsWasted);
        sb.append("\tequips=").append(weaponsEquipped);
        sb.append("\tavoids=").append(roomsAvoided);
        return sb.toString();
    }

    /** Empty when the line is malformed or from an unknown schema version. */
    public static Optional<RunRecord> parse(String line) {
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
            String seedText = kv.get("seed");
            return Optional.of(new RunRecord(
                    seedText == null ? null : Long.valueOf(seedText),
                    kv.getOrDefault("ruleset", "unknown"),
                    Status.valueOf(kv.get("outcome")),
                    Integer.parseInt(kv.get("score")),
                    Instant.parse(kv.get("ended")),
                    longOrZero(kv.get("seconds")),
                    intOrZero(kv.get("monsters")),
                    intOrZero(kv.get("damage")),
                    intOrZero(kv.get("healed")),
                    intOrZero(kv.get("drunk")),
                    intOrZero(kv.get("wasted")),
                    intOrZero(kv.get("equips")),
                    intOrZero(kv.get("avoids"))));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    private static int intOrZero(String value) {
        return value == null ? 0 : Integer.parseInt(value);
    }

    private static long longOrZero(String value) {
        return value == null ? 0 : Long.parseLong(value);
    }
}
