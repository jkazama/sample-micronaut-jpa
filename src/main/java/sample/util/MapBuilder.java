package sample.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Simple Map Builder. */
public class MapBuilder {

    public final Map<Object, Object> origin = new ConcurrentHashMap<>();
    
    public MapBuilder put(Object key, Object value) {
        this.origin.put(key, value);
        return this;
    }
    
    public Map<Object, Object> build() {
        Map<Object, Object> result = new ConcurrentHashMap<>(this.origin);
        this.origin.clear();;
        return result;
    }
    
    public static MapBuilder of(Object key, Object value) {
        return new MapBuilder().put(key, value);
    }
    
}
