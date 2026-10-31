package com.clientmod;

import net.minecraft.client.gui.DrawContext;

/** Small drawing helpers: smooth rounded rectangles, rings and pills. */
public final class Gfx {
    private Gfx() {}

    public static int alpha(int c, float mul) {
        int a = (int) (((c >>> 24) & 255) * Math.max(0f, Math.min(1f, mul)));
        return (a << 24) | (c & 0xFFFFFF);
    }

    /** Horizontal inset of row j for a rectangle of height h with corner radius r. */
    private static float inset(int j, int h, int r) {
        int jj = Math.min(j, h - 1 - j);
        if (jj >= r || r <= 0) return 0f;
        float dy = r - jj - 0.5f;
        return r - (float) Math.sqrt(Math.max(0f, r * (float) r - dy * dy));
    }

    public static void rrect(DrawContext c, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0) return;
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        for (int j = 0; j < h; j++) {
            float e = inset(j, h, r);
            int ie = (int) Math.floor(e);
            float cov = 1f - (e - ie);
            int x0 = x + ie, x1 = x + w - ie;
            if (cov < 0.97f && x1 - x0 > 2) {
                int edge = alpha(color, cov);
                c.fill(x0, y + j, x0 + 1, y + j + 1, edge);
                c.fill(x1 - 1, y + j, x1, y + j + 1, edge);
                x0++;
                x1--;
            }
            if (x1 > x0) c.fill(x0, y + j, x1, y + j + 1, color);
        }
    }

    /** 1px outline following the rounded shape. */
    public static void ring(DrawContext c, int x, int y, int w, int h, int r, int color) {
        if (w <= 2 || h <= 2) return;
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        for (int j = 0; j < h; j++) {
            int io = Math.round(inset(j, h, r));
            int x0 = x + io, x1 = x + w - io;
            if (j == 0 || j == h - 1) {
                c.fill(x0, y + j, x1, y + j + 1, color);
                continue;
            }
            int ii = 1 + Math.round(inset(j - 1, h - 2, Math.max(0, r - 1)));
            int lx = Math.max(x0 + 1, x + ii);
            int rx = Math.min(x1 - 1, x + w - ii);
            c.fill(x0, y + j, lx, y + j + 1, color);
            c.fill(rx, y + j, x1, y + j + 1, color);
        }
    }

    public static void pill(DrawContext c, int x, int y, int w, int h, int color) {
        rrect(c, x, y, w, h, h / 2, color);
    }
}
