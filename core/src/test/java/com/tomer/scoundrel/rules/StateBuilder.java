package com.tomer.scoundrel.rules;

import com.tomer.scoundrel.model.Card;
import com.tomer.scoundrel.model.EquippedWeapon;
import com.tomer.scoundrel.model.GameState;
import com.tomer.scoundrel.model.Status;

import java.util.List;

/** Builds a specific in-progress GameState so a rule can be tested in isolation. */
final class StateBuilder {

    private List<Card> dungeon = List.of();
    private List<Card> room = List.of();
    private EquippedWeapon weapon;
    private int health = 20;
    private int potionsUsed = 0;
    private boolean started = false;
    private boolean previousAvoided = false;
    private Card lastResolved;

    static StateBuilder state() {
        return new StateBuilder();
    }

    StateBuilder dungeon(Card... cards) {
        this.dungeon = List.of(cards);
        return this;
    }

    StateBuilder room(Card... cards) {
        this.room = List.of(cards);
        return this;
    }

    StateBuilder weapon(Card weaponCard) {
        this.weapon = new EquippedWeapon(weaponCard);
        return this;
    }

    StateBuilder weapon(Card weaponCard, Card... slain) {
        this.weapon = new EquippedWeapon(weaponCard, List.of(slain));
        return this;
    }

    StateBuilder health(int health) {
        this.health = health;
        return this;
    }

    StateBuilder potionsUsed(int potionsUsed) {
        this.potionsUsed = potionsUsed;
        return this;
    }

    StateBuilder started() {
        this.started = true;
        return this;
    }

    StateBuilder previousAvoided() {
        this.previousAvoided = true;
        return this;
    }

    GameState build() {
        return new GameState(dungeon, room, weapon, health, potionsUsed,
                started, previousAvoided, lastResolved, Status.IN_PROGRESS, null);
    }
}
