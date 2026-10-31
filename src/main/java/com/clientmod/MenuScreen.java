package com.clientmod;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Client menu, laid out 1:1 after the reference image (1315 x 585 window, coordinates in image pixels). */
public class MenuScreen extends Screen {
    private static final int TXT = 0xFFE8E9EC, SUB = 0xFF868A92, NAVC = 0xFFB9BCC2, DIM = 0xFF5B5E66;
    private static final String[][] NAV = {{"Modules", "grid"}, {"Appearance", "brush"}, {"Profiles", "user"}, {"Settings", "cog"}};
    private static final String[] CATS = {"All", "HUD", "Visual", "Utility"};

    private interface Act { void run(double mx, double my); }

    private static final class Hit {
        final int x, y, w, h;
        final Act click, drag;
        final boolean content;

        Hit(int x, int y, int w, int h, Act click, Act drag, boolean content) {
            this.x = x; this.y = y; this.w = w; this.h = h;
            this.click = click; this.drag = drag; this.content = content;
        }

        boolean in(double mx, double my) { return mx >= x && mx < x + w && my >= y && my < y + h; }
    }

    private final List<Hit> hits = new ArrayList<>();
    private Hit dragging;
    private float s = 0.45f;
    private int wx, wy, ww, wh, cx, cy, cw, ch;
    private String page = "Modules", cat = "All";
    private Module sel;
    private double scroll, maxScroll;
    private int accent = 0xFFE1E4EB;

    public MenuScreen() {
        super(Text.literal("Client"));
    }

    private int p(float v) { return Math.round(v * s); }

    @Override
    protected void init() {
        s = Math.min(width * 0.896f / 1315f, height * 0.72f / 585f);
        ww = p(1315);
        wh = p(585);
        wx = Math.round(width * 0.501f - ww / 2f);
        wy = Math.round(height * 0.508f - wh / 2f);
        cx = wx + p(260);
        cw = p(1035);
        cy = wy + p(84);
        ch = wh - p(84) - p(22);
    }

    // ------------------------------------------------------------ helpers

    private int tw(String str, float sc) { return Math.round(textRenderer.getWidth(str) * sc); }

    /** Draws text with its vertical center at centerY. */
    private void txt(DrawContext c, String str, int x, float centerY, float sc, int col) {
        var m = c.getMatrices();
        m.pushMatrix();
        m.translate(x, centerY - 4f * sc);
        m.scale(sc, sc);
        c.drawText(textRenderer, Text.literal(str), 0, 0, col, false);
        m.popMatrix();
    }

    private void rect(DrawContext c, int x, int y, int w, int h, int col) { c.fill(x, y, x + w, y + h, col); }

    private boolean inContent(double mx, double my) {
        return mx >= cx - p(6) && mx < cx + cw + p(6) && my >= cy && my < cy + ch;
    }

    private void hit(int x, int y, int w, int h, Act click, Act drag) {
        if (y + h < cy || y > cy + ch) return;
        hits.add(new Hit(x, y, w, h, click, drag, true));
    }

    private void fixed(int x, int y, int w, int h, Act click) { hits.add(new Hit(x, y, w, h, click, null, false)); }

    private void toggle(DrawContext c, int x, int y, boolean on) {
        int w = p(38), h = p(20), k = p(14), pad = (h - k) / 2;
        Gfx.pill(c, x, y, w, h, on ? 0x58FFFFFF : 0x2AFFFFFF);
        Gfx.rrect(c, x + (on ? w - pad - k : pad), y + pad, k, k, k / 2, on ? 0xFFB4B7BD : 0xFF777B80);
    }

    private void checker(DrawContext c, int x, int y, int w, int h) {
        int q = Math.max(3, p(8));
        for (int yy = 0; yy < h; yy += q)
            for (int xx = 0; xx < w; xx += q)
                c.fill(x + xx, y + yy, Math.min(x + xx + q, x + w), Math.min(y + yy + q, y + h),
                        (((xx / q) + (yy / q)) & 1) == 0 ? 0xFF6A6A6A : 0xFF3A3A3A);
    }

    // ------------------------------------------------------------ render

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        super.render(c, mx, my, delta);
        hits.clear();
        Module menu = ModuleManager.MENU;
        accent = menu.c("accent") | 0xFF000000;
        boolean bg = menu.b("bg");
        if (bg) Icons.background(c, width, height);

        int R = p(28);
        if (bg) {
            Gfx.rrect(c, wx, wy, ww, wh, R, 0x0DFFFFFF);
        } else {
            int a = Math.max(0, Math.min(255, Math.round(menu.n("opacity") * 2.55f)));
            Gfx.rrect(c, wx, wy, ww, wh, R, (a << 24) | 0x0B0C10);
        }
        Gfx.ring(c, wx, wy, ww, wh, R, 0x1EFFFFFF);

        // sidebar + content panel
        Gfx.rrect(c, wx, wy, p(247), wh, R, 0x08FFFFFF);
        int px0 = wx + p(234), pw0 = ww - p(234) - p(2);
        Gfx.rrect(c, px0, wy + p(2), pw0, wh - p(4), p(26), 0x05FFFFFF);
        Gfx.ring(c, px0, wy + p(2), pw0, wh - p(4), p(26), 0x14FFFFFF);

        Icons.logo(c, wx + p(115) - p(38), wy + p(59) - p(38), p(76));

        // navigation
        for (int i = 0; i < NAV.length; i++) {
            String name = NAV[i][0];
            int ry = 117 + 51 * i;
            int x = wx + p(14), y = wy + p(ry), w = p(204), h = p(48);
            boolean on = page.equals(name);
            boolean hov = mx >= x && mx < x + w && my >= y && my < y + h;
            if (on) Gfx.rrect(c, x, y, w, h, p(10), 0x12FFFFFF);
            else if (hov) Gfx.rrect(c, x, y, w, h, p(10), 0x08FFFFFF);
            Icons.draw(c, NAV[i][1], wx + p(41) - p(11), wy + p(ry + 24) - p(11), p(22));
            txt(c, name, wx + p(71), wy + p(ry + 24), 1.7f * s, on ? TXT : NAVC);
            fixed(x, y, w, h, (hx, hy) -> { page = name; sel = null; scroll = 0; });
        }
        Gfx.rrect(c, wx + p(34) - p(5), wy + p(549) - p(5), p(10), p(10), p(5), 0xFF86898F);
        txt(c, "Client v1.0", wx + p(46), wy + p(549), 1.2f * s, SUB);
        rect(c, wx + p(127), wy + p(549), p(30), Math.max(1, p(1.5f)), 0x30FFFFFF);

        // header
        String icon = page.equals("Modules") ? "grid" : page.equals("Appearance") ? "brush" : page.equals("Profiles") ? "user" : "cog";
        Icons.draw(c, icon, wx + p(270) - p(10), wy + p(43) - p(10), p(20));
        txt(c, page, wx + p(291), wy + p(43), 2.3f * s, TXT);
        Icons.draw(c, "close", wx + p(1274) - p(9), wy + p(42) - p(9), p(18));
        fixed(wx + p(1274) - p(16), wy + p(42) - p(16), p(32), p(32), (hx, hy) -> client.setScreen(null));
        rect(c, wx + p(1225) - p(8), wy + p(42), p(16), Math.max(1, p(2)), 0xFFB9BCC2);
        fixed(wx + p(1225) - p(14), wy + p(42) - p(14), p(28), p(28), (hx, hy) -> client.setScreen(null));

        if (page.equals("Modules") && sel == null) {
            int tx = wx + p(520);
            for (String k : CATS) {
                int w = tw(k, 1.4f * s) + p(28), h = p(28);
                boolean on = cat.equals(k);
                Gfx.rrect(c, tx, wy + p(43) - h / 2, w, h, h / 2, on ? 0x26FFFFFF : 0x0AFFFFFF);
                txt(c, k, tx + p(14), wy + p(43), 1.4f * s, on ? TXT : SUB);
                fixed(tx, wy + p(43) - h / 2, w, h, (hx, hy) -> { cat = k; scroll = 0; });
                tx += w + p(8);
            }
        }

        // content
        c.enableScissor(cx - p(6), cy, cx + cw + p(6), cy + ch);
        int y = cy + p(11) - (int) scroll;
        int end;
        switch (page) {
            case "Appearance" -> end = settings(c, menu, y, mx, my);
            case "Profiles" -> end = profiles(c, y);
            case "Settings" -> end = settingsPage(c, y, mx, my);
            default -> end = sel != null ? detail(c, sel, y, mx, my) : grid(c, y, mx, my);
        }
        c.disableScissor();

        maxScroll = Math.max(0, (end - (cy + p(11) - (int) scroll)) - ch + p(30));
        scroll = Math.max(0, Math.min(maxScroll, scroll));
    }

    // ------------------------------------------------------------ pages

    private int profiles(DrawContext c, int y) {
        Icons.draw(c, "user", cx + cw / 2 - p(24), y + p(80), p(48));
        String str = "Profiles are coming soon.";
        txt(c, str, cx + cw / 2 - tw(str, 1.6f * s) / 2, y + p(170), 1.6f * s, SUB);
        return y + p(240);
    }

    private int settingsPage(DrawContext c, int y, int mx, int my) {
        txt(c, "GENERAL", cx + p(8), y + p(8), 1.2f * s, SUB);
        y += p(34);
        txt(c, "Open menu key", cx + p(8), y + p(26), 1.55f * s, TXT);
        String k = "Options > Controls > Client Mod";
        txt(c, k, cx + cw - p(8) - tw(k, 1.4f * s), y + p(26), 1.4f * s, SUB);
        y += p(60);
        String label = "Open HUD editor";
        int bw = tw(label, 1.5f * s) + p(70), bh = p(40);
        boolean hov = mx >= cx + p(8) && mx < cx + p(8) + bw && my >= y && my < y + bh && inContent(mx, my);
        Gfx.rrect(c, cx + p(8), y, bw, bh, p(12), hov ? 0x20FFFFFF : 0x12FFFFFF);
        Gfx.ring(c, cx + p(8), y, bw, bh, p(12), 0x1CFFFFFF);
        Icons.draw(c, "layout", cx + p(24), y + bh / 2 - p(10), p(20));
        txt(c, label, cx + p(56), y + bh / 2f, 1.5f * s, TXT);
        hit(cx + p(8), y, bw, bh, (hx, hy) -> client.setScreen(new HudEditorScreen()), null);
        return y + p(70);
    }

    private int grid(DrawContext c, int y, int mx, int my) {
        int gap = p(22);
        int cardW = (cw - 2 * gap) / 3, cardH = p(118), pitch = p(139);
        int i = 0;
        for (Module m : ModuleManager.all()) {
            if (!cat.equals("All") && !m.category.equals(cat)) continue;
            int x = cx + (i % 3) * (cardW + gap);
            int yy = y + (i / 3) * pitch;
            card(c, m, x, yy, cardW, cardH, mx, my);
            i++;
        }
        return y + ((i + 2) / 3) * pitch;
    }

    private void card(DrawContext c, Module m, int x, int y, int w, int h, int mx, int my) {
        boolean hov = mx >= x && mx < x + w && my >= y && my < y + h && inContent(mx, my);
        int r = p(16);
        Gfx.rrect(c, x, y, w, h, r, hov ? 0x0FFFFFFF : 0x08FFFFFF);
        Gfx.ring(c, x, y, w, h, r, m.enabled && m.implemented ? 0x26FFFFFF : 0x16FFFFFF);
        Icons.draw(c, m.id, x + p(42) - p(16), y + p(35) - p(16), p(32));
        txt(c, m.name, x + p(84), y + p(35), 1.7f * s, m.implemented ? TXT : 0xFF9A9DA6);
        txt(c, m.desc, x + p(84), y + p(62), 1.25f * s, SUB);
        Icons.draw(c, "arrow", x + w - p(26) - p(7), y + p(96) - p(7), p(14));
        hit(x, y, w, h, (hx, hy) -> { sel = m; scroll = 0; }, null);
        if (m.implemented) {
            toggle(c, x + w - p(23) - p(38), y + p(25), m.enabled);
            hit(x + w - p(23) - p(38) - p(6), y + p(19), p(50), p(32), (hx, hy) -> m.enabled = !m.enabled, null);
        } else {
            txt(c, "SOON", x + w - p(23) - tw("SOON", 1.2f * s), y + p(35), 1.2f * s, DIM);
        }
    }

    private int detail(DrawContext c, Module m, int y, int mx, int my) {
        Icons.draw(c, "back", cx + p(4), y + p(4), p(20));
        txt(c, "Modules", cx + p(32), y + p(14), 1.5f * s, SUB);
        hit(cx, y, p(130), p(30), (hx, hy) -> { sel = null; scroll = 0; }, null);
        y += p(44);

        int hh = p(100);
        Gfx.rrect(c, cx, y, cw, hh, p(16), 0x0AFFFFFF);
        Gfx.ring(c, cx, y, cw, hh, p(16), 0x18FFFFFF);
        Icons.draw(c, m.id, cx + p(26), y + hh / 2 - p(20), p(40));
        txt(c, m.name, cx + p(84), y + p(36), 1.9f * s, TXT);
        txt(c, m.desc, cx + p(84), y + p(66), 1.3f * s, SUB);
        if (m.implemented) {
            int tx = cx + cw - p(30) - p(38);
            toggle(c, tx, y + hh / 2 - p(10), m.enabled);
            hit(tx - p(6), y + hh / 2 - p(18), p(52), p(36), (hx, hy) -> m.enabled = !m.enabled, null);
            int rw = tw("Reset", 1.4f * s) + p(34), rx = tx - p(24) - rw;
            Gfx.rrect(c, rx, y + hh / 2 - p(15), rw, p(30), p(15), 0x14FFFFFF);
            txt(c, "Reset", rx + p(17), y + hh / 2f, 1.4f * s, TXT);
            hit(rx, y + hh / 2 - p(15), rw, p(30), (hx, hy) -> { for (Setting st : m.all()) st.reset(); }, null);
        }
        y += hh + p(20);

        if (!m.implemented) {
            txt(c, "This module is planned and not built yet.", cx + p(8), y + p(12), 1.5f * s, SUB);
            return y + p(50);
        }
        if (m.id.equals("itf")) {
            ItemStack held = client != null && client.player != null ? client.player.getMainHandStack() : ItemStack.EMPTY;
            if (held.isEmpty()) {
                txt(c, "Hold an item in your hand to edit it.", cx + p(8), y + p(12), 1.5f * s, SUB);
            } else {
                c.drawItem(held, cx + p(8), y);
                txt(c, "Editing: " + held.getName().getString(), cx + p(8) + p(40), y + p(8), 1.5f * s, TXT);
            }
            y += p(50);
        }
        y = settings(c, m, y, mx, my);
        if (m.id.equals("wp")) y = waypointList(c, y, mx, my);
        return y;
    }

    private int waypointList(DrawContext c, int y, int mx, int my) {
        txt(c, "YOUR WAYPOINTS", cx + p(8), y + p(8), 1.2f * s, SUB);
        y += p(34);
        String add = "Add waypoint here";
        int bw = tw(add, 1.4f * s) + p(40), bh = p(34);
        Gfx.rrect(c, cx + p(8), y, bw, bh, p(12), 0x14FFFFFF);
        Gfx.ring(c, cx + p(8), y, bw, bh, p(12), 0x1CFFFFFF);
        txt(c, add, cx + p(28), y + bh / 2f, 1.4f * s, TXT);
        hit(cx + p(8), y, bw, bh, (hx, hy) -> Waypoints.addHere("Waypoint " + (Waypoints.list.size() + 1)), null);
        y += bh + p(14);
        for (Waypoints.Wp w : new ArrayList<>(Waypoints.list)) {
            Gfx.rrect(c, cx + p(8), y, cw - p(16), p(44), p(12), 0x08FFFFFF);
            txt(c, w.name == null ? "Waypoint" : w.name, cx + p(26), y + p(22), 1.5f * s, TXT);
            String pos = Math.round(w.x) + ", " + Math.round(w.y) + ", " + Math.round(w.z);
            txt(c, pos, cx + p(300), y + p(22), 1.35f * s, SUB);
            Icons.draw(c, "close", cx + cw - p(16) - p(32), y + p(22) - p(8), p(16));
            hit(cx + cw - p(16) - p(40), y, p(40), p(44), (hx, hy) -> Waypoints.remove(w), null);
            y += p(52);
        }
        return y + p(10);
    }

    // ------------------------------------------------------------ settings

    private int settings(DrawContext c, Module m, int y, int mx, int my) {
        for (Map.Entry<String, List<Setting>> g : m.groups.entrySet()) {
            String name = g.getKey().toUpperCase();
            txt(c, name, cx + p(8), y + p(8), 1.2f * s, SUB);
            int w = tw(name, 1.2f * s);
            rect(c, cx + p(8) + w + p(14), y + p(8), cw - w - p(30), Math.max(1, p(1.5f)), 0x14FFFFFF);
            y += p(34);
            for (Setting st : g.getValue()) y = row(c, st, y, mx, my);
            y += p(14);
        }
        return y;
    }

    private int row(DrawContext c, Setting st, int y, int mx, int my) {
        int rh = p(52), right = cx + cw - p(8);
        txt(c, st.label, cx + p(8), y + rh / 2f, 1.55f * s, TXT);

        if (st instanceof Setting.Bool b) {
            int tx = right - p(38);
            toggle(c, tx, y + rh / 2 - p(10), b.value);
            hit(tx - p(8), y, p(54), rh, (hx, hy) -> b.value = !b.value, null);
            return y + rh;
        }
        if (st instanceof Setting.Num n) {
            int trackW = p(260), tx = right - trackW - p(110), ty = y + rh / 2 - p(3);
            Gfx.rrect(c, tx, ty, trackW, p(6), p(3), 0x30FFFFFF);
            int fw = Math.round(trackW * Math.max(0f, Math.min(1f, n.fraction())));
            Gfx.rrect(c, tx, ty, Math.max(p(6), fw), p(6), p(3), accent);
            int k = p(18);
            Gfx.rrect(c, Math.max(tx, Math.min(tx + trackW - k, tx + fw - k / 2)), ty + p(3) - k / 2, k, k, k / 2, 0xFFFFFFFF);
            txt(c, n.fmt(), tx + trackW + p(18), y + rh / 2f, 1.4f * s, SUB);
            Act set = (hx, hy) -> n.set(n.min + (float) Math.max(0, Math.min(1, (hx - tx) / (double) trackW)) * (n.max - n.min));
            hit(tx - p(8), y, trackW + p(16), rh, set, set);
            return y + rh;
        }
        if (st instanceof Setting.Choice ch2) {
            int total = 0;
            for (String o : ch2.opts) total += tw(o, 1.4f * s) + p(32) + p(8);
            int x = right - total + p(8), ph = p(30);
            for (int i = 0; i < ch2.opts.length; i++) {
                int pw = tw(ch2.opts[i], 1.4f * s) + p(32);
                boolean on = ch2.value == i;
                Gfx.rrect(c, x, y + rh / 2 - ph / 2, pw, ph, ph / 2, on ? 0x30FFFFFF : 0x0EFFFFFF);
                txt(c, ch2.opts[i], x + p(16), y + rh / 2f, 1.4f * s, on ? TXT : SUB);
                final int idx = i;
                hit(x, y + rh / 2 - ph / 2, pw, ph, (hx, hy) -> ch2.value = idx, null);
                x += pw + p(8);
            }
            return y + rh;
        }
        if (st instanceof Setting.Color col) {
            int sw = p(64), sh = p(30), sx = right - sw, sy = y + rh / 2 - sh / 2;
            checker(c, sx, sy, sw, sh);
            Gfx.rrect(c, sx, sy, sw, sh, p(10), col.argb());
            Gfx.ring(c, sx - 1, sy - 1, sw + 2, sh + 2, p(11), 0x66FFFFFF);
            String hex = String.format("#%08X", col.argb());
            txt(c, hex, sx - p(18) - tw(hex, 1.4f * s), y + rh / 2f, 1.4f * s, SUB);
            hit(sx - p(220), y, sw + p(230), rh, (hx, hy) -> col.open = !col.open, null);
            y += rh;
            if (col.open) y = picker(c, col, y);
            return y;
        }
        return y + rh;
    }

    // ------------------------------------------------------------ color picker

    private int picker(DrawContext c, Setting.Color col, int y) {
        int pad = p(18), pw = p(420), svh = p(190), bar = p(18);
        int px = cx + pad + p(8), top = y + pad;
        int totalH = pad + svh + p(26) + bar + p(22) + bar + p(30) + p(34) + pad;
        Gfx.rrect(c, cx + p(8), y, pw + 2 * pad + p(170), totalH, p(14), 0x0AFFFFFF);
        Gfx.ring(c, cx + p(8), y, pw + 2 * pad + p(170), totalH, p(14), 0x18FFFFFF);

        int step = Math.max(2, p(4));
        for (int i = 0; i < pw; i += step) {
            float sat = i / (float) (pw - 1);
            int topCol = 0xFF000000 | Setting.Color.hsv(col.h, sat, 1f);
            c.fillGradient(px + i, top, px + Math.min(pw, i + step), top + svh, topCol, 0xFF000000);
        }
        int mxp = px + Math.round(col.s * (pw - 1)), myp = top + Math.round((1 - col.v) * (svh - 1));
        int k = p(16);
        Gfx.rrect(c, mxp - k / 2, myp - k / 2, k, k, k / 2, 0xFFFFFFFF);
        Gfx.rrect(c, mxp - k / 2 + 2, myp - k / 2 + 2, k - 4, k - 4, (k - 4) / 2, 0xFF000000 | col.rgb());
        final int ftop = top;
        Act sv = (hx, hy) -> {
            col.s = (float) Math.max(0, Math.min(1, (hx - px) / (pw - 1)));
            col.v = 1f - (float) Math.max(0, Math.min(1, (hy - ftop) / (svh - 1)));
        };
        hit(px, top, pw, svh, sv, sv);

        int hy0 = top + svh + p(26);
        for (int i = 0; i < pw; i++) c.fill(px + i, hy0, px + i + 1, hy0 + bar, 0xFF000000 | Setting.Color.hsv(i * 360f / pw, 1f, 1f));
        int hx0 = px + Math.round(col.h / 360f * (pw - 1));
        Gfx.rrect(c, hx0 - p(5), hy0 - p(4), p(10), bar + p(8), p(5), 0xFFFFFFFF);
        Act hue = (hx, hy) -> col.h = (float) Math.max(0, Math.min(1, (hx - px) / (pw - 1))) * 359.9f;
        hit(px, hy0 - p(6), pw, bar + p(12), hue, hue);

        int oy = hy0 + bar + p(22);
        checker(c, px, oy, pw, bar);
        int rgb = col.rgb();
        for (int i = 0; i < pw; i += step) {
            int al = Math.round(i / (float) (pw - 1) * 255f);
            c.fill(px + i, oy, px + Math.min(pw, i + step), oy + bar, (al << 24) | rgb);
        }
        int ox = px + Math.round(col.a * (pw - 1));
        Gfx.rrect(c, ox - p(5), oy - p(4), p(10), bar + p(8), p(5), 0xFFFFFFFF);
        Act op = (hx, hy) -> col.a = (float) Math.max(0, Math.min(1, (hx - px) / (pw - 1)));
        hit(px, oy - p(6), pw, bar + p(12), op, op);
        txt(c, "Opacity " + Math.round(col.a * 100) + " %", px + pw + p(24), oy + bar / 2f, 1.4f * s, SUB);

        checker(c, px + pw + p(24), top, p(130), p(100));
        Gfx.rrect(c, px + pw + p(24), top, p(130), p(100), p(12), col.argb());

        int ry = oy + bar + p(30);
        txt(c, "Chroma", px, ry + p(15), 1.5f * s, TXT);
        int tx = px + p(110);
        toggle(c, tx, ry + p(5), col.chroma);
        hit(tx - p(6), ry, p(52), p(30), (hx, hy) -> col.chroma = !col.chroma, null);
        txt(c, String.format("#%08X", col.argb()), px + p(230), ry + p(15), 1.4f * s, SUB);
        return y + totalH + p(10);
    }

    // ------------------------------------------------------------ input

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() == 0) {
            double mx = click.x(), my = click.y();
            for (int i = hits.size() - 1; i >= 0; i--) {
                Hit h = hits.get(i);
                if (!h.in(mx, my)) continue;
                if (h.content && !inContent(mx, my)) continue;
                h.click.run(mx, my);
                if (h.drag != null) dragging = h;
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (dragging != null) {
            dragging.drag.run(click.x(), click.y());
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (dragging != null) {
            dragging = null;
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scroll = Math.max(0, Math.min(maxScroll, scroll - verticalAmount * p(60)));
        return true;
    }

    @Override
    public void removed() {
        ModuleManager.save();
        ClientModMain.oneConfigPush();
        super.removed();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
