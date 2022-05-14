package com.gmail.val59000mc.configuration.options;

import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.Map;

public class MapOption<T> implements Option<T> {

    private final String path;
    private final T def;

    public MapOption(String path, T def){
        this.path = path;
        this.def = def;
    }

    public Map<String, T> getMapValue(YamlConfiguration config){
        Map<String, T> result = new HashMap<>();

        for (String key : config.getConfigurationSection(path).getKeys(false)) {
            if (def instanceof String) {
                result.put(key, (T) config.getString(path + "." + key));
            }
            if (def instanceof Integer){
                result.put(key, (T) ((Integer) config.getInt(path + "." + key)));
            }
            if (def instanceof Double){
                result.put(key, (T) ((Double) config.getDouble(path + "." + key)));
            }
            if (def instanceof Long){
                result.put(key, (T) ((Long) config.getLong(path + "." + key)));
            }
            if (def instanceof Boolean){
                result.put(key, (T) ((Boolean) config.getBoolean(path + "." + key)));
            }
        }

        return result;
    }

    @Override
    public T getValue(YamlConfiguration config) {
        return null;
    }
}
