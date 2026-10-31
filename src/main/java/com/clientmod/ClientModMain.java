package com.clientmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;

public class ClientModMain implements ClientModInitializer {
    public static KeyBinding openKey, zoomKey, waypointKey;

    public static double lastReach = -1;
    public static long lastReachTime = 0;

    private static final Deque<Long> LEFT = new ArrayDeque<>();
    private static final Deque<Long> RIGHT = new ArrayDeque<>();
    private static boolean wasLeft, wasRight, fbApplied;

    public static int cps(boolean left) {
        Deque<Long> d = left ? LEFT : RIGHT;
        long now = System.currentTimeMillis();
        while (!d.isEmpty() && now - d.peekFirst() > 1000) d.pollFirst();
        return d.size();
    }

    private static void oneConfigCall(String method) {
        try {
            Class.forName("com.clientmod.oneconfig.OneConfigBridge").getMethod(method).invoke(null);
        } catch (Throwable ignored) {
            // OneConfig is optional
        }
    }

    public static void oneConfigPush() { oneConfigCall("push"); }

    @Override
    public void onInitializeClient() {
        ModuleManager.load();
        TransformManager.load();
        Waypoints.load();
        oneConfigCall("init");
        ClientLifecycleEvents.CLIENT_STOPPING.register(c -> ModuleManager.save());

        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("clientmod", "main"));
        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.clientmod.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_CONTROL, category));
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.clientmod.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, category));
        waypointKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.clientmod.waypoint", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, category));

        HudRenderCallback.EVENT.register((ctx, tickCounter) -> HudRenderer.render(ctx));

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (world.isClient() && player == mc.player) {
                Vec3d eye = player.getEyePos();
                Box b = entity.getBoundingBox();
                double dx = Math.max(Math.max(b.minX - eye.x, 0), eye.x - b.maxX);
                double dy = Math.max(Math.max(b.minY - eye.y, 0), eye.y - b.maxY);
                double dz = Math.max(Math.max(b.minZ - eye.z, 0), eye.z - b.maxZ);
                lastReach = Math.sqrt(dx * dx + dy * dy + dz * dz);
                lastReachTime = System.currentTimeMillis();
            }
            return ActionResult.PASS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (openKey.wasPressed()) {
                if (mc.currentScreen == null && mc.player != null) mc.setScreen(new MenuScreen());
            }
            while (waypointKey.wasPressed()) {
                if (mc.currentScreen == null && mc.player != null && ModuleManager.isEnabled("wp")) {
                    Waypoints.addHere("Waypoint " + (Waypoints.list.size() + 1));
                }
            }
            if (mc.player == null) return;
            Waypoints.tick(mc);

            // CPS tracking (key state edges)
            boolean l = mc.options.attackKey.isPressed();
            boolean r = mc.options.useKey.isPressed();
            if (l && !wasLeft) LEFT.addLast(System.currentTimeMillis());
            if (r && !wasRight) RIGHT.addLast(System.currentTimeMillis());
            wasLeft = l;
            wasRight = r;

            // Fullbright (client-side night vision)
            if (ModuleManager.isEnabled("full")) {
                if (!mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                    mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 100000, 0, false, false, false));
                    fbApplied = true;
                }
            } else if (fbApplied) {
                mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
                fbApplied = false;
            }

            // Toggle sprint
            Module spr = ModuleManager.get("spr");
            if (spr.enabled && spr.b("on") && mc.currentScreen == null && mc.options.forwardKey.isPressed()
                    && !mc.player.isSneaking() && !mc.player.isUsingItem()) {
                mc.player.setSprinting(true);
            }
        });
    }
}
