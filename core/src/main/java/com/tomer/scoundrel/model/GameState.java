package com.tomer.scoundrel.model;

import java.util.List;

/**
 * Immutable snapshot of a whole game. Index 0 of {@code dungeon} is the top
 * of the face-down pile. {@code weapon}, {@code lastResolvedCard} and
 * {@code score} are null when absent ({@code score} is set only once the
 * game is over).
 */
public record GameState(
        List<Card> dungeon,
        List<Card> room,
        EquippedWeapon weapon,
        int health,
        int potionsUsedThisRoom,
        boolean roomResolutionStarted,
        boolean previousRoomAvoided,
        Card lastResolvedCard,
        Status status,
        Integer score) {

    public GameState {
        dungeon = List.copyOf(dungeon);
        room = List.copyOf(room);
    }
}
