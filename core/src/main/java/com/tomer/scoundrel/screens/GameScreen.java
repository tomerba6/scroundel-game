package com.tomer.scoundrel.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.tomer.scoundrel.model.Card;
import com.tomer.scoundrel.model.CardType;
import com.tomer.scoundrel.model.EquippedWeapon;
import com.tomer.scoundrel.model.GameState;
import com.tomer.scoundrel.rules.GameEvent;
import com.tomer.scoundrel.rules.Move;
import com.tomer.scoundrel.rules.MoveResult;
import com.tomer.scoundrel.rules.Ruleset;
import com.tomer.scoundrel.rules.Rulesets;
import com.tomer.scoundrel.rules.ScoundrelEngine;

import java.util.List;
import java.util.Random;

/**
 * The one in-game screen: draws the current GameState and (in later slices)
 * translates clicks into engine moves. Contains no rule logic — everything it
 * knows about the game comes from the state, the ruleset, and legalMoves().
 * The whole board is rebuilt from the state after every move.
 */
public final class GameScreen extends ScreenAdapter {

    private static final float WORLD_WIDTH = 1280;
    private static final float WORLD_HEIGHT = 720;

    private final Theme theme;
    private final Ruleset rules;
    private final ScoundrelEngine engine;
    private final Stage stage;
    private final Table root = new Table();
    private final VerticalGroup feed = new VerticalGroup();
    private GameState state;

    public GameScreen(Theme theme) {
        this.theme = theme;
        this.rules = Rulesets.standard();
        this.engine = new ScoundrelEngine(rules);
        this.stage = new Stage(new FitViewport(WORLD_WIDTH, WORLD_HEIGHT));
        this.state = engine.newGame(new Random().nextLong());

        root.setFillParent(true);
        stage.addActor(root);

        // The fading feed floats top-right, above the board and out of the
        // way of clicks; its lines outlive board rebuilds.
        feed.columnRight();
        feed.space(4);
        Table feedAnchor = new Table();
        feedAnchor.setFillParent(true);
        feedAnchor.top().right().padTop(96).padRight(28);
        feedAnchor.add(feed);
        feedAnchor.setTouchable(Touchable.disabled);
        stage.addActor(feedAnchor);

        rebuild();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Theme.SOOT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) {
            return; // minimized window
        }
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    /** Rebuilds the whole board from the current state. */
    private void rebuild() {
        root.clearChildren();
        root.top();
        root.add(topStrip()).growX().height(72).pad(12, 24, 0, 24);
        root.row();
        root.add(roomRow()).grow();
        root.row();
        root.add(bottomStrip()).growX().height(64).pad(0, 24, 12, 24);
    }

    // --- top strip: health, depth ticker, avoid ---

    private Actor topStrip() {
        Table strip = new Table();
        strip.add(healthGroup()).left();
        strip.add(depthTicker()).expandX();
        strip.add(avoidButton()).right();
        return strip;
    }

    private Actor healthGroup() {
        Table group = new Table();
        group.add(label("HP", theme.bodyBold, Theme.BONE)).padRight(8);
        group.add(healthBar()).width(160).height(14).padRight(8);
        group.add(label(String.valueOf(state.health()), theme.bodyBold, Theme.BONE));
        return group;
    }

    /** Bone at full health, charring toward dried blood as it drops. */
    private Actor healthBar() {
        float fraction = Math.max(0f, Math.min(1f, state.health() / (float) rules.healthCap()));
        Color fill = new Color(Theme.DRIED_BLOOD).lerp(Theme.BONE, fraction);
        Table bar = new Table();
        bar.setBackground(theme.solid(Theme.STONE));
        bar.left().pad(2);
        bar.add(new Image(theme.solid(fill))).width(156 * fraction).height(10);
        return bar;
    }

    /**
     * The depth ticker — one tick per card the dungeon started with; lit
     * ticks are what is still face-down below you. Avoided rooms visibly
     * return their ticks to the pile.
     */
    private Actor depthTicker() {
        Color consumed = Color.valueOf("3a3229");
        int remaining = state.dungeon().size();
        int total = rules.deck().cards().size();
        Table ticker = new Table();
        Table ticks = new Table();
        for (int i = 0; i < total; i++) {
            Color c = i < remaining ? Theme.TORCHLIGHT : consumed;
            ticks.add(new Image(theme.solid(c))).width(4).height(16).padRight(2);
        }
        ticker.add(ticks);
        ticker.row();
        ticker.add(label("depth: " + remaining + " cards", theme.small, dim(Theme.BONE, 0.6f)))
                .padTop(4);
        return ticker;
    }

    private Actor avoidButton() {
        TextButton button = torchButton("Avoid");
        button.setDisabled(!engine.legalMoves(state).contains(new Move.AvoidRoom()));
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                applyMove(new Move.AvoidRoom());
            }
        });
        return button;
    }

    private TextButton torchButton(String text) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = theme.bodyBold;
        style.up = theme.solid(Theme.TORCHLIGHT);
        style.fontColor = Theme.SOOT;
        style.disabled = theme.solid(Theme.STONE);
        style.disabledFontColor = dim(Theme.BONE, 0.35f);
        TextButton button = new TextButton(text, style);
        button.pad(6, 20, 6, 20);
        return button;
    }

    // --- center: the room ---

    private Actor roomRow() {
        Table row = new Table();
        for (Card card : state.room()) {
            row.add(cardTile(card)).size(170, 240).pad(12);
        }
        return row;
    }

    private Actor cardTile(Card card) {
        Color background = roleColor(card.type());
        Color text = card.type() == CardType.WEAPON ? Theme.SOOT : Theme.BONE;
        Table tile = new Table();
        tile.setBackground(theme.solid(background));
        tile.add(label(card.type().name(), theme.small, dim(text, 0.7f))).padTop(12);
        tile.row();
        tile.add(label(String.valueOf(card.value()), theme.display, text)).expand();
        tile.row();
        tile.add(cardCorner(card, text)).right().padRight(12).padBottom(10);
        tile.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onCardClicked(card, tile);
            }
        });
        return tile;
    }

    /** One legal move plays immediately; two or more open the chooser. */
    private void onCardClicked(Card card, Actor tile) {
        List<Move> moves = engine.legalMoves(state).stream()
                .filter(m -> m instanceof Move.CardMove cm && cm.targetCard().equals(card))
                .toList();
        if (moves.size() == 1) {
            applyMove(moves.get(0));
        } else if (moves.size() > 1) {
            showChooser(moves, tile);
        }
    }

    /** Small button stack over the clicked card; clicking elsewhere dismisses. */
    private void showChooser(List<Move> moves, Actor tile) {
        Group overlay = new Group();
        Actor catcher = new Actor();
        catcher.setBounds(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        catcher.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                overlay.remove();
            }
        });
        overlay.addActor(catcher);

        Table popup = new Table();
        popup.setBackground(theme.solid(Theme.STONE));
        popup.pad(8);
        popup.defaults().growX().space(6);
        for (Move move : moves) {
            TextButton button = torchButton(moveLabel(move));
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    overlay.remove();
                    applyMove(move);
                }
            });
            popup.add(button);
            popup.row();
        }
        popup.pack();
        Vector2 center = tile.localToStageCoordinates(
                new Vector2(tile.getWidth() / 2f, tile.getHeight() / 2f));
        popup.setPosition(center.x - popup.getWidth() / 2f, center.y - popup.getHeight() / 2f);
        overlay.addActor(popup);
        stage.addActor(overlay);
    }

    private static String moveLabel(Move move) {
        return switch (move) {
            case Move.FightWithWeapon ignored -> "Use weapon";
            case Move.FightBarehanded ignored -> "Barehanded";
            case Move.TakeWeapon ignored -> "Equip";
            case Move.TakePotion ignored -> "Drink";
            case Move.AvoidRoom ignored -> "Avoid";
        };
    }

    private void applyMove(Move move) {
        MoveResult result = engine.apply(state, move);
        state = result.state();
        for (GameEvent event : result.events()) {
            String line = feedLine(event);
            if (line != null) {
                pushFeedLine(line);
            }
        }
        rebuild();
    }

    // --- the fading feed ---

    private void pushFeedLine(String text) {
        Label line = label(text, theme.body, dim(Theme.BONE, 0.9f));
        line.addAction(Actions.sequence(
                Actions.delay(4f), Actions.fadeOut(1.5f), Actions.removeActor()));
        feed.addActor(line);
        while (feed.getChildren().size > 4) {
            feed.removeActorAt(0, false);
        }
    }

    /** Events the player should read; null for ones the board already shows. */
    private static String feedLine(GameEvent event) {
        return switch (event) {
            case GameEvent.MonsterDefeated m -> {
                String name = cardName(m.monster());
                if (!m.withWeapon()) {
                    yield "Fought " + name + " barehanded — took " + m.damageTaken();
                }
                yield m.damageTaken() > 0
                        ? "Slew " + name + " — took " + m.damageTaken()
                        : "Slew " + name + " — unharmed";
            }
            case GameEvent.PotionUsed p -> p.healed() > 0
                    ? "Drank " + cardName(p.potion()) + " — healed " + p.healed()
                    : "Drank " + cardName(p.potion()) + " — already full";
            case GameEvent.PotionWasted p ->
                    cardName(p.potion()) + " wasted — one potion a turn";
            case GameEvent.WeaponEquipped w -> "Equipped " + cardName(w.weapon());
            case GameEvent.WeaponDegraded d -> d.newThreshold() <= 2
                    ? "The weapon is spent"
                    : "The weapon dulls — slays < " + d.newThreshold();
            case GameEvent.RoomAvoided ignored -> "Avoided the room";
            default -> null; // RoomDealt is visible on the board; win/loss get the overlay
        };
    }

    /** "the Queen of clubs", "the 7 of hearts" — the fonts have no suit glyphs. */
    private static String cardName(Card card) {
        String id = card.id();
        char suitChar = id.charAt(id.length() - 1);
        String suit = switch (suitChar) {
            case 'S' -> "spades";
            case 'H' -> "hearts";
            case 'D' -> "diamonds";
            case 'C' -> "clubs";
            default -> null;
        };
        if (suit == null || id.length() < 2) {
            return card.type().name().toLowerCase() + " " + card.value();
        }
        String rank = switch (id.substring(0, id.length() - 1)) {
            case "J" -> "Jack";
            case "Q" -> "Queen";
            case "K" -> "King";
            case "A" -> "Ace";
            default -> id.substring(0, id.length() - 1);
        };
        return "the " + rank + " of " + suit;
    }

    /** Rank + suit identity, bottom-right like a playing card index. */
    private Actor cardCorner(Card card, Color text) {
        Table corner = new Table();
        String id = card.id();
        char suit = id.charAt(id.length() - 1);
        if (id.length() >= 2 && "SHDC".indexOf(suit) >= 0) {
            corner.add(label(id.substring(0, id.length() - 1), theme.bodyBold, text)).padRight(4);
            corner.add(new Image(theme.suitIcon(suit, text))).size(14, 14);
        }
        return corner;
    }

    private static Color roleColor(CardType type) {
        return switch (type) {
            case MONSTER -> Theme.DRIED_BLOOD;
            case WEAPON -> Theme.IRON;
            case POTION -> Theme.HERBAL;
        };
    }

    // --- bottom strip: trophy rail and potion marker ---

    private Actor bottomStrip() {
        Table strip = new Table();
        strip.add(trophyRail()).left();
        strip.add().expandX();
        strip.add(potionMarker()).right();
        return strip;
    }

    /** Equipped weapon, its slain stack, and the degradation plate. */
    private Actor trophyRail() {
        Table rail = new Table();
        EquippedWeapon weapon = state.weapon();
        if (weapon == null) {
            rail.add(label("Barehanded", theme.body, dim(Theme.BONE, 0.6f)));
            return rail;
        }
        Table mini = new Table();
        mini.setBackground(theme.solid(Theme.IRON));
        mini.add(label(String.valueOf(weapon.weapon().value()), theme.bodyBold, Theme.SOOT));
        rail.add(mini).size(36, 50).padRight(8);
        for (Card slain : weapon.slain()) {
            Table chip = new Table();
            chip.setBackground(theme.solid(Theme.DRIED_BLOOD));
            chip.add(label(String.valueOf(slain.value()), theme.small, Theme.BONE));
            rail.add(chip).size(26, 36).padRight(4);
        }
        Table plate = new Table();
        plate.setBackground(theme.solid(Theme.TORCHLIGHT));
        plate.add(label(thresholdText(weapon), theme.bodyBold, Theme.SOOT)).pad(2, 10, 2, 10);
        rail.add(plate).padLeft(8);
        return rail;
    }

    private static String thresholdText(EquippedWeapon weapon) {
        if (weapon.threshold().isEmpty()) {
            return "slays anything";
        }
        int threshold = weapon.threshold().getAsInt();
        return threshold <= 2 ? "spent" : "slays < " + threshold;
    }

    private Actor potionMarker() {
        boolean used = state.potionsUsedThisRoom() >= rules.potionsPerTurn();
        return used
                ? label("• potion used this turn", theme.body, Theme.TORCHLIGHT)
                : label("potion ready", theme.body, dim(Theme.BONE, 0.4f));
    }

    // --- small helpers ---

    private static Label label(String text, com.badlogic.gdx.graphics.g2d.BitmapFont font, Color color) {
        return new Label(text, new LabelStyle(font, color));
    }

    private static Color dim(Color color, float alpha) {
        Color c = new Color(color);
        c.a = alpha;
        return c;
    }
}
