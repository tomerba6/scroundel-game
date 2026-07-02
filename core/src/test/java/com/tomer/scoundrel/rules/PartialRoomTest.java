package com.tomer.scoundrel.rules;

import com.tomer.scoundrel.model.GameState;
import com.tomer.scoundrel.model.Status;
import com.tomer.scoundrel.rules.Move.AvoidRoom;
import com.tomer.scoundrel.rules.Move.TakePotion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.tomer.scoundrel.rules.Cards.potion;
import static com.tomer.scoundrel.rules.StateBuilder.state;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PartialRoomTest {

    private final ScoundrelEngine engine = new ScoundrelEngine(Rulesets.standard());

    @Test
    void refillWithFewCardsLeftMakesAPartialRoom() {
        GameState g = engine.newGame(List.of(
                potion(2), potion(3), potion(4), potion(5), potion(6), potion(7)));
        g = engine.apply(g, new TakePotion(potion(2))).state();
        g = engine.apply(g, new TakePotion(potion(3))).state();
        g = engine.apply(g, new TakePotion(potion(4))).state();
        assertEquals(List.of(potion(5), potion(6), potion(7)), g.room()); // carryover + last 2
        assertTrue(g.dungeon().isEmpty());
        assertEquals(Status.IN_PROGRESS, g.status());
    }

    @Test
    void everyCardOfAPartialRoomMustBeResolvedBeforeTheGameEnds() {
        GameState g = engine.newGame(List.of(
                potion(2), potion(3), potion(4), potion(5), potion(6), potion(7)));
        g = engine.apply(g, new TakePotion(potion(2))).state();
        g = engine.apply(g, new TakePotion(potion(3))).state();
        g = engine.apply(g, new TakePotion(potion(4))).state();
        g = engine.apply(g, new TakePotion(potion(5))).state();
        assertEquals(Status.IN_PROGRESS, g.status());
        g = engine.apply(g, new TakePotion(potion(6))).state();
        assertEquals(Status.IN_PROGRESS, g.status());
        g = engine.apply(g, new TakePotion(potion(7))).state();
        assertEquals(Status.WON, g.status());
    }

    @Test
    void resolvingTheFinalSingleCardWinsWithHealthLeft() {
        GameState s = state().health(9).room(potion(5)).build();
        GameState won = engine.apply(s, new TakePotion(potion(5))).state();
        assertEquals(Status.WON, won.status());
        assertEquals(14, won.score());
    }

    @Test
    void lastCarryoverWithEmptyDungeonBecomesAOneCardRoom() {
        GameState g = engine.newGame(List.of(
                potion(2), potion(3), potion(4), potion(5),
                potion(6), potion(7), potion(8)));
        g = engine.apply(g, new TakePotion(potion(2))).state();
        g = engine.apply(g, new TakePotion(potion(3))).state();
        g = engine.apply(g, new TakePotion(potion(4))).state();
        // full room again: carryover 5H + the last 3 dungeon cards
        assertEquals(4, g.room().size());
        g = engine.apply(g, new TakePotion(potion(5))).state();
        g = engine.apply(g, new TakePotion(potion(6))).state();
        g = engine.apply(g, new TakePotion(potion(7))).state();
        assertEquals(List.of(potion(8)), g.room());
        assertEquals(Status.IN_PROGRESS, g.status());
        g = engine.apply(g, new TakePotion(potion(8))).state();
        assertEquals(Status.WON, g.status());
    }

    @Test
    void avoidingAPartialRoomIsIllegal() {
        GameState s = state().room(potion(2), potion(3), potion(4)).build(); // dungeon empty
        assertFalse(engine.legalMoves(s).contains(new AvoidRoom()));
        assertThrows(IllegalMoveException.class, () -> engine.apply(s, new AvoidRoom()));
    }

    @Test
    void avoidingAFullRoomWithAnEmptyDungeonIsAlsoIllegal() {
        GameState s = state().room(potion(2), potion(3), potion(4), potion(5)).build();
        assertFalse(engine.legalMoves(s).contains(new AvoidRoom()));
        assertThrows(IllegalMoveException.class, () -> engine.apply(s, new AvoidRoom()));
    }

    @Test
    void avoidingWithASingleDungeonCardLeftIsLegal() {
        GameState s = state().room(potion(2), potion(3), potion(4), potion(5))
                .dungeon(potion(6)).build();
        assertTrue(engine.legalMoves(s).contains(new AvoidRoom()));
        GameState next = engine.apply(s, new AvoidRoom()).state();
        assertEquals(List.of(potion(6), potion(2), potion(3), potion(4)), next.room());
        assertEquals(List.of(potion(5)), next.dungeon());
    }
}
