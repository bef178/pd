package pd.injano;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;
import pd.injano.annotation.FromProperty;
import pd.injano.annotation.Managed;
import pd.injano.annotation.OnConstructed;

@Slf4j
class InstanceHolder {

    private final PropertyHolder propertyHolder;

    // className => classInstance
    private final Map<String, Object> cache = new ConcurrentHashMap<>();

    private boolean isInitialized = false;

    public InstanceHolder(PropertyHolder propertyHolder) {
        assert propertyHolder != null;
        this.propertyHolder = propertyHolder;
    }

    public void scan(Set<Class<?>> managedClasses) {
        if (isInitialized) {
            throw new IllegalStateException();
        }
        isInitialized = true;

        // instantiate managed singleton(s)
        for (Class<?> clazz : managedClasses) {
            if (clazz.isInterface()) {
                log.info("Skipping instantiating interface {}", clazz);
                continue;
            }
            cache.put(clazz.getCanonicalName(), createInstance(clazz));
        }

        for (Object instance : cache.values()) {
            inject(instance);
        }

        // as if onConstructed callback
        for (Object instance : cache.values()) {
            Method[] methods = instance.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(OnConstructed.class)) {
                    try {
                        method.setAccessible(true);
                        method.invoke(instance);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(
                                "Failed to invoke @OnConstructed method \"" + method.getName() + "\"", e);
                    }
                }
            }
        }
    }

    public void inject(Object instance) {
        if (!isInitialized) {
            throw new IllegalStateException();
        }
        for (Field field : instance.getClass().getDeclaredFields()) {
            for (Annotation annotation : field.getAnnotations()) {
                Class<?> annotationType = annotation.annotationType();
                if (annotationType == Managed.class) {
                    assign(instance, field, findInstance(field.getType()));
                }
                if (annotationType == FromProperty.class) {
                    String propertyKey = ((FromProperty) annotation).value();
                    Object value = propertyHolder.getProperty(propertyKey);
                    if (value == null) {
                        throw new IllegalArgumentException("Failed to find property \"" + propertyKey + "\"");
                    }
                    assign(instance, field, value);
                }
            }
        }
    }

    private Object findInstance(Class<?> clazz) {
        Object object = cache.get(clazz.getCanonicalName());
        if (object != null) {
            return object;
        }
        int mod = clazz.getModifiers();
        if (Modifier.isInterface(mod) || Modifier.isAbstract(mod)) {
            for (Object instance : cache.values()) {
                if (clazz.isAssignableFrom(instance.getClass())) {
                    return instance;
                }
            }
        }
        return null;
    }

    public void clear() {
        cache.clear();
    }

    private static void assign(Object instance, Field field, Object value) {
        try {
            field.setAccessible(true);
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Failed to set field \"" + field.getName() + "\"", e);
        }
    }

    private static Object createInstance(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Failed to find no-args constructor \"" + clazz.getCanonicalName() + "\"", e);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new RuntimeException("Failed to create instance \"" + clazz.getCanonicalName() + "\"", e);
        }
    }
}
