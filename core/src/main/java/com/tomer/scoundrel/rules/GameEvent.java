package com.tomer.scoundrel.rules;

import com.tomer.scoundrel.model.Card;

import java.util.List;

/**
 * Something that happened while applying a move. Returned from
 * {@link ScoundrelEngine#apply} so achievements, stats and the UI can observe
 * the game from outside core. Deliberately not sealed: new card effects may
 * emit their own event types.
 */
public interface GameEvent {

    record RoomDealt(List<Card> room) implements GameEvent {
        public RoomDealt {
            room = List.copyOf(room);
        }
    }

    record RoomAvoided(List<Card> room) implements GameEvent {
        public RoomAvoided {
            room = List.copyOf(room);
        }
    }

    record WeaponEquipped(Card weapon) implements GameEvent {
    }

    record MonsterDefeated(Card monster, boolean withWeapon, int damageTaken) implements GameEvent {
    }

    record PotionUsed(Card potion, int healed) implements GameEvent {
    }

    record PotionWasted(Card potion) implements GameEvent {
    }

    record WeaponDegraded(Card weapon, int newThreshold) implements GameEvent {
    }

    record GameWon(int score) implements GameEvent {
    }

    record GameLost(int score) implements GameEvent {
    }
}
