package com.tomer.scoundrel.rules;

import com.tomer.scoundrel.model.GameState;

/** When avoiding the current room is legal (Strategy — variants may relax it). */
public interface AvoidRule {

    boolean canAvoid(GameState state);
}
