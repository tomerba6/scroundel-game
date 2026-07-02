package com.tomer.scoundrel.rules;

import com.tomer.scoundrel.model.Card;
import com.tomer.scoundrel.model.EquippedWeapon;
import com.tomer.scoundrel.model.GameState;

import java.util.ArrayList;
import java.util.List;

/**
 * Mutable working copy of the parts of the state a card effect may change.
 * Created by the engine for a single {@code apply} call and never escapes it;
 * the engine reads the results back into the next immutable GameState.
 */
public final class ResolutionContext {

    private final Ruleset rules;
    private int health;
    private EquippedWeapon weapon;
    private int potionsUsedThisRoom;
    private final List<GameEvent> events = new ArrayList<>();

    ResolutionContext(Ruleset rules, GameState state) {
        this.rules = rules;
        this.health = state.health();
        this.weapon = state.weapon();
        this.potionsUsedThisRoom = state.potionsUsedThisRoom();
    }

    public Ruleset rules() {
        return rules;
    }

    public int health() {
        return health;
    }

    /** Null when nothing is equipped. */
    public EquippedWeapon weapon() {
        return weapon;
    }

    public int potionsUsedThisRoom() {
        return potionsUsedThisRoom;
    }

    /** Health may go below zero; the loss score needs the raw value. */
    public void damage(int amount) {
        health -= amount;
    }

    /** Heals up to the ruleset's health cap; returns the amount actually healed. */
    public int heal(int amount) {
        int healed = Math.min(amount, Math.max(0, rules.healthCap() - health));
        health += healed;
        return healed;
    }

    /** Equips a fresh weapon; the previous weapon and its stack are discarded. */
    public void equip(Card weaponCard) {
        weapon = new EquippedWeapon(weaponCard);
    }

    public void slayWithWeapon(Card monster) {
        weapon = weapon.withSlain(monster);
    }

    public void notePotionTaken() {
        potionsUsedThisRoom++;
    }

    public void emit(GameEvent event) {
        events.add(event);
    }

    List<GameEvent> events() {
        return events;
    }
}
