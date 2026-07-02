package com.tomer.scoundrel.rules;

import com.tomer.scoundrel.model.GameState;
import com.tomer.scoundrel.rules.Move.FightBarehanded;
import com.tomer.scoundrel.rules.Move.FightWithWeapon;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.tomer.scoundrel.rules.Cards.monster;
import static com.tomer.scoundrel.rules.Cards.monster2;
import static com.tomer.scoundrel.rules.Cards.weapon;
import static com.tomer.scoundrel.rules.CombatTest.movesFor;
import static com.tomer.scoundrel.rules.StateBuilder.state;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeaponDegradationTest {

    private final ScoundrelEngine engine = new ScoundrelEngine(Rulesets.standard());

    @Test
    void freshWeaponCanFightAnyMonsterIncludingAnAce() {
        GameState s = state().room(monster(14), weapon(3), weapon(4), weapon(6))
                .weapon(weapon(2)).build();
        assertTrue(movesFor(engine, s, monster(14)).contains(new FightWithWeapon(monster(14))));
        GameState next = engine.apply(s, new FightWithWeapon(monster(14))).state();
        assertEquals(8, next.health()); // Ace(14) - weapon 2 = 12
    }

    @Test
    void weaponThatSlewAQueenCanStillFightASix() {
        GameState s = state().room(monster(6), weapon(2), weapon(3), weapon(4))
                .weapon(weapon(5), monster(12)).build();
        GameState next = engine.apply(s, new FightWithWeapon(monster(6))).state();
        assertEquals(19, next.health()); // 6 - 5 = 1
        assertEquals(List.of(monster(12), monster(6)), next.weapon().slain());
    }

    @Test
    void equalValueIsRejectedStrictLessThanNotLessOrEqual() {
        GameState s = state().room(monster2(6), weapon(2), weapon(3), weapon(4))
                .weapon(weapon(5), monster(6)).build();
        assertFalse(movesFor(engine, s, monster2(6)).contains(new FightWithWeapon(monster2(6))));
        assertThrows(IllegalMoveException.class,
                () -> engine.apply(s, new FightWithWeapon(monster2(6))));
    }

    @Test
    void oneBelowTheLastSlainValueIsStillAllowed() {
        GameState s = state().room(monster2(5), weapon(2), weapon(3), weapon(4))
                .weapon(weapon(5), monster(6)).build();
        assertTrue(movesFor(engine, s, monster2(5)).contains(new FightWithWeapon(monster2(5))));
        GameState next = engine.apply(s, new FightWithWeapon(monster2(5))).state();
        assertEquals(20, next.health());
    }

    @Test
    void strongerMonsterThanLastSlainIsRejected() {
        GameState s = state().room(monster(7), weapon(2), weapon(3), weapon(4))
                .weapon(weapon(5), monster(6)).build();
        assertFalse(movesFor(engine, s, monster(7)).contains(new FightWithWeapon(monster(7))));
        assertThrows(IllegalMoveException.class,
                () -> engine.apply(s, new FightWithWeapon(monster(7))));
    }

    @Test
    void thresholdTracksTheLastSlainMonsterNotTheLowestOrFirst() {
        // Killed a Queen(12), then a 6: now even another 6 is rejected, only <6 remains.
        GameState s = state().room(monster2(6), monster2(5), weapon(3), weapon(4))
                .weapon(weapon(5), monster(12), monster(6)).build();
        assertFalse(movesFor(engine, s, monster2(6)).contains(new FightWithWeapon(monster2(6))));
        assertTrue(movesFor(engine, s, monster2(5)).contains(new FightWithWeapon(monster2(5))));
    }

    @Test
    void thresholdIsTheMonstersValueNotTheDamageDealt() {
        // Weapon 5 kills a Jack(11) taking 6 damage; a 10 is still fightable (10 < 11).
        GameState s = state().room(monster(11), monster2(10), weapon(3), weapon(4))
                .weapon(weapon(5)).build();
        GameState next = engine.apply(s, new FightWithWeapon(monster(11))).state();
        assertTrue(movesFor(engine, next, monster2(10)).contains(new FightWithWeapon(monster2(10))));
    }

    @Test
    void unusableWeaponStaysEquippedAndBarehandedIsStillOffered() {
        GameState s = state().room(monster2(12), weapon(2), weapon(3), weapon(4))
                .weapon(weapon(5), monster(6)).build();
        assertEquals(List.of(new FightBarehanded(monster2(12))), movesFor(engine, s, monster2(12)));
        GameState next = engine.apply(s, new FightBarehanded(monster2(12))).state();
        assertEquals(weapon(5), next.weapon().weapon());
        assertEquals(List.of(monster(6)), next.weapon().slain());
    }

    @Test
    void barehandedFightDoesNotChangeTheWeaponThreshold() {
        GameState s = state().room(monster2(9), monster2(10), weapon(3), weapon(4))
                .weapon(weapon(5), monster(12)).build();
        GameState next = engine.apply(s, new FightBarehanded(monster2(9))).state();
        assertEquals(List.of(monster(12)), next.weapon().slain());
        // 10 < 12 still fightable with the weapon
        assertTrue(movesFor(engine, next, monster2(10)).contains(new FightWithWeapon(monster2(10))));
    }

    @Test
    void weaponThatSlewATwoCanNeverBeUsedAgain() {
        GameState s = state().room(monster2(2), monster2(5), monster2(14), weapon(4))
                .weapon(weapon(9), monster(2)).build();
        for (var card : List.of(monster2(2), monster2(5), monster2(14))) {
            assertEquals(List.of(new FightBarehanded(card)), movesFor(engine, s, card),
                    "no weapon option for " + card.id());
        }
        GameState next = engine.apply(s, new FightBarehanded(monster2(5))).state();
        assertEquals(weapon(9), next.weapon().weapon()); // still equipped
    }
}
