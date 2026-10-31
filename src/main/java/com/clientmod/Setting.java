package com.clientmod;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class Setting {
    public final String id, label;

    protected Setting(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public abstract JsonElement save();
    public abstract void load(JsonElement e);
    public abstract void reset();

    public static class Bool extends Setting {
        public boolean value;
        private final boolean def;

        public Bool(String id, String label, boolean def) {
            super(id, label);
            this.value = def;
            this.def = def;
        }

        @Override public JsonElement save() { return new JsonPrimitive(value); }
        @Override public void load(JsonElement e) { if (e.isJsonPrimitive()) value = e.getAsBoolean(); }
        @Override public void reset() { value = def; }
    }

    public static class Num extends Setting {
        public final float min, max, step;
        public final String unit;
        private float value;
        private final float def;
        private Supplier<Float> getter;
        private Consumer<Float> setter;

        public Num(String id, String label, float min, float max, float step, float def, String unit) {
            super(id, label);
            this.min = min;
            this.max = max;
            this.step = step;
            this.def = def;
            this.value = def;
            this.unit = unit;
        }

        /** Binds this slider to external data instead of an own stored value. */
        public Num bind(Supplier<Float> g, Consumer<Float> s) {
            this.getter = g;
            this.setter = s;
            return this;
        }

        public float get() { return getter != null ? getter.get() : value; }

        public void set(float v) {
            v = Math.max(min, Math.min(max, Math.round(v / step) * step));
            v = Math.round(v * 10000f) / 10000f;
            if (setter != null) setter.accept(v); else value = v;
        }

        public float fraction() { return (get() - min) / (max - min); }

        public String fmt() {
            float v = get();
            String s = step >= 1f ? String.valueOf(Math.round(v)) : String.format("%.2f", v);
            return s + unit;
        }

        @Override public JsonElement save() { return getter != null ? JsonNull.INSTANCE : new JsonPrimitive(value); }
        @Override public void load(JsonElement e) { if (getter == null && e.isJsonPrimitive()) value = e.getAsFloat(); }
        @Override public void reset() { set(def); }
    }

    public static class Choice extends Setting {
        public final String[] opts;
        public int value;
        private final int def;

        public Choice(String id, String label, int def, String... opts) {
            super(id, label);
            this.opts = opts;
            this.value = def;
            this.def = def;
        }

        @Override public JsonElement save() { return new JsonPrimitive(value); }
        @Override public void load(JsonElement e) {
            if (e.isJsonPrimitive()) value = Math.max(0, Math.min(opts.length - 1, e.getAsInt()));
        }
        @Override public void reset() { value = def; }
    }

    public static class Color extends Setting {
        public float h, s, v, a;
        public boolean chroma;
        public boolean open; // picker expanded in the menu
        private final float dh, ds, dv, da;

        public Color(String id, String label, int r, int g, int b, float a) {
            super(id, label);
            float[] x = rgbToHsv(r, g, b);
            h = dh = x[0];
            s = ds = x[1];
            v = dv = x[2];
            this.a = da = a;
        }

        public int rgb() {
            float hue = chroma ? (System.currentTimeMillis() / 15L) % 360L : h;
            return hsv(hue, s, v);
        }

        public int argb() {
            return (((int) (a * 255f + .5f)) << 24) | rgb();
        }

        public static int hsv(float h, float s, float v) {
            float c = v * s;
            float hh = (((h % 360f) + 360f) % 360f) / 60f;
            float x = c * (1 - Math.abs(hh % 2 - 1));
            float r = 0, g = 0, b = 0;
            switch ((int) hh) {
                case 0: r = c; g = x; break;
                case 1: r = x; g = c; break;
                case 2: g = c; b = x; break;
                case 3: g = x; b = c; break;
                case 4: r = x; b = c; break;
                default: r = c; b = x; break;
            }
            float m = v - c;
            return (((int) ((r + m) * 255f + .5f)) << 16) | (((int) ((g + m) * 255f + .5f)) << 8) | ((int) ((b + m) * 255f + .5f));
        }

        public static float[] rgbToHsv(int r, int g, int b) {
            float rf = r / 255f, gf = g / 255f, bf = b / 255f;
            float mx = Math.max(rf, Math.max(gf, bf)), mn = Math.min(rf, Math.min(gf, bf)), d = mx - mn;
            float h = 0;
            if (d > 0) {
                if (mx == rf) h = 60f * (((gf - bf) / d) % 6f);
                else if (mx == gf) h = 60f * ((bf - rf) / d + 2f);
                else h = 60f * ((rf - gf) / d + 4f);
            }
            if (h < 0) h += 360f;
            return new float[]{h, mx == 0 ? 0 : d / mx, mx};
        }

        @Override public JsonElement save() {
            JsonArray arr = new JsonArray();
            arr.add(h); arr.add(s); arr.add(v); arr.add(a); arr.add(chroma ? 1 : 0);
            return arr;
        }

        @Override public void load(JsonElement e) {
            if (!e.isJsonArray()) return;
            JsonArray arr = e.getAsJsonArray();
            if (arr.size() < 5) return;
            h = arr.get(0).getAsFloat(); s = arr.get(1).getAsFloat(); v = arr.get(2).getAsFloat();
            a = arr.get(3).getAsFloat(); chroma = arr.get(4).getAsInt() == 1;
        }

        @Override public void reset() { h = dh; s = ds; v = dv; a = da; chroma = false; }
    }
}
