package com.clientmod;

import net.minecraft.item.ItemDisplayContext;

/** Duck interface injected into ItemRenderState. */
public interface ItemStateHolder {
    void itemtransformer$set(String id, ItemDisplayContext context);
}
