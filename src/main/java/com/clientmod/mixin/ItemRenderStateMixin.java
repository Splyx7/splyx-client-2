package com.clientmod.mixin;

import com.clientmod.ItemStateHolder;
import com.clientmod.Module;
import com.clientmod.ModuleManager;
import com.clientmod.TransformData;
import com.clientmod.TransformManager;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderState.class)
public class ItemRenderStateMixin implements ItemStateHolder {
    @Unique private String clientmod$id;
    @Unique private ItemDisplayContext clientmod$ctx;

    @Override
    public void itemtransformer$set(String id, ItemDisplayContext context) {
        this.clientmod$id = id;
        this.clientmod$ctx = context;
    }

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;III)V",
            at = @At("HEAD"))
    private void clientmod$push(MatrixStack matrices, OrderedRenderCommandQueue queue,
                                int light, int overlay, int outline, CallbackInfo ci) {
        matrices.push();
        if (clientmod$id == null || clientmod$ctx == null) return;

        if (clientmod$id.equals("minecraft:shield") && ModuleManager.isEnabled("shield")) {
            Module sm = ModuleManager.get("shield");
            boolean first = clientmod$ctx == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || clientmod$ctx == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
            boolean third = clientmod$ctx == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || clientmod$ctx == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
            if (first || (third && sm.b("third"))) {
                float f = sm.n("scale");
                boolean left = clientmod$ctx == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || clientmod$ctx == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
                matrices.translate(sm.n("offX") * (left ? -1f : 1f), sm.n("offY"), 0f);
                matrices.scale(f, f, f);
            }
        }

        if (!ModuleManager.isEnabled("itf")) return;
        TransformData d = TransformManager.get(clientmod$id);
        if (d == null) return;

        switch (clientmod$ctx) {
            case GROUND -> matrices.scale(d.groundScale, d.groundScale, d.groundScale);
            case FIRST_PERSON_LEFT_HAND, THIRD_PERSON_LEFT_HAND -> applyHand(matrices, d, true);
            case FIRST_PERSON_RIGHT_HAND, THIRD_PERSON_RIGHT_HAND -> applyHand(matrices, d, false);
            default -> { }
        }
    }

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;III)V",
            at = @At("RETURN"))
    private void clientmod$pop(MatrixStack matrices, OrderedRenderCommandQueue queue,
                               int light, int overlay, int outline, CallbackInfo ci) {
        matrices.pop();
    }

    @Unique
    private static void applyHand(MatrixStack m, TransformData d, boolean left) {
        float flip = left ? -1f : 1f;
        m.translate(d.px * flip, d.py, d.pz);
        m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(d.rx));
        m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(d.ry * flip));
        m.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(d.rz * flip));
        m.scale(d.scale, d.scale, d.scale);
    }
}
