package com.tomer.scoundrel.runs;

import com.tomer.scoundrel.model.Card;
import com.tomer.scoundrel.model.CardType;
import com.tomer.scoundrel.model.GameState;
import com.tomer.scoundrel.model.Status;
import com.tomer.scoundrel.rules.GameEvent;
import com.tomer.scoundrel.rules.MoveResult;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

/** Minimal fixtures: the recorder only reads events, so one dummy state serves all. */
final class RunsTestData {

    static final GameState DUMMY_STATE = new GameState(
            List.of(), List.of(), null, 20, 0, 0, false, null, Status.IN_PROGRESS, null);

    private RunsTestData() {
    }

    static MoveResult result(GameEvent... events) {
        return new MoveResult(DUMMY_STATE, List.of(events));
    }

    static Card monster(String id, int value) {
        return new Card(id, CardType.MONSTER, value);
    }

    static Card weapon(int value) {
        return new Card(value + "D", CardType.WEAPON, value);
    }

    static Card potion(int value) {
        return new Card(value + "H", CardType.POTION, value);
    }

    /** A clock that returns the given instants in order (last one repeats). */
    static Clock sequenceClock(Instant... instants) {
        return new Clock() {
            private int next;

            @Override
            public Instant instant() {
                return instants[Math.min(next++, instants.length - 1)];
            }

            @Override
            public ZoneId getZone() {
                return ZoneOffset.UTC;
            }

            @Override
            public Clock withZone(ZoneId zone) {
                return this;
            }
        };
    }
}
