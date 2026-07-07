package com.tomer.scoundrel.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.tomer.scoundrel.ScoundrelGame;
import com.tomer.scoundrel.model.Status;
import com.tomer.scoundrel.runs.HighScores;
import com.tomer.scoundrel.runs.RunLog;
import com.tomer.scoundrel.runs.RunRecord;
import com.tomer.scoundrel.runs.RunTotals;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import static com.tomer.scoundrel.screens.Widgets.dim;
import static com.tomer.scoundrel.screens.Widgets.label;
import static com.tomer.scoundrel.screens.Widgets.torchButton;

/**
 * THE LEDGER — the top runs and lifetime totals, read once from the run log
 * on entry. Totals are labeled "finished runs" deliberately: abandoned games
 * are never recorded, so finished games are the whole universe.
 */
public final class RecordsScreen extends ScreenAdapter {

    private static final String[] ROMAN =
            {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
    private static final DateTimeFormatter DAY =
            DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH).withZone(ZoneId.systemDefault());

    private final Stage stage;

    public RecordsScreen(ScoundrelGame game, Theme theme, RunLog runLog) {
        stage = new Stage(new FitViewport(Theme.WORLD_WIDTH, Theme.WORLD_HEIGHT));
        List<RunRecord> records = readSafely(runLog);

        Table root = new Table();
        root.setFillParent(true);
        root.top().pad(28, 56, 24, 56);
        stage.addActor(root);

        root.add(label("THE LEDGER", theme.title, Theme.BONE)).colspan(2).padBottom(24);
        root.row();
        if (records.isEmpty()) {
            root.add(label("No runs recorded yet — the dungeon awaits.",
                    theme.body, dim(Theme.BONE, 0.6f))).colspan(2).expand();
            root.row();
        } else {
            root.add(scoreTable(theme, records)).top().expandX().left();
            root.add(totalsColumn(theme, RunTotals.of(records))).top().padLeft(56);
            root.row();
            root.add().expand().colspan(2);
            root.row();
        }
        TextButton back = torchButton(theme, "Back");
        back.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.showTitle();
            }
        });
        root.add(back).left().colspan(2);
    }

    private static List<RunRecord> readSafely(RunLog runLog) {
        try {
            return runLog.readAll();
        } catch (RuntimeException e) {
            Gdx.app.error("scoundrel", "failed to read the run log", e);
            return List.of();
        }
    }

    private Table scoreTable(Theme theme, List<RunRecord> records) {
        Table table = new Table();
        List<RunRecord> top = HighScores.top(records, ROMAN.length);
        for (int i = 0; i < top.size(); i++) {
            RunRecord run = top.get(i);
            boolean won = run.outcome() == Status.WON;
            table.add(label(ROMAN[i], theme.bodyBold, Theme.TORCHLIGHT)).right().padRight(20);
            table.add(label(String.valueOf(run.score()), theme.bodyBold,
                    run.score() < 0 ? Theme.DRIED_BLOOD : Theme.BONE)).right().padRight(28);
            table.add(label(won ? "cleared" : "defeated", theme.body,
                    won ? dim(Theme.TORCHLIGHT, 0.9f) : dim(Theme.BONE, 0.5f))).left().padRight(28);
            table.add(label(DAY.format(run.endedAt()), theme.small, dim(Theme.BONE, 0.5f)))
                    .left().padRight(28);
            table.add(label(duration(run.seconds()), theme.small, dim(Theme.BONE, 0.5f)))
                    .left().padRight(28);
            table.add(label(run.monstersDefeated() + " slain", theme.small, dim(Theme.BONE, 0.5f)))
                    .left();
            table.row();
            table.add(new Image(theme.solid(dim(Theme.BONE, 0.13f))))
                    .colspan(6).growX().height(1).padTop(7).padBottom(7);
            table.row();
        }
        return table;
    }

    private Table totalsColumn(Theme theme, RunTotals totals) {
        Table side = new Table();
        side.add(label("ACROSS " + totals.runs() + " FINISHED RUNS",
                theme.bodyBold, Theme.TORCHLIGHT)).colspan(2).left().padBottom(14);
        side.row();
        stat(side, theme, "cleared", totals.wins() + "  (" + Math.round(totals.winRate() * 100) + "%)");
        stat(side, theme, "defeated", String.valueOf(totals.losses()));
        stat(side, theme, "monsters slain", String.valueOf(totals.monstersDefeated()));
        stat(side, theme, "damage taken", String.valueOf(totals.damageTaken()));
        stat(side, theme, "health healed", String.valueOf(totals.healthHealed()));
        stat(side, theme, "potions drunk", totals.potionsDrunk() + "  (" + totals.potionsWasted() + " wasted)");
        stat(side, theme, "weapons equipped", String.valueOf(totals.weaponsEquipped()));
        stat(side, theme, "rooms avoided", String.valueOf(totals.roomsAvoided()));
        stat(side, theme, "time below", duration(totals.secondsPlayed()));
        return side;
    }

    private void stat(Table side, Theme theme, String name, String value) {
        side.add(label(name, theme.body, dim(Theme.BONE, 0.55f))).left().padRight(28).padBottom(5);
        side.add(label(value, theme.bodyBold, Theme.BONE)).right().padBottom(5);
        side.row();
    }

    private static String duration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long rest = seconds % 60;
        return hours > 0 ? hours + "h " + minutes + "m" : minutes + ":" + String.format("%02d", rest);
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
            return;
        }
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
