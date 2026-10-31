package com.clientmod;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

/** Draws the PNG icons from assets/clientmod/textures/gui. */
public final class Icons {
    private static final Map<String, Identifier> CACHE = new HashMap<>();
    private static final Identifier LOGO = Identifier.of("clientmod", "textures/gui/logo.png");

    private static final Identifier BG = Identifier.of("clientmod", "textures/gui/menu_bg.png");

    private Icons() {}

    /** Full-screen background image (cover scaling). */
    public static void background(DrawContext c, int w, int h) {
        float sc = Math.max(w / 1468f, h / 812f);
        int rw = Math.min(1468, Math.round(w / sc)), rh = Math.min(812, Math.round(h / sc));
        float u = (1468 - rw) / 2f, v = (812 - rh) / 2f;
        c.drawTexture(RenderPipelines.GUI_TEXTURED, BG, 0, 0, u, v, w, h, rw, rh, 1468, 812);
    }

    public static void draw(DrawContext c, String name, int x, int y, int size) {
        Identifier id = CACHE.computeIfAbsent(name, n -> Identifier.of("clientmod", "textures/gui/icons/" + n + ".png"));
        c.drawTexture(RenderPipelines.GUI_TEXTURED, id, x, y, 0f, 0f, size, size, 64, 64, 64, 64);
    }

    public static void logo(DrawContext c, int x, int y, int size) {
        c.drawTexture(RenderPipelines.GUI_TEXTURED, LOGO, x, y, 0f, 0f, size, size, 256, 256, 256, 256);
    }
}
