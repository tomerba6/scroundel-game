package com.tomer.scoundrel.rules;

/** Thrown when a move is applied that {@code legalMoves} does not offer. */
public class IllegalMoveException extends RuntimeException {

    public IllegalMoveException(String message) {
        super(message);
    }
}
