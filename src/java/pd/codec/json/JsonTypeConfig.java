package pd.codec.json;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pd.fenc.ParsingException;

class JsonTypeConfig {

    private LinkedHashMap<SimpleImmutableEntry<String, String>, Class<?>> typeRefs = new LinkedHashMap<>();

    public JsonTypeConfig() {
        registerTypeRef(List.class, ArrayList.class);
        registerTypeRef(Map.class, LinkedHashMap.class);
    }

    public Class<?> findPathRef(final String descentPath) {
        assert descentPath != null && !descentPath.isEmpty();
        return getType(null, descentPath);
    }

    // TODO use it
    Class<?> findTypeDescentRef(final Class<?> declaredClass, final String descentPath) {
        assert declaredClass != null;
        assert descentPath != null && !descentPath.isEmpty();
        return getType(declaredClass, descentPath);
    }

    public Class<?> findTypeRef(final Class<?> declaredClass) {
        assert declaredClass != null;

        Class<?> targetClass = getType(declaredClass, null);
        if (targetClass == null) {
            return null;
        }
        while (true) {
            Class<?> next = getType(targetClass, null);
            if (next == null) {
                return targetClass;
            }
            targetClass = next;
        }
    }

    private Class<?> getType(Class<?> clazz, String descentPath) {
        if (typeRefs.isEmpty()) {
            return null;
        }
        SimpleImmutableEntry<String, String> key = new SimpleImmutableEntry<>(
                clazz == null ? null : clazz.getName(), descentPath);
        return typeRefs.get(key);
    }

    private void putType(Class<?> clazz, String descentPathPattern, Class<?> dstClass) {
        assert dstClass != null;
        SimpleImmutableEntry<String, String> key = new SimpleImmutableEntry<>(
                clazz == null ? null : clazz.getName(), descentPathPattern);
        typeRefs.put(key, dstClass);
    }

    /**
     * defines the implementation type at the given path<br/>
     */
    public void registerPathRef(String descentPathPattern, Class<?> implementationClass) {
        assert descentPathPattern != null && !descentPathPattern.isEmpty();
        assert implementationClass != null;
        putType(null, descentPathPattern, implementationClass);
    }

    /**
     * defines the true implementation type at given path of given type<br/>
     */
    void registerTypeDescentRef(Class<?> clazz, String descentPathPattern, Class<?> dstClass) {
        assert clazz != null;
        assert descentPathPattern != null && !descentPathPattern.isEmpty();
        assert dstClass != null;
        putType(clazz, descentPathPattern, dstClass);
    }

    /**
     * defines the true implementation type of declared type<br/>
     * <br/>
     * e.g.#1 call(List.class, ArrayList.class);<br/>
     * e.g.#2 call(HashMap.class, LinkedHashMap.class);<br/>
     * e.g.#3 call(Map.class, HashMap.class); // now Map => LinkedHashMap<br/>
     */
    public void registerTypeRef(Class<?> clazz, Class<?> dstClass) {
        assert clazz != null;
        assert dstClass != null;

        if (clazz == dstClass) {
            throw new ParsingException("E: cannot map one type to itself");
        }
        if (!clazz.isAssignableFrom(dstClass)) {
            throw new ParsingException("E: should map one type to its derived type");
        }

        putType(clazz, null, dstClass);
    }
}
