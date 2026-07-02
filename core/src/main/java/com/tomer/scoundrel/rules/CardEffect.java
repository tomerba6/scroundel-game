package com.tomer.scoundrel.rules;

import com.tomer.scoundrel.model.Card;
import com.tomer.scoundrel.model.GameState;

import java.util.List;

/**
 * A card's behavior (Strategy). The engine never switches on suit or type:
 * it asks the effect which moves the card offers, and dispatches resolution
 * to the effect via a {@link ResolutionContext}.
 */
public interface CardEffect {

    /** The moves this card offers the player in the given state. */
    List<Move> legalMoves(Card card, GameState state);

    /** Apply the chosen move's consequences to the in-flight resolution. */
    void resolve(Move.CardMove move, ResolutionContext ctx);
}
