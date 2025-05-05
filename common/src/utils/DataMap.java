package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Denne klasse hjælper med at sende maps over GSON.
 */
public class DataMap extends HashMap<String, Object> {
    public DataMap() {
        super();
    }

    /**
     * Opret er data map med et map.
     * @param map
     */
    public DataMap(Map<String, Object> map) {
        super(map);
    }

    /**
     * Hent et felt som et DataMap.
     * @param key - Nøgle
     */
    public DataMap getMap(String key) {
        return new DataMap((Map) super.get(key));
    }

    /**
     * Gem et map.
     * @param key - Nøgle
     * @param value - Mappet som skal gemmes.
     */
    public void putMap(String key, Map<String, Object> value) {
        super.put(key, value);
    }

    /**
     * Gem et map.
     * @param key - Nøgle
     * @param value - Mappet som skal gemmes.
     * @return DataMappet
     */
    public DataMap with(String key, Map<String, Object> value) {
        super.put(key, value);
        return this;
    }

    /**
     * Hent en array af DataMaps fra mappet.
     * @param key - Nøgle
     */
    public List<DataMap> getMapArray(String key) {
        var list = (List<Map<String, Object>>) super.get(key);

        return list.stream().map(m -> new DataMap(m)).toList();
    }

    /**
     * Hent en array fra mappet.
     */
    public <T> List<T> getArray(String key) {
        return (List<T>) super.get(key);
    }

    /**
     * Gem en array fra mappet.
     */
    public <T> void putArray(String key, List<T> value) {
        super.put(key, value);
    }

    public <T> DataMap with(String key, List<T> value) {
        List<Object> fixedList = new ArrayList<>();

        for (int i = 0; i < value.size(); i++) {
            var v = value.get(i);
            if (v instanceof Long || v instanceof Integer) fixedList.add(v.toString());
            else fixedList.add(v);
        }

        super.put(key, fixedList);
        return this;
    }

    public String getString(String key) {
        return (String) super.get(key);
    }

    public void putString(String key, String value) {
        super.put(key, value);
    }

    public DataMap with(String key, String value) {
        super.put(key, value);
        return this;
    }

    public int getInt(String key) {
        return Integer.parseInt((String) super.get(key));
    }

    public void putInt(String key, int value) {
        super.put(key, Integer.toString(value));
    }

    public DataMap with(String key, int value) {
        super.put(key, Integer.toString(value));
        return this;
    }

    public long getLong(String key) {
        return Long.parseLong((String) super.get(key));
    }

    public void putLong(String key, long value) {
        super.put(key, Long.toString(value));
    }

    public DataMap with(String key, long value) {
        super.put(key, Long.toString(value));
        return this;
    }

    public List<Long> getLongsArray(String key) {
        var list = (List<String>) super.get(key);

        return list.stream().map(v -> Long.parseLong(v)).toList();
    }
}
