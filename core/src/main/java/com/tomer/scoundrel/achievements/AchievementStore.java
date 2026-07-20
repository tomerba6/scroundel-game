package com.tomer.scoundrel.achievements;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Append-only local latch of earned achievements, one
 * {@link UnlockedAchievement} line each — the sibling of
 * {@link com.tomer.scoundrel.runs.RunLog}. Append never rewrites existing data;
 * reading tolerates a missing file, skips lines that don't parse, and dedupes a
 * repeated id to its earliest earn (append order is chronological). The path is
 * injected so tests use a temp directory and the app decides the real location.
 */
public final class AchievementStore {

    private final Path file;

    public AchievementStore(Path file) {
        this.file = file;
    }

    public void append(UnlockedAchievement unlocked) {
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            Files.writeString(file, unlocked.toLine() + System.lineSeparator(),
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException("could not append achievement to " + file, e);
        }
    }

    /**
     * Erases the latch by moving the file aside to a {@code .bak} sibling
     * (overwriting any earlier backup), so an accidental reset stays
     * recoverable from disk; the game never restores it automatically. A no-op
     * when nothing has been unlocked yet.
     */
    public void clear() {
        try {
            if (Files.exists(file)) {
                Path backup = file.resolveSibling(file.getFileName().toString() + ".bak");
                Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("could not clear " + file, e);
        }
    }

    /** Every earned achievement, deduped by id (earliest earn kept); empty when none exist. */
    public List<UnlockedAchievement> readAll() {
        if (!Files.exists(file)) {
            return List.of();
        }
        try {
            Map<String, UnlockedAchievement> byId = new LinkedHashMap<>();
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (!line.isBlank()) {
                    UnlockedAchievement.parse(line)
                            .ifPresent(unlocked -> byId.putIfAbsent(unlocked.id(), unlocked));
                }
            }
            return List.copyOf(byId.values());
        } catch (IOException e) {
            throw new UncheckedIOException("could not read achievements from " + file, e);
        }
    }

    /** The set of earned achievement ids — what {@link AchievementService} filters against. */
    public Set<String> unlockedIds() {
        return readAll().stream().map(UnlockedAchievement::id)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }
}
