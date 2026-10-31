package com.clientmod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Module {
    public final String id, name, desc, category;
    public final boolean implemented;
    public boolean enabled;
    public final Map<String, List<Setting>> groups = new LinkedHashMap<>();
    private final Map<String, Setting> byId = new HashMap<>();

    public Module(String id, String name, String desc, String category, boolean implemented) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.category = category;
        this.implemented = implemented;
    }

    public Module add(String group, Setting s) {
        groups.computeIfAbsent(group, k -> new ArrayList<>()).add(s);
        byId.put(s.id, s);
        return this;
    }

    public List<Setting> all() {
        List<Setting> out = new ArrayList<>();
        for (List<Setting> l : groups.values()) out.addAll(l);
        return out;
    }

    public Setting find(String id) { return byId.get(id); }
    public Setting.Num num(String id) { return (Setting.Num) byId.get(id); }

    public boolean b(String id) { return ((Setting.Bool) byId.get(id)).value; }
    public float n(String id) { return ((Setting.Num) byId.get(id)).get(); }
    public int ch(String id) { return ((Setting.Choice) byId.get(id)).value; }
    public int c(String id) { return ((Setting.Color) byId.get(id)).argb(); }
}
