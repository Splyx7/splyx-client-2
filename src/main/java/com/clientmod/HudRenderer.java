package com.clientmod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.util.Identifier;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class HudRenderer {
    /** Last drawn bounds per module: {x, y, w, h} in scaled GUI pixels (used by the HUD editor). */
    public static final java.util.Map<String, float[]> BOUNDS = new java.util.HashMap<>();

    private HudRenderer() {}

    public static void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        BOUNDS.clear();
        if (mc.player == null || mc.options.hudHidden) return;

        Waypoints.render(ctx, mc);
        Module zm = ModuleManager.get("zoom");
        if (zm != null && zm.enabled && zm.b("hideHud") && Zoom.active()) return;

        float[] used = new float[4];
        for (Module m : ModuleManager.all()) {
            if (!m.enabled || !m.implemented || !m.category.equals("HUD")) continue;
            if (m.id.equals("arm")) {
                armor(ctx, mc, m);
                continue;
            }
            List<String> lines = lines(m, mc);
            if (!lines.isEmpty()) drawLines(ctx, mc, m, lines, used);
        }
    }

    private static String line(Module m, String label, String value) {
        String s = m.b("label") ? label + ": " + value : value;
        return m.b("brackets") ? "[" + s + "]" : s;
    }

    private static List<String> lines(Module m, MinecraftClient mc) {
        List<String> out = new ArrayList<>();
        switch (m.id) {
            case "fps" -> out.add(line(m, "FPS", String.valueOf(mc.getCurrentFps())));
            case "cps" -> {
                String sep = new String[]{" | ", " / ", " - "}[m.ch("sep")];
                String v = String.valueOf(ClientModMain.cps(true));
                if (m.b("right")) v += sep + ClientModMain.cps(false);
                out.add(line(m, "CPS", v));
            }
            case "ping" -> {
                int ping = 0;
                if (mc.getNetworkHandler() != null) {
                    PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
                    if (e != null) ping = e.getLatency();
                }
                out.add(line(m, "Ping", ping + (m.b("unit") ? " ms" : "")));
            }
            case "xyz" -> {
                int dec = Math.round(m.n("dec"));
                String f = "%." + dec + "f";
                String x = String.format(Locale.ROOT, f, mc.player.getX());
                String y = String.format(Locale.ROOT, f, mc.player.getY());
                String z = String.format(Locale.ROOT, f, mc.player.getZ());
                String dir = "";
                if (m.b("dir")) {
                    String n = mc.player.getHorizontalFacing().name();
                    dir = n.charAt(0) + n.substring(1).toLowerCase(Locale.ROOT);
                }
                if (m.ch("layout") == 0) {
                    out.add(line(m, "XYZ", x + ", " + y + ", " + z + (dir.isEmpty() ? "" : " (" + dir + ")")));
                } else {
                    out.add(m.b("label") ? "X: " + x : x);
                    out.add(m.b("label") ? "Y: " + y : y);
                    out.add(m.b("label") ? "Z: " + z : z);
                    if (!dir.isEmpty()) out.add(dir);
                }
            }
            case "reach" -> {
                long age = System.currentTimeMillis() - ClientModMain.lastReachTime;
                String v = "-";
                if (ClientModMain.lastReach >= 0 && age < m.n("hold") * 1000f) {
                    v = String.format(Locale.ROOT, "%." + Math.round(m.n("dec")) + "f", ClientModMain.lastReach);
                }
                out.add(line(m, "Reach", v));
            }
            default -> { }
        }
        return out;
    }

    private static void drawLines(DrawContext ctx, MinecraftClient mc, Module m, List<String> lines, float[] used) {
        TextRenderer tr = mc.textRenderer;
        float sc = m.n("scale");
        int pad = Math.round(m.n("padding"));
        int anchor = m.ch("anchor");
        int w = 0;
        for (String l : lines) w = Math.max(w, tr.getWidth(l));
        int bw = w + pad * 2;
        int bh = lines.size() * 10 - 1 + pad * 2;
        int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();

        float px = (anchor == 1 || anchor == 3) ? sw - bw * sc - 4 : 4;
        float py = (anchor >= 2) ? sh - bh * sc - 4 - used[anchor] : 4 + used[anchor];
        px += m.n("offX");
        py += m.n("offY");
        used[anchor] += bh * sc + 2;
        BOUNDS.put(m.id, new float[]{px, py, bw * sc, bh * sc});

        int tc = m.c("textColor");
        var mat = ctx.getMatrices();
        mat.pushMatrix();
        mat.translate(px, py);
        mat.scale(sc, sc);
        int bg = m.c("bgColor");
        if (((bg >>> 24) & 255) > 0) ctx.fill(0, 0, bw, bh, bg);
        if (m.b("outline")) {
            ctx.fill(0, 0, bw, 1, tc);
            ctx.fill(0, bh - 1, bw, bh, tc);
            ctx.fill(0, 0, 1, bh, tc);
            ctx.fill(bw - 1, 0, bw, bh, tc);
        }
        boolean shadow = m.b("shadow");
        for (int i = 0; i < lines.size(); i++) {
            ctx.drawText(tr, Text.literal(lines.get(i)), pad, pad + i * 10, tc, shadow);
        }
        mat.popMatrix();
    }

    // ---------------------------------------------------------------- armor

    private static final Identifier HOTBAR = Identifier.ofVanilla("hud/hotbar");
    private static final Identifier[] EMPTY = {
            Identifier.ofVanilla("container/slot/helmet"), Identifier.ofVanilla("container/slot/chestplate"),
            Identifier.ofVanilla("container/slot/leggings"), Identifier.ofVanilla("container/slot/boots")};

    /** Armor widget built from the vanilla hotbar texture, so it looks exactly like the hotbar. */
    private static void armor(DrawContext ctx, MinecraftClient mc, Module m) {
        TextRenderer tr = mc.textRenderer;
        PlayerEntity p = mc.player;
        List<ItemStack> st = new ArrayList<>();
        st.add(p.getEquippedStack(EquipmentSlot.HEAD));
        st.add(p.getEquippedStack(EquipmentSlot.CHEST));
        st.add(p.getEquippedStack(EquipmentSlot.LEGS));
        st.add(p.getEquippedStack(EquipmentSlot.FEET));
        if (m.b("held")) st.add(p.getMainHandStack());

        boolean vert = m.ch("orient") == 1;
        boolean nums = m.b("nums"), empty = m.b("empty");
        int n = st.size();

        int numW = 0;
        if (nums && vert) {
            for (ItemStack s : st) if (s.isDamageable()) numW = Math.max(numW, tr.getWidth(String.valueOf(s.getMaxDamage() - s.getDamage())));
            numW += 4;
        }
        int numH = (nums && !vert) ? 11 : 0;
        int fw = vert ? 22 : 2 + 20 * n;
        int fh = vert ? 1 + 21 * n : 22;
        int totalW = fw + numW, totalH = fh + numH;

        float sc = m.n("scale");
        int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();
        int anchor = m.ch("anchor");
        float px = (anchor == 1 || anchor == 3) ? sw - totalW * sc : 0;
        float py = (anchor >= 2) ? sh - totalH * sc : 0;
        px += m.n("offX");
        py += m.n("offY");
        BOUNDS.put(m.id, new float[]{px, py, totalW * sc, totalH * sc});

        var mat = ctx.getMatrices();
        mat.pushMatrix();
        mat.translate(px, py);
        mat.scale(sc, sc);

        // frame: the real hotbar sprite
        if (vert) {
            for (int i = 0; i < n; i++) {
                int y = numH + i * 21;
                ctx.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HOTBAR, 182, 22, 0, 0, 0, y, 21, 22);
                ctx.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HOTBAR, 182, 22, 181, 0, 21, y, 1, 22);
            }
        } else {
            ctx.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HOTBAR, 182, 22, 0, 0, 0, numH, 1 + 20 * n, 22);
            ctx.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HOTBAR, 182, 22, 181, 0, 1 + 20 * n, numH, 1, 22);
        }

        int numColor = m.c("numColor");
        boolean low = false;
        float warnBelow = m.n("warnBelow") / 100f;
        for (int i = 0; i < n; i++) {
            ItemStack s = st.get(i);
            int ix = vert ? 3 : 3 + i * 20;
            int iy = vert ? numH + 3 + i * 21 : numH + 3;
            if (s.isEmpty()) {
                if (empty && i < 4) {
                    ctx.drawGuiTexture(RenderPipelines.GUI_TEXTURED, EMPTY[i], ix, iy, 16, 16, 0.35f);
                }
                continue;
            }
            ctx.drawItem(s, ix, iy);
            ctx.drawStackOverlay(tr, s, ix, iy);
            if (s.isDamageable()) {
                int left = s.getMaxDamage() - s.getDamage();
                if (i < 4 && left / (float) s.getMaxDamage() < warnBelow) low = true;
                if (nums) {
                    String t = String.valueOf(left);
                    if (vert) ctx.drawText(tr, Text.literal(t), 22 + 3, iy + 4, numColor, true);
                    else ctx.drawText(tr, Text.literal(t), ix + 8 - tr.getWidth(t) / 2, 1, numColor, true);
                }
            }
        }

        if (low && m.b("warn")) {
            int wx = fw + numW + 3, wy = numH + 7;
            String[] icon = {".rrrrrrr.", "rwrrwrrwr", "rwrrwrrwr", "rwrrwrrwr", "rrrrrrrrr", "rwrrwrrwr", ".rrrrrrr."};
            for (int r = 0; r < icon.length; r++) {
                for (int k = 0; k < 9; k++) {
                    char ch = icon[r].charAt(k);
                    if (ch == '.') continue;
                    ctx.fill(wx + k, wy + r, wx + k + 1, wy + r + 1, ch == 'w' ? 0xFFFFFFFF : 0xFFD42A2A);
                }
            }
        }
        mat.popMatrix();
    }
}
