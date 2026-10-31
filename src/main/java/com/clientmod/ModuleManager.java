package com.clientmod;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ModuleManager {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("clientmod.json");
    private static final List<Module> LIST = new ArrayList<>();
    public static final Module MENU = new Module("menu", "Menu", "", "Menu", true);

    private ModuleManager() {}

    public static List<Module> all() { return LIST; }

    public static Module get(String id) {
        for (Module m : LIST) if (m.id.equals(id)) return m;
        return null;
    }

    public static boolean isEnabled(String id) {
        Module m = get(id);
        return m != null && m.enabled && m.implemented;
    }

    // ------------------------------------------------------------ definitions

    private static Module hud(String id, String name, String desc) {
        Module m = new Module(id, name, desc, "HUD", true);
        m.add("Display", new Setting.Bool("label", "Show label", true));
        m.add("Display", new Setting.Bool("brackets", "Brackets", false));
        m.add("Colors", new Setting.Color("textColor", "Text color", 255, 255, 255, 1f));
        m.add("Colors", new Setting.Color("bgColor", "Background color", 0, 0, 0, 0.4f));
        m.add("Colors", new Setting.Bool("shadow", "Text shadow", true));
        m.add("Colors", new Setting.Bool("outline", "Outline", false));
        layout(m, 0);
        return m;
    }

    private static void layout(Module m, int anchor) {
        m.add("Layout", new Setting.Num("scale", "Scale", 0.5f, 3f, 0.05f, 1f, "x"));
        m.add("Layout", new Setting.Num("padding", "Padding", 0f, 12f, 1f, 3f, " px"));
        m.add("Layout", new Setting.Choice("anchor", "Anchor", anchor, "Top left", "Top right", "Bottom left", "Bottom right"));
        m.add("Layout", new Setting.Num("offX", "Offset X", -1000f, 1000f, 1f, 0f, " px"));
        m.add("Layout", new Setting.Num("offY", "Offset Y", -1000f, 1000f, 1f, 0f, " px"));
    }

    private static Module soon(String id, String name, String desc, String cat) {
        return new Module(id, name, desc, cat, false);
    }

    private static TransformData cur() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return null;
        ItemStack s = mc.player.getMainHandStack();
        if (s.isEmpty()) return null;
        return TransformManager.getOrCreate(TransformManager.idOf(s));
    }

    private static Setting.Num itemNum(String id, String label, float min, float max, float step, float def, String unit,
                                       java.util.function.Function<TransformData, Float> g,
                                       java.util.function.BiConsumer<TransformData, Float> s) {
        return new Setting.Num(id, label, min, max, step, def, unit).bind(
                () -> { TransformData d = cur(); return d == null ? def : g.apply(d); },
                v -> { TransformData d = cur(); if (d != null) s.accept(d, v); });
    }

    static {
        Module fps = hud("fps", "FPS", "Frames per second");
        fps.enabled = true;
        LIST.add(fps);

        Module cps = hud("cps", "CPS", "Left and right clicks");
        cps.add("Display", new Setting.Bool("right", "Show right click", true));
        cps.add("Display", new Setting.Choice("sep", "Separator", 0, "|", "/", "-"));
        cps.enabled = true;
        LIST.add(cps);

        Module ping = hud("ping", "Ping", "Server latency");
        ping.add("Display", new Setting.Bool("unit", "Show unit", true));
        ping.enabled = true;
        LIST.add(ping);

        Module xyz = hud("xyz", "Coordinates", "Position and facing");
        xyz.add("Display", new Setting.Bool("dir", "Show direction", true));
        xyz.add("Display", new Setting.Num("dec", "Decimals", 0f, 3f, 1f, 0f, ""));
        xyz.add("Display", new Setting.Choice("layout", "Layout", 0, "One line", "Three lines"));
        xyz.enabled = true;
        LIST.add(xyz);

        Module arm = new Module("arm", "Armor status", "Your armor in a hotbar", "HUD", true);
        arm.add("Display", new Setting.Bool("nums", "Durability numbers", true));
        arm.add("Display", new Setting.Bool("held", "Show held item", false));
        arm.add("Display", new Setting.Bool("empty", "Show empty slot icons", true));
        arm.add("Display", new Setting.Choice("orient", "Orientation", 0, "Horizontal", "Vertical"));
        arm.add("Warning", new Setting.Bool("warn", "Low durability warning", true));
        arm.add("Warning", new Setting.Num("warnBelow", "Warn below", 5f, 50f, 5f, 20f, "%"));
        arm.add("Colors", new Setting.Color("numColor", "Number color", 255, 255, 255, 1f));
        arm.add("Layout", new Setting.Num("scale", "Scale", 0.5f, 3f, 0.05f, 1f, "x"));
        arm.add("Layout", new Setting.Choice("anchor", "Anchor", 2, "Top left", "Top right", "Bottom left", "Bottom right"));
        arm.add("Layout", new Setting.Num("offX", "Offset X", -1000f, 1000f, 1f, 6f, " px"));
        arm.add("Layout", new Setting.Num("offY", "Offset Y", -1000f, 1000f, 1f, -30f, " px"));
        arm.enabled = true;
        LIST.add(arm);

        Module reach = hud("reach", "Reach display", "Distance of your last hit");
        reach.add("Display", new Setting.Num("hold", "Hold time", 1f, 10f, 1f, 3f, " s"));
        reach.add("Display", new Setting.Num("dec", "Decimals", 0f, 3f, 1f, 2f, ""));
        LIST.add(reach);

        Module full = new Module("full", "Fullbright", "Everything fully lit", "Visual", true);
        full.add("Light", new Setting.Bool("info", "Uses a client-side night vision effect", true));
        LIST.add(full);

        Module itf = new Module("itf", "Item transform", "Size, position, rotation", "Visual", true);
        itf.add("Size", itemNum("scale", "Scale", 0.1f, 5f, 0.05f, 1f, "x", d -> d.scale, (d, v) -> d.scale = v));
        itf.add("Size", itemNum("ground", "Ground size", 0.1f, 5f, 0.05f, 1f, "x", d -> d.groundScale, (d, v) -> d.groundScale = v));
        itf.add("Position", itemNum("px", "X", -2f, 2f, 0.05f, 0f, "", d -> d.px, (d, v) -> d.px = v));
        itf.add("Position", itemNum("py", "Y", -2f, 2f, 0.05f, 0f, "", d -> d.py, (d, v) -> d.py = v));
        itf.add("Position", itemNum("pz", "Z", -2f, 2f, 0.05f, 0f, "", d -> d.pz, (d, v) -> d.pz = v));
        itf.add("Rotation", itemNum("rx", "X", -180f, 180f, 1f, 0f, "°", d -> d.rx, (d, v) -> d.rx = v));
        itf.add("Rotation", itemNum("ry", "Y", -180f, 180f, 1f, 0f, "°", d -> d.ry, (d, v) -> d.ry = v));
        itf.add("Rotation", itemNum("rz", "Z", -180f, 180f, 1f, 0f, "°", d -> d.rz, (d, v) -> d.rz = v));
        itf.enabled = true;
        LIST.add(itf);

        Module spr = new Module("spr", "Toggle sprint", "Sprint without holding", "Utility", true);
        spr.add("Sprint", new Setting.Bool("on", "Keep sprinting while moving forward", true));
        spr.enabled = true;
        LIST.add(spr);

        Module shield = new Module("shield", "Shield scale", "Size of the shield", "Visual", true);
        shield.add("Size", new Setting.Num("scale", "Scale", 0.3f, 1.5f, 0.05f, 0.8f, "x"));
        shield.add("Position", new Setting.Num("offX", "Offset X", -1f, 1f, 0.05f, 0f, ""));
        shield.add("Position", new Setting.Num("offY", "Offset Y", -1f, 1f, 0.05f, 0f, ""));
        shield.add("Display", new Setting.Bool("third", "Also in third person", true));
        LIST.add(shield);

        Module zoom = new Module("zoom", "Zoom", "Hold a key to zoom", "Utility", true);
        zoom.add("Zoom", new Setting.Num("factor", "Zoom factor", 1.5f, 10f, 0.5f, 4f, "x"));
        zoom.add("Zoom", new Setting.Bool("smooth", "Smooth zoom", true));
        zoom.add("Zoom", new Setting.Num("speed", "Smooth speed", 50f, 500f, 10f, 150f, " ms"));
        zoom.add("Zoom", new Setting.Bool("hideHud", "Hide HUD while zooming", false));
        LIST.add(zoom);

        Module wp = new Module("wp", "Waypoints", "Markers with distance", "Utility", true);
        wp.add("Display", new Setting.Bool("name", "Show name", true));
        wp.add("Display", new Setting.Bool("dist", "Show distance", true));
        wp.add("Display", new Setting.Num("size", "Marker size", 0.5f, 2f, 0.1f, 1f, "x"));
        wp.add("Display", new Setting.Num("max", "Max distance", 50f, 5000f, 50f, 1000f, " blocks"));
        wp.add("Create", new Setting.Bool("death", "Add death point", true));
        wp.enabled = true;
        LIST.add(wp);

        // planned modules (shown greyed out in the menu)
        LIST.add(soon("sat", "Color saturation", "Stronger or softer colors", "Visual"));
        LIST.add(soon("blur", "Motion blur", "Blur while turning", "Visual"));
        LIST.add(soon("fog", "No fog", "Removes distance fog", "Visual"));
        LIST.add(soon("hit", "Hit color", "Custom damage tint", "Visual"));
        LIST.add(soon("out", "Block outlines", "Color of block outline", "Visual"));
        LIST.add(soon("box", "Hitbox", "Hitbox per entity type", "Visual"));
        LIST.add(soon("tw", "Time and weather", "Client-side time and rain", "Visual"));
        LIST.add(soon("rec", "Auto reconnect", "Rejoin after a kick", "Utility"));

        MENU.add("Window", new Setting.Bool("bg", "Background image", true));
        MENU.add("Window", new Setting.Num("opacity", "Window opacity (without background)", 30f, 95f, 1f, 62f, "%"));
        MENU.add("Window", new Setting.Color("accent", "Accent color", 225, 228, 235, 1f));
    }

    // ------------------------------------------------------------ persistence

    public static void load() {
        if (!Files.exists(FILE)) return;
        try (Reader r = Files.newBufferedReader(FILE)) {
            JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
            List<Module> all = new ArrayList<>(LIST);
            all.add(MENU);
            for (Module m : all) {
                if (!root.has(m.id)) continue;
                JsonObject o = root.getAsJsonObject(m.id);
                if (o.has("enabled") && m != MENU) m.enabled = o.get("enabled").getAsBoolean();
                if (o.has("s")) {
                    JsonObject s = o.getAsJsonObject("s");
                    for (Setting st : m.all()) if (s.has(st.id)) st.load(s.get(st.id));
                }
            }
        } catch (Exception e) {
            System.err.println("[ClientMod] Could not load config: " + e);
        }
    }

    public static void save() {
        JsonObject root = new JsonObject();
        List<Module> all = new ArrayList<>(LIST);
        all.add(MENU);
        for (Module m : all) {
            JsonObject o = new JsonObject();
            o.addProperty("enabled", m.enabled);
            JsonObject s = new JsonObject();
            for (Setting st : m.all()) {
                JsonElement e = st.save();
                if (e != null && !e.isJsonNull()) s.add(st.id, e);
            }
            o.add("s", s);
            root.add(m.id, o);
        }
        try (Writer w = Files.newBufferedWriter(FILE)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(root, w);
        } catch (Exception e) {
            System.err.println("[ClientMod] Could not save config: " + e);
        }
        TransformManager.save();
    }
}
