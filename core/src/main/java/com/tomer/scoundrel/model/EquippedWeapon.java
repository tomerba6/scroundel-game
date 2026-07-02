package com.tomer.scoundrel.model;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

/**
 * The equipped weapon plus the stack of monsters it has slain, most recent
 * last. Degradation state is derived from the stack rather than stored.
 */
public record EquippedWeapon(Card weapon, List<Card> slain) {

    public EquippedWeapon {
        slain = List.copyOf(slain);
    }

    /** A freshly equipped weapon that has slain nothing yet. */
    public EquippedWeapon(Card weapon) {
        this(weapon, List.of());
    }

    public boolean hasSlain() {
        return !slain.isEmpty();
    }

    /** Value of the last slain monster; empty while the weapon is fresh (no limit). */
    public OptionalInt threshold() {
        return hasSlain() ? OptionalInt.of(slain.getLast().value()) : OptionalInt.empty();
    }

    /**
     * A fresh weapon can fight anything; afterwards only monsters strictly
     * weaker than the last slain one (equal value must be fought barehanded).
     */
    public boolean canUseAgainst(Card monster) {
        return !hasSlain() || monster.value() < slain.getLast().value();
    }

    public EquippedWeapon withSlain(Card monster) {
        List<Card> next = new ArrayList<>(slain);
        next.add(monster);
        return new EquippedWeapon(weapon, next);
    }
}
