package com.clientmod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class Waypoints {
    public static final class Wp {
        public String name;
        public double x, y, z;
        public String dim;
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("clientmod_waypoints.json");
    public static List<Wp> list = new ArrayList<>();
    private static boolean wasDead;

    private Waypoints() {}

    public static void load() {
        if (!Files.exists(FILE)) return;
        try (Reader r = Files.newBufferedReader(FILE)) {
            Type t = new TypeToken<List<Wp>>() {}.getType();
            List<Wp> l = GSON.fromJson(r, t);
            if (l != null) list = l;
        } catch (Exception e) {
            System.err.println("[ClientMod] Could not load waypoints: " + e);
        }
    }

    public static void save() {
        try (Writer w = Files.newBufferedWriter(FILE)) {
            GSON.toJson(list, w);
        } catch (Exception e) {
            System.err.println("[ClientMod] Could not save waypoints: " + e);
        }
    }

    private static String dimOf(MinecraftClient mc) {
        return mc.world == null ? "" : mc.world.getRegistryKey().getValue().toString();
    }

    public static void addHere(String name) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        Wp w = new Wp();
        w.name = name;
        w.x = mc.player.getX();
        w.y = mc.player.getY();
        w.z = mc.player.getZ();
        w.dim = dimOf(mc);
        list.add(w);
        save();
    }

    public static void remove(Wp w) {
        list.remove(w);
        save();
    }

    public static void tick(MinecraftClient mc) {
        Module m = ModuleManager.get("wp");
        if (mc.player == null || m == null) return;
        boolean dead = mc.player.isDead();
        if (dead && !wasDead && m.enabled && m.b("death")) addHere("Death");
        wasDead = dead;
    }

    /** Draws the waypoint markers by projecting them onto the screen. */
    public static void render(DrawContext ctx, MinecraftClient mc) {
        Module m = ModuleManager.get("wp");
        if (m == null || !m.enabled || mc.player == null || list.isEmpty()) return;
        float delta = mc.getRenderTickCounter().getTickProgress(false);
        Vec3d eye = mc.player.getLerpedPos(delta).add(0, mc.player.getEyeHeight(mc.player.getPose()), 0);
        double yaw = Math.toRadians(mc.player.getYaw(delta)), pitch = Math.toRadians(mc.player.getPitch(delta));
        double fx = -Math.sin(yaw) * Math.cos(pitch), fy = -Math.sin(pitch), fz = Math.cos(yaw) * Math.cos(pitch);
        double rx = -Math.cos(yaw), ry = 0, rz = -Math.sin(yaw);
        double ux = ry * fz - rz * fy, uy = rz * fx - rx * fz, uz = rx * fy - ry * fx;

        int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();
        double tan = Math.tan(Math.toRadians(mc.options.getFov().getValue()) / 2.0);
        double aspect = sw / (double) sh;
        TextRenderer tr = mc.textRenderer;
        String dim = dimOf(mc);
        float size = m.n("size");

        for (Wp w : list) {
            if (w.dim != null && !w.dim.isEmpty() && !w.dim.equals(dim)) continue;
            double dx = w.x - eye.x, dy = w.y + 1.0 - eye.y, dz = w.z - eye.z;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist > m.n("max")) continue;
            double zc = dx * fx + dy * fy + dz * fz;
            if (zc <= 0.1) continue;
            double xc = dx * rx + dy * ry + dz * rz, yc = dx * ux + dy * uy + dz * uz;
            float px = (float) ((xc / (zc * tan * aspect)) * 0.5 + 0.5) * sw;
            float py = (float) (0.5 - (yc / (zc * tan)) * 0.5) * sh;
            if (px < -20 || px > sw + 20 || py < -20 || py > sh + 20) continue;

            var mat = ctx.getMatrices();
            mat.pushMatrix();
            mat.translate(px, py);
            mat.scale(size, size);
            Icons.draw(ctx, "wp", -6, -6, 12);
            int ty = 8;
            if (m.b("name") && w.name != null) {
                ctx.drawText(tr, Text.literal(w.name), -tr.getWidth(w.name) / 2, ty, 0xFFFFFFFF, true);
                ty += 10;
            }
            if (m.b("dist")) {
                String d = Math.round(dist) + "m";
                ctx.drawText(tr, Text.literal(d), -tr.getWidth(d) / 2, ty, 0xFFBBBBBB, true);
            }
            mat.popMatrix();
        }
    }
}
