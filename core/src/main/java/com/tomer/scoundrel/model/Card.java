package com.tomer.scoundrel.model;

/**
 * A card in play. {@code type} and {@code value} are stamped from the card's
 * definition so the state stays plain and serializable; behavior is looked up
 * by {@code id} from the active deck definition at resolution time.
 */
public record Card(String id, CardType type, int value) {
}
