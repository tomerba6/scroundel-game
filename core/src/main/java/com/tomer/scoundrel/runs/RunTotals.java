package com.tomer.scoundrel.runs;

import com.tomer.scoundrel.model.Status;

import java.util.List;

/**
 * Lifetime sums across finished runs — a pure view over the run log like
 * {@link HighScores}, no I/O. "Finished" is the whole universe by design:
 * abandoned games are never recorded.
 */
public record RunTotals(
        int runs,
        int wins,
        int losses,
        int monstersDefeated,
        int damageTaken,
        int healthHealed,
        int potionsDrunk,
        int potionsWasted,
        int weaponsEquipped,
        int roomsAvoided,
        long secondsPlayed) {

    public static RunTotals of(List<RunRecord> records) {
        int wins = 0;
        int losses = 0;
        int monsters = 0;
        int damage = 0;
        int healed = 0;
        int drunk = 0;
        int wasted = 0;
        int equips = 0;
        int avoids = 0;
        long seconds = 0;
        for (RunRecord record : records) {
            if (record.outcome() == Status.WON) {
                wins++;
            } else {
                losses++;
            }
            monsters += record.monstersDefeated();
            damage += record.damageTaken();
            healed += record.healthHealed();
            drunk += record.potionsDrunk();
            wasted += record.potionsWasted();
            equips += record.weaponsEquipped();
            avoids += record.roomsAvoided();
            seconds += record.seconds();
        }
        return new RunTotals(records.size(), wins, losses, monsters, damage,
                healed, drunk, wasted, equips, avoids, seconds);
    }

    /** Fraction of runs cleared; 0 while no runs exist. */
    public double winRate() {
        return runs == 0 ? 0 : (double) wins / runs;
    }
}
