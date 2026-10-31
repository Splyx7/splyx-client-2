package com.clientmod;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;

/** Drag HUD elements with the mouse. */
public class HudEditorScreen extends Screen {
    private Module drag;
    private float startX, startY, accX, accY;

    public HudEditorScreen() {
        super(Text.literal("HUD editor"));
    }

    private static boolean in(float[] b, double mx, double my) {
        return mx >= b[0] - 2 && mx < b[0] + Math.max(8, b[2]) + 2 && my >= b[1] - 2 && my < b[1] + Math.max(8, b[3]) + 2;
    }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        super.render(c, mx, my, delta);
        int accent = ModuleManager.MENU.c("accent") | 0xFF000000;
        for (Module m : ModuleManager.all()) {
            float[] b = HudRenderer.BOUNDS.get(m.id);
            if (b == null || !m.enabled) continue;
            int x = Math.round(b[0]), y = Math.round(b[1]);
            int w = Math.max(8, Math.round(b[2])), h = Math.max(8, Math.round(b[3]));
            boolean hot = m == drag || (drag == null && in(b, mx, my));
            int col = hot ? accent : 0xAAFFFFFF;
            if (hot) c.fill(x - 2, y - 2, x + w + 2, y + h + 2, (accent & 0x00FFFFFF) | 0x33000000);
            c.fill(x - 2, y - 2, x + w + 2, y - 1, col);
            c.fill(x - 2, y + h + 1, x + w + 2, y + h + 2, col);
            c.fill(x - 2, y - 2, x - 1, y + h + 2, col);
            c.fill(x + w + 1, y - 2, x + w + 2, y + h + 2, col);
            c.drawTextWithShadow(textRenderer, Text.literal(m.name), x, y - 12 < 0 ? y + h + 4 : y - 12, hot ? accent : 0xFFDDDDDD);
        }
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("HUD editor: drag elements with the mouse. ESC to save and close."),
                width / 2, 8, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() == 0) {
            List<Module> all = ModuleManager.all();
            for (int i = all.size() - 1; i >= 0; i--) {
                Module m = all.get(i);
                float[] b = HudRenderer.BOUNDS.get(m.id);
                if (b == null || !m.enabled || !in(b, click.x(), click.y())) continue;
                drag = m;
                startX = m.num("offX").get();
                startY = m.num("offY").get();
                accX = 0;
                accY = 0;
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (drag != null) {
            accX += (float) offsetX;
            accY += (float) offsetY;
            drag.num("offX").set(startX + accX);
            drag.num("offY").set(startY + accY);
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (drag != null) {
            drag = null;
            ModuleManager.save();
            return true;
        }
        return super.mouseReleased(click);
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
