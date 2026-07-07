package com.tomer.scoundrel.screens;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.tomer.scoundrel.model.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Plays purely cosmetic move animations over the already-rebuilt board. The
 * state underneath is final before any motion starts, so skipping is always
 * safe: {@link #finish()} just discards the flight proxies and reveals the
 * real tiles. While a choreography plays, a fullscreen gate blocks the board
 * and any click skips.
 */
final class Choreographer {

    private final Stage stage;
    private final Theme theme;
    private final Group flightLayer = new Group();
    private final Actor gate = new Actor();
    private final List<Actor> hiddenTiles = new ArrayList<>();
    private boolean playing;

    Choreographer(Stage stage, Theme theme) {
        this.stage = stage;
        this.theme = theme;
        flightLayer.setTouchable(Touchable.disabled);
        gate.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                finish();
            }
        });
    }

    boolean isPlaying() {
        return playing;
    }

    /**
     * Deal-in: the real room tiles (already laid out) are hidden and proxies
     * fly to them — from their previous slot for the carryover card, from the
     * dungeon (the depth ticker) for freshly dealt ones. {@code roomTiles}
     * must iterate in room order for the stagger to read left-to-right.
     */
    void playDealIn(Map<Card, Table> roomTiles, Map<String, Vector2> previousSlots, Vector2 dungeonSource) {
        finish();
        playing = true;
        gate.setBounds(0, 0, stage.getWidth(), stage.getHeight());
        stage.addActor(flightLayer);
        stage.addActor(gate);

        float longest = 0;
        int slot = 0;
        for (Map.Entry<Card, Table> entry : roomTiles.entrySet()) {
            Card card = entry.getKey();
            Table tile = entry.getValue();
            Vector2 destination = tile.localToStageCoordinates(new Vector2(0, 0));
            Vector2 from = previousSlots.get(card.id());
            float delay = slot * Theme.DEAL_STAGGER;

            tile.setVisible(false);
            hiddenTiles.add(tile);

            Table proxy = CardTiles.build(theme, card);
            proxy.setSize(tile.getWidth(), tile.getHeight());
            proxy.setTransform(true);
            proxy.setOrigin(tile.getWidth() / 2f, tile.getHeight() / 2f);
            if (from != null) {
                // The carryover card slides from where it just was.
                proxy.setPosition(from.x, from.y);
                proxy.addAction(Actions.delay(delay, Actions.moveTo(
                        destination.x, destination.y, Theme.DEAL_DURATION, Interpolation.pow2Out)));
            } else {
                // Fresh cards emerge from the dungeon — the depth ticker.
                proxy.setPosition(dungeonSource.x - tile.getWidth() / 2f,
                        dungeonSource.y - tile.getHeight() / 2f);
                proxy.setScale(0.2f);
                proxy.getColor().a = 0f;
                proxy.addAction(Actions.delay(delay, Actions.parallel(
                        Actions.moveTo(destination.x, destination.y, Theme.DEAL_DURATION, Interpolation.pow2Out),
                        Actions.scaleTo(1f, 1f, Theme.DEAL_DURATION, Interpolation.pow2Out),
                        Actions.fadeIn(Theme.DEAL_DURATION * 0.6f))));
            }
            flightLayer.addActor(proxy);
            longest = Math.max(longest, delay + Theme.DEAL_DURATION);
            slot++;
        }
        flightLayer.addAction(Actions.delay(longest, Actions.run(this::finish)));
    }

    /**
     * Ends any running choreography immediately — natural completion and a
     * skip click both land here. The board underneath is already final.
     */
    void finish() {
        if (!playing) {
            return;
        }
        playing = false;
        flightLayer.clearActions();
        flightLayer.clearChildren();
        flightLayer.remove();
        gate.remove();
        for (Actor tile : hiddenTiles) {
            tile.setVisible(true);
        }
        hiddenTiles.clear();
    }
}
