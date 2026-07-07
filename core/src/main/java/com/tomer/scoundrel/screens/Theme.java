package com.tomer.scoundrel.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;

import java.util.HashMap;
import java.util.Map;

/**
 * The torchlit-dungeon look of the plain UI: palette, fonts, and flat
 * drawables. Every visual decision derives from here so the later sprite
 * pass swaps assets, not screen code. Must be created and disposed on the
 * GL thread.
 */
public final class Theme implements Disposable {

    // Palette (see docs: torchlit dungeon mood).
    public static final Color SOOT = Color.valueOf("17130f");
    public static final Color STONE = Color.valueOf("241d16");
    public static final Color DRIED_BLOOD = Color.valueOf("8c2f22");
    public static final Color IRON = Color.valueOf("7a8794");
    public static final Color HERBAL = Color.valueOf("5d8a4a");
    public static final Color TORCHLIGHT = Color.valueOf("d9a441");
    public static final Color BONE = Color.valueOf("e8ddc7");

    // Motion tokens (seconds) — the art pass tunes these in one place.
    public static final float DEAL_DURATION = 0.28f;
    public static final float DEAL_STAGGER = 0.07f;
    public static final float SWEEP_DURATION = 0.30f;

    /** Characters beyond the freetype defaults used by the UI copy. */
    private static final String EXTRA_CHARS = "—–×•";

    /** IM Fell English — card values and other large set pieces. */
    public final BitmapFont display;
    /** IM Fell English — overlay titles and the wordmark. */
    public final BitmapFont title;
    /** Alegreya Sans — HUD labels, buttons, feed lines. */
    public final BitmapFont body;
    /** Alegreya Sans Bold — numbers and emphasized labels. */
    public final BitmapFont bodyBold;
    /** Alegreya Sans — the smallest text: feed detail, card corners. */
    public final BitmapFont small;

    private final Texture white;
    private final Map<Character, Texture> suitTextures = new HashMap<>();

    public Theme() {
        FreeTypeFontGenerator fell =
                new FreeTypeFontGenerator(Gdx.files.internal("fonts/IMFellEnglish-Regular.ttf"));
        FreeTypeFontGenerator sans =
                new FreeTypeFontGenerator(Gdx.files.internal("fonts/AlegreyaSans-Regular.ttf"));
        FreeTypeFontGenerator sansBold =
                new FreeTypeFontGenerator(Gdx.files.internal("fonts/AlegreyaSans-Bold.ttf"));
        display = generate(fell, 64);
        title = generate(fell, 42);
        body = generate(sans, 18);
        bodyBold = generate(sansBold, 18);
        small = generate(sans, 14);
        fell.dispose();
        sans.dispose();
        sansBold.dispose();

        Pixmap pixel = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixel.setColor(Color.WHITE);
        pixel.fill();
        white = new Texture(pixel);
        pixel.dispose();

        suitTextures.put('S', suitTexture('S'));
        suitTextures.put('H', suitTexture('H'));
        suitTextures.put('D', suitTexture('D'));
        suitTextures.put('C', suitTexture('C'));
    }

    /** A flat rectangle of the given color, stretchable to any size. */
    public Drawable solid(Color color) {
        return new TextureRegionDrawable(new TextureRegion(white)).tint(color);
    }

    /** Suit shape for a card id's suit letter (S/H/D/C), tinted. */
    public Drawable suitIcon(char suitLetter, Color tint) {
        Texture texture = suitTextures.get(suitLetter);
        if (texture == null) {
            throw new IllegalArgumentException("Unknown suit letter: " + suitLetter);
        }
        return new TextureRegionDrawable(new TextureRegion(texture)).tint(tint);
    }

    private static BitmapFont generate(FreeTypeFontGenerator generator, int size) {
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = size;
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + EXTRA_CHARS;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;
        return generator.generateFont(parameter);
    }

    /**
     * The bundled fonts have no suit glyphs, so the four suits are drawn as
     * simple shapes on a 64px canvas (white, tinted at use).
     */
    private static Texture suitTexture(char suitLetter) {
        Pixmap p = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        p.setColor(Color.WHITE);
        switch (suitLetter) {
            case 'H' -> { // two lobes over a point
                p.fillCircle(19, 20, 14);
                p.fillCircle(45, 20, 14);
                p.fillTriangle(5, 26, 59, 26, 32, 60);
            }
            case 'D' -> {
                p.fillTriangle(32, 2, 6, 32, 58, 32);
                p.fillTriangle(6, 32, 58, 32, 32, 62);
            }
            case 'S' -> { // inverted heart plus a flared stem
                p.fillTriangle(32, 2, 4, 34, 60, 34);
                p.fillCircle(18, 38, 14);
                p.fillCircle(46, 38, 14);
                p.fillTriangle(32, 42, 22, 62, 42, 62);
            }
            case 'C' -> { // three lobes plus a flared stem
                p.fillCircle(32, 16, 13);
                p.fillCircle(18, 36, 13);
                p.fillCircle(46, 36, 13);
                p.fillTriangle(32, 40, 22, 62, 42, 62);
            }
            default -> throw new IllegalArgumentException("Unknown suit letter: " + suitLetter);
        }
        Texture texture = new Texture(p);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        p.dispose();
        return texture;
    }

    @Override
    public void dispose() {
        display.dispose();
        title.dispose();
        body.dispose();
        bodyBold.dispose();
        small.dispose();
        white.dispose();
        suitTextures.values().forEach(Texture::dispose);
    }
}
