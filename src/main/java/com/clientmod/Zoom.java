package com.clientmod;

import net.minecraft.client.MinecraftClient;

public final class Zoom {
    private static float cur = 1f;
    private static long last = System.nanoTime();

    private Zoom() {}

    public static boolean active() {
        Module z = ModuleManager.get("zoom");
        MinecraftClient mc = MinecraftClient.getInstance();
        return z != null && z.enabled && ClientModMain.zoomKey != null && ClientModMain.zoomKey.isPressed() && mc.currentScreen == null;
    }

    /** Current zoom factor (1 = no zoom). Called once per frame from the FOV hook. */
    public static float factor() {
        Module z = ModuleManager.get("zoom");
        long now = System.nanoTime();
        float dt = (now - last) / 1_000_000f;
        last = now;
        float target = active() ? z.n("factor") : 1f;
        if (z.b("smooth")) {
            float speed = Math.max(20f, z.n("speed"));
            float k = 1f - (float) Math.exp(-dt / (speed * 0.35f));
            cur += (target - cur) * Math.min(1f, k);
        } else {
            cur = target;
        }
        return cur;
    }
}
