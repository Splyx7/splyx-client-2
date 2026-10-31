package com.clientmod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class TransformManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("clientmod_items.json");
    private static Map<String, TransformData> map = new HashMap<>();

    private TransformManager() {}

    public static void load() {
        if (!Files.exists(FILE)) return;
        try (Reader r = Files.newBufferedReader(FILE)) {
            Type type = new TypeToken<Map<String, TransformData>>() {}.getType();
            Map<String, TransformData> loaded = GSON.fromJson(r, type);
            if (loaded != null) map = loaded;
        } catch (Exception e) {
            System.err.println("[ClientMod] Could not load item config: " + e);
        }
    }

    public static void save() {
        map.values().removeIf(TransformData::isDefault);
        try (Writer w = Files.newBufferedWriter(FILE)) {
            GSON.toJson(map, w);
        } catch (Exception e) {
            System.err.println("[ClientMod] Could not save item config: " + e);
        }
    }

    /** Returns null if the item has no custom transform. */
    public static TransformData get(String id) {
        TransformData d = map.get(id);
        return (d == null || d.isDefault()) ? null : d;
    }

    public static TransformData getOrCreate(String id) {
        return map.computeIfAbsent(id, k -> new TransformData());
    }

    public static String idOf(ItemStack stack) {
        return Registries.ITEM.getId(stack.getItem()).toString();
    }
}
