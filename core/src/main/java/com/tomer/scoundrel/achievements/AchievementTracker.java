package com.tomer.scoundrel.achievements;

import com.tomer.scoundrel.model.Status;
import com.tomer.scoundrel.rules.GameEvent;
import com.tomer.scoundrel.rules.MoveResult;

/**
 * Observes one game from outside the engine — the caller feeds it every
 * {@link MoveResult} — and distils the per-run facts achievements test into a
 * {@link RunSummary} once the game ends. A sibling to
 * {@link com.tomer.scoundrel.runs.RunRecorder}, kept separate so achievements
 * never depend on the persisted run schema. Pure Java.
 */
public final class AchievementTracker {

    private final int cardsPerFullRoom;

    private int highestBarehandedKill;
    private int barehandedKillCount;
    private boolean flawlessRoom;
    private int finalHealth;
    private Status outcome;
    private int score;

    // Accumulated within the current room-turn; reset at each turn boundary.
    private int roomDamage;
    private int roomCardsResolved;
    private int roomMonstersDefeated;

    public AchievementTracker(int cardsPerFullRoom) {
        this.cardsPerFullRoom = cardsPerFullRoom;
    }

    public void observe(MoveResult result) {
        finalHealth = result.state().health();
        boolean avoided = false;
        boolean turnEnded = false;
        for (GameEvent event : result.events()) {
            switch (event) {
                case GameEvent.MonsterDefeated monster -> {
                    roomCardsResolved++;
                    roomMonstersDefeated++;
                    roomDamage += monster.damageTaken();
                    if (!monster.withWeapon()) {
                        barehandedKillCount++;
                        highestBarehandedKill = Math.max(highestBarehandedKill, monster.monster().value());
                    }
                }
                case GameEvent.PotionUsed ignored -> roomCardsResolved++;
                case GameEvent.PotionWasted ignored -> roomCardsResolved++;
                case GameEvent.WeaponEquipped ignored -> roomCardsResolved++;
                case GameEvent.RoomAvoided ignored -> avoided = true;
                case GameEvent.RoomDealt ignored -> turnEnded = true;
                case GameEvent.GameWon won -> {
                    outcome = Status.WON;
                    score = won.score();
                    turnEnded = true;
                }
                case GameEvent.GameLost lost -> {
                    outcome = Status.LOST;
                    score = lost.score();
                    turnEnded = true;
                }
                default -> { } // WeaponDegraded carries no card of its own
            }
        }
        if (avoided) {
            resetRoom(); // scooping a room never counts as clearing it unscathed
        } else if (turnEnded) {
            // "A whole room" means a full turn's worth of resolutions taken without a
            // scratch. The endgame's final short room (the standard deck always ends in
            // a 2-card room) resolves fewer than cardsPerFullRoom and so deliberately
            // never qualifies — Flawless Room is earned on an earlier full room instead.
            // Once set the flag latches: a later damaged room never clears it.
            if (roomDamage == 0 && roomMonstersDefeated >= 1 && roomCardsResolved >= cardsPerFullRoom) {
                flawlessRoom = true;
            }
            resetRoom();
        }
    }

    public boolean isFinished() {
        return outcome != null;
    }

    /** Only valid once the observed game has ended; {@code seconds} comes from the run timer. */
    public RunSummary toSummary(long seconds) {
        if (!isFinished()) {
            throw new IllegalStateException("the run has not ended yet");
        }
        return new RunSummary(outcome, score, finalHealth, seconds,
                highestBarehandedKill, barehandedKillCount, flawlessRoom);
    }

    private void resetRoom() {
        roomDamage = 0;
        roomCardsResolved = 0;
        roomMonstersDefeated = 0;
    }
}
