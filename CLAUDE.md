# Scoundrel — Project Memory

A desktop implementation of **Scoundrel**, a single-player roguelike card game, built with LibGDX.

## Tech stack
- Java (language level 21), Gradle multi-module project.
- LibGDX with the **LWJGL3** desktop backend only. No Android/iOS/web modules.
- UI built with Scene2D (`scene2d.ui`).

## Commands
- Run the game: `./gradlew lwjgl3:run` (Windows: `gradlew.bat lwjgl3:run`).
- Run tests: `./gradlew core:test`. Tests use JUnit 5 and live in `core/src/test/java`.

## Architecture — these are hard rules
- **All game logic lives in the `core` module.** `lwjgl3` is a thin launcher only; do not
  put game logic there.
- **The rules engine MUST be pure Java with no LibGDX imports.** No `com.badlogic.gdx.*`
  anywhere in the rules/model code. It is plain, deterministic, headless logic.
- Player actions are modeled as functions that take the current game state and return a new
  state (or mutate a well-encapsulated state object). They must be unit-testable without a
  window, a render loop, or any graphics.
- The UI layer (Scene2D screens) only does two things: draw the current state, and translate
  user input into calls on the rules engine. It never contains rule logic.
- Suggested package split inside `core`:
  - `...scoundrel.model` — cards, deck, game state (pure).
  - `...scoundrel.rules` — actions and rule resolution (pure).
  - `...scoundrel.screens` — Scene2D screens (LibGDX-dependent).

## Working preferences
- For any non-trivial change, propose a plan first and wait for review before coding.
- Write unit tests for the rules engine, especially the tricky rules below. Prefer getting
  tests green before touching the UI.
- Keep commits small and focused; commit after each working piece.

---

## Game rules (the spec — implement exactly)

### Deck (44 cards)
Start from a standard 52-card deck plus 2 jokers, then remove: both jokers, the red face
cards (J/Q/K of hearts and diamonds), and the red aces (A of hearts, A of diamonds). What
remains:
- **Monsters** — all 26 clubs and spades.
- **Weapons** — the 9 diamonds (2 through 10).
- **Health potions** — the 9 hearts (2 through 10).

Shuffle the 44 cards into the face-down **Dungeon**. Starting **health is 20** (hard cap; you
can never heal above 20, except in the one scoring edge case noted below).

### Card values
- Monster damage = ordered value: 2–10 face value, J=11, Q=12, K=13, A=14.
- Weapon value = 2–10 (its number).
- Potion heal = 2–10 (its number).

### Turn structure (the Room)
- A **Room** is 4 face-up cards. Flip from the Dungeon until 4 are showing.
- **Avoiding:** you may scoop all 4 cards and place them at the bottom of the Dungeon.
  You may avoid any number of rooms, but **never two rooms in a row**.
- If you don't avoid, you must resolve **3 of the 4 cards**, one at a time. The remaining
  4th card carries over as the first card of the next Room.

### Resolving a card
- **Weapon:** binding — you must equip it, discarding your previous weapon (and any monsters
  stacked on it).
- **Potion:** add its value to health (capped at 20), then discard. **Only one potion heals
  per turn** — a second potion taken in the same turn is discarded and does nothing.
- **Monster:** fight it barehanded or with the equipped weapon.

### Combat
- **Barehanded:** subtract the monster's full value from your health; discard the monster.
- **With a weapon:** damage taken = max(0, monster value − weapon value). (Weapon 5 vs a 3
  monster → 0 damage; weapon 5 vs a Jack(11) → 6 damage.) Stack the monster on the weapon.
- **Weapon degradation (IMPORTANT):** once a weapon has slain a monster, it can only be used
  on monsters whose value is **less than or equal to the last monster it slew**. Example: a
  5-weapon that killed a Queen(12) can still be used on a 6 (6 ≤ 12); but after it is used on
  a 6, it can only be used on monsters ≤ 6 — a Queen would have to be fought barehanded. The
  weapon is **not** discarded when it can't be used; it stays equipped for weaker monsters.

### End and scoring
- The game ends when health reaches 0, or you clear the entire Dungeon.
- **If health reached 0:** sum the values of all monsters still left in the Dungeon and
  subtract that from your (zero/negative) life. The resulting negative number is your score.
- **If you cleared the Dungeon:** your score is your remaining positive life. Special case:
  if your life is 20 *and* the last card resolved was a health potion, your score is
  20 + that potion's value (the only way the total exceeds 20).
