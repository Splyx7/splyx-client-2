package com.clientmod.oneconfig;

import com.clientmod.Module;
import com.clientmod.ModuleManager;
import com.clientmod.Setting;
import org.polyfrost.oneconfig.api.config.v1.Config;
import org.polyfrost.oneconfig.api.config.v1.annotations.Dropdown;
import org.polyfrost.oneconfig.api.config.v1.annotations.Slider;
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;

/**
 * Optional OneConfig page for Client Mod (generated). Colors are edited in the Client Mod menu.
 * The mod's own config is the source of truth at game start; changes in OneConfig are pulled back.
 */
public class OneConfigBridge extends Config {
    @Switch(title = "Enabled", category = "FPS")
    public static boolean fps_enabled = true;

    @Switch(title = "Show label", category = "FPS")
    public static boolean fps_label = true;

    @Switch(title = "Brackets", category = "FPS")
    public static boolean fps_brackets = false;

    @Switch(title = "Text shadow", category = "FPS")
    public static boolean fps_shadow = true;

    @Switch(title = "Outline", category = "FPS")
    public static boolean fps_outline = false;

    @Slider(title = "Scale", category = "FPS", unit = "x", min = 0.5f, max = 3.0f, step = 0.05f)
    public static float fps_scale = 1.0f;

    @Slider(title = "Padding", category = "FPS", unit = "px", min = 0.0f, max = 12.0f, step = 1.0f)
    public static float fps_padding = 3.0f;

    @Dropdown(title = "Anchor", category = "FPS", options = { "Top left", "Top right", "Bottom left", "Bottom right" })
    public static int fps_anchor = 0;

    @Slider(title = "Offset X", category = "FPS", unit = "px", min = -1000.0f, max = 1000.0f, step = 1.0f)
    public static float fps_offX = 0.0f;

    @Slider(title = "Offset Y", category = "FPS", unit = "px", min = -1000.0f, max = 1000.0f, step = 1.0f)
    public static float fps_offY = 0.0f;

    @Switch(title = "Enabled", category = "CPS")
    public static boolean cps_enabled = true;

    @Switch(title = "Show label", category = "CPS")
    public static boolean cps_label = true;

    @Switch(title = "Brackets", category = "CPS")
    public static boolean cps_brackets = false;

    @Switch(title = "Text shadow", category = "CPS")
    public static boolean cps_shadow = true;

    @Switch(title = "Outline", category = "CPS")
    public static boolean cps_outline = false;

    @Slider(title = "Scale", category = "CPS", unit = "x", min = 0.5f, max = 3.0f, step = 0.05f)
    public static float cps_scale = 1.0f;

    @Slider(title = "Padding", category = "CPS", unit = "px", min = 0.0f, max = 12.0f, step = 1.0f)
    public static float cps_padding = 3.0f;

    @Dropdown(title = "Anchor", category = "CPS", options = { "Top left", "Top right", "Bottom left", "Bottom right" })
    public static int cps_anchor = 0;

    @Slider(title = "Offset X", category = "CPS", unit = "px", min = -1000.0f, max = 1000.0f, step = 1.0f)
    public static float cps_offX = 0.0f;

    @Slider(title = "Offset Y", category = "CPS", unit = "px", min = -1000.0f, max = 1000.0f, step = 1.0f)
    public static float cps_offY = 0.0f;

    @Switch(title = "Show right click", category = "CPS")
    public static boolean cps_right = true;

    @Dropdown(title = "Separator", category = "CPS", options = { "|", "/", "-" })
    public static int cps_sep = 0;

    @Switch(title = "Enabled", category = "Ping")
    public static boolean ping_enabled = true;

    @Switch(title = "Show label", category = "Ping")
    public static boolean ping_label = true;

    @Switch(title = "Brackets", category = "Ping")
    public static boolean ping_brackets = false;

    @Switch(title = "Text shadow", category = "Ping")
    public static boolean ping_shadow = true;

    @Switch(title = "Outline", category = "Ping")
    public static boolean ping_outline = false;

    @Slider(title = "Scale", category = "Ping", unit = "x", min = 0.5f, max = 3.0f, step = 0.05f)
    public static float ping_scale = 1.0f;

    @Slider(title = "Padding", category = "Ping", unit = "px", min = 0.0f, max = 12.0f, step = 1.0f)
    public static float ping_padding = 3.0f;

    @Dropdown(title = "Anchor", category = "Ping", options = { "Top left", "Top right", "Bottom left", "Bottom right" })
    public static int ping_anchor = 0;

    @Slider(title = "Offset X", category = "Ping", unit = "px", min = -1000.0f, max = 1000.0f, step = 1.0f)
    public static float ping_offX = 0.0f;

    @Slider(title = "Offset Y", category = "Ping", unit = "px", min = -1000.0f, max = 1000.0f, step = 1.0f)
    public static float ping_offY = 0.0f;

    @Switch(title = "Show unit", category = "Ping")
    public static boolean ping_unit = true;

    @Switch(title = "Enabled", category = "Coordinates")
    public static boolean xyz_enabled = true;

    @Switch(title = "Show label", category = "Coordinates")
    public static boolean xyz_label = true;

    @Switch(title = "Brackets", category = "Coordinates")
    public static boolean xyz_brackets = false;

    @Switch(title = "Text shadow", category = "Coordinates")
    public static boolean xyz_shadow = true;

    @Switch(title = "Outline", category = "Coordinates")
    public static boolean xyz_outline = false;

    @Slider(title = "Scale", category = "Coordinates", unit = "x", min = 0.5f, max = 3.0f, step = 0.05f)
    public static float xyz_scale = 1.0f;

    @Slider(title = "Padding", category = "Coordinates", unit = "px", min = 0.0f, max = 12.0f, step = 1.0f)
    public static float xyz_padding = 3.0f;

    @Dropdown(title = "Anchor", category = "Coordinates", options = { "Top left", "Top right", "Bottom left", "Bottom right" })
    public static int xyz_anchor = 0;

    @Slider(title = "Offset X", category = "Coordinates", unit = "px", min = -1000.0f, max = 1000.0f, step = 1.0f)
    public static float xyz_offX = 0.0f;

    @Slider(title = "Offset Y", category = "Coordinates", unit = "px", min = -1000.0f, max = 1000.0f, step = 1.0f)
    public static float xyz_offY = 0.0f;

    @Switch(title = "Show direction", category = "Coordinates")
    public static boolean xyz_dir = true;

    @Slider(title = "Decimals", category = "Coordinates", unit = "", min = 0.0f, max = 3.0f, step = 1.0f)
    public static float xyz_dec = 0.0f;

    @Dropdown(title = "Layout", category = "Coordinates", options = { "One line", "Three lines" })
    public static int xyz_layout = 0;

    @Switch(title = "Enabled", category = "Armor status")
    public static boolean arm_enabled = true;

    @Switch(title = "Durability numbers", category = "Armor status")
    public static boolean arm_nums = true;

    @Switch(title = "Show held item", category = "Armor status")
    public static boolean arm_held = false;

    @Switch(title = "Show empty slot icons", category = "Armor status")
    public static boolean arm_empty = true;

    @Switch(title = "Low durability warning", category = "Armor status")
    public static boolean arm_warn = true;

    @Slider(title = "Warn below", category = "Armor status", unit = "%", min = 5.0f, max = 50.0f, step = 5.0f)
    public static float arm_warnBelow = 20.0f;

    @Dropdown(title = "Orientation", category = "Armor status", options = { "Horizontal", "Vertical" })
    public static int arm_orient = 0;

    @Slider(title = "Scale", category = "Armor status", unit = "x", min = 0.5f, max = 3.0f, step = 0.05f)
    public static float arm_scale = 1.0f;

    @Dropdown(title = "Anchor", category = "Armor status", options = { "Top left", "Top right", "Bottom left", "Bottom right" })
    public static int arm_anchor = 2;

    @Slider(title = "Offset X", category = "Armor status", unit = "px", min = -1000.0f, max = 1000.0f, step = 1.0f)
    public static float arm_offX = 6.0f;

    @Slider(title = "Offset Y", category = "Armor status", unit = "px", min = -1000.0f, max = 1000.0f, step = 1.0f)
    public static float arm_offY = -30.0f;

    @Switch(title = "Enabled", category = "Reach display")
    public static boolean reach_enabled = false;

    @Switch(title = "Show label", category = "Reach display")
    public static boolean reach_label = true;

    @Switch(title = "Brackets", category = "Reach display")
    public static boolean reach_brackets = false;

    @Switch(title = "Text shadow", category = "Reach display")
    public static boolean reach_shadow = true;

    @Switch(title = "Outline", category = "Reach display")
    public static boolean reach_outline = false;

    @Slider(title = "Scale", category = "Reach display", unit = "x", min = 0.5f, max = 3.0f, step = 0.05f)
    public static float reach_scale = 1.0f;

    @Slider(title = "Padding", category = "Reach display", unit = "px", min = 0.0f, max = 12.0f, step = 1.0f)
    public static float reach_padding = 3.0f;

    @Dropdown(title = "Anchor", category = "Reach display", options = { "Top left", "Top right", "Bottom left", "Bottom right" })
    public static int reach_anchor = 0;

    @Slider(title = "Offset X", category = "Reach display", unit = "px", min = -1000.0f, max = 1000.0f, step = 1.0f)
    public static float reach_offX = 0.0f;

    @Slider(title = "Offset Y", category = "Reach display", unit = "px", min = -1000.0f, max = 1000.0f, step = 1.0f)
    public static float reach_offY = 0.0f;

    @Slider(title = "Hold time", category = "Reach display", unit = "s", min = 1.0f, max = 10.0f, step = 1.0f)
    public static float reach_hold = 3.0f;

    @Slider(title = "Decimals", category = "Reach display", unit = "", min = 0.0f, max = 3.0f, step = 1.0f)
    public static float reach_dec = 2.0f;

    @Switch(title = "Enabled", category = "Fullbright")
    public static boolean full_enabled = false;

    @Switch(title = "Enabled", category = "Item transform")
    public static boolean itf_enabled = true;

    @Switch(title = "Enabled", category = "Toggle sprint")
    public static boolean spr_enabled = true;

    @Switch(title = "Keep sprinting while moving forward", category = "Toggle sprint")
    public static boolean spr_on = true;

    private static final String[] FIELDS = {
        "fps_enabled",
        "fps_label",
        "fps_brackets",
        "fps_shadow",
        "fps_outline",
        "fps_scale",
        "fps_padding",
        "fps_anchor",
        "fps_offX",
        "fps_offY",
        "cps_enabled",
        "cps_label",
        "cps_brackets",
        "cps_shadow",
        "cps_outline",
        "cps_scale",
        "cps_padding",
        "cps_anchor",
        "cps_offX",
        "cps_offY",
        "cps_right",
        "cps_sep",
        "ping_enabled",
        "ping_label",
        "ping_brackets",
        "ping_shadow",
        "ping_outline",
        "ping_scale",
        "ping_padding",
        "ping_anchor",
        "ping_offX",
        "ping_offY",
        "ping_unit",
        "xyz_enabled",
        "xyz_label",
        "xyz_brackets",
        "xyz_shadow",
        "xyz_outline",
        "xyz_scale",
        "xyz_padding",
        "xyz_anchor",
        "xyz_offX",
        "xyz_offY",
        "xyz_dir",
        "xyz_dec",
        "xyz_layout",
        "arm_enabled",
        "arm_nums",
        "arm_held",
        "arm_orient",
        "arm_empty",
        "arm_warn",
        "arm_warnBelow",
        "arm_scale",
        "arm_anchor",
        "arm_offX",
        "arm_offY",
        "reach_enabled",
        "reach_label",
        "reach_brackets",
        "reach_shadow",
        "reach_outline",
        "reach_scale",
        "reach_padding",
        "reach_anchor",
        "reach_offX",
        "reach_offY",
        "reach_hold",
        "reach_dec",
        "full_enabled",
        "itf_enabled",
        "spr_enabled",
        "spr_on"
    };

    public static final OneConfigBridge INSTANCE = new OneConfigBridge();

    public OneConfigBridge() {
        super("clientmod.json", "/assets/clientmod/icon.png", "Client Mod", Config.Category.QOL);
        for (String f : FIELDS) {
            addCallback(f, () -> pull(f));
        }
    }

    public static void init() {
        INSTANCE.preload();
        push();
    }

    /** Client Mod -> OneConfig */
    public static void push() {
        try {
            for (String f : FIELDS) {
                int i = f.indexOf('_');
                Module m = ModuleManager.get(f.substring(0, i));
                if (m == null) continue;
                String sid = f.substring(i + 1);
                java.lang.reflect.Field field = OneConfigBridge.class.getField(f);
                if (sid.equals("enabled")) {
                    field.setBoolean(null, m.enabled);
                    continue;
                }
                Setting s = m.find(sid);
                if (s instanceof Setting.Bool b) field.setBoolean(null, b.value);
                else if (s instanceof Setting.Num n) field.setFloat(null, n.get());
                else if (s instanceof Setting.Choice c) field.setInt(null, c.value);
            }
            INSTANCE.save();
        } catch (Throwable t) {
            System.err.println("[ClientMod] OneConfig push failed: " + t);
        }
    }

    /** OneConfig -> Client Mod */
    private static void pull(String f) {
        try {
            int i = f.indexOf('_');
            Module m = ModuleManager.get(f.substring(0, i));
            if (m == null) return;
            String sid = f.substring(i + 1);
            java.lang.reflect.Field field = OneConfigBridge.class.getField(f);
            if (sid.equals("enabled")) {
                m.enabled = field.getBoolean(null);
                return;
            }
            Setting s = m.find(sid);
            if (s instanceof Setting.Bool b) b.value = field.getBoolean(null);
            else if (s instanceof Setting.Num n) n.set(field.getFloat(null));
            else if (s instanceof Setting.Choice c) c.value = field.getInt(null);
        } catch (Throwable t) {
            System.err.println("[ClientMod] OneConfig pull failed: " + t);
        }
    }
}
