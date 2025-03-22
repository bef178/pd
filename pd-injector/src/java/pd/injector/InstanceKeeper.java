package pd.injector;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;
import pd.injector.annotation.Managed;

@Slf4j
class InstanceKeeper {

    // className => classInstance
    private final Map<String, Object> cache = new LinkedHashMap<>();

    /**
     * instantiate managed singleton(s)
     */
    public void instantiateClasses(Collection<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            if (clazz.isInterface()) {
                log.info("Skip instantiating interface {}", clazz);
                continue;
            }

            String className = clazz.getCanonicalName();
            if (!cache.containsKey(className)) {
                // createInstance
                try {
                    Constructor<?> constructor = clazz.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    Object instance = constructor.newInstance();
                    cache.put(className, instance);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException("Failed to find no-args constructor \"" + className + "\"", e);
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException e) {
                    throw new RuntimeException("Failed to create instance \"" + className + "\"", e);
                }
            }
        }
    }

    public void injectClassFields(ValueKeeper valueKeeper) {
        injectClassFields(cache.values(), valueKeeper);
    }

    public void injectClassFields(Collection<Object> objects, ValueKeeper valueKeeper) {
        objects.stream()
                .flatMap(instance -> {
                    Managed managedClassAnnotation = instance.getClass().getAnnotation(Managed.class);
                    return Arrays.stream(instance.getClass().getDeclaredFields())
                            .map(field -> {
                                Managed managedFieldAnnotation = field.getAnnotation(Managed.class);
                                if (managedFieldAnnotation == null) {
                                    return null;
                                }
                                PrioritizedClassField a = new PrioritizedClassField();
                                a.instance = instance;
                                a.field = field;
                                a.classAnnotation = managedClassAnnotation;
                                a.fieldAnnotation = managedFieldAnnotation;
                                return a;
                            });
                })
                .filter(Objects::nonNull)
                .sorted(PrioritizedClassField.comparator)
                .forEachOrdered(a -> {
                    if (a.fieldAnnotation != null) {
                        String expr = a.fieldAnnotation.value();
                        if (!expr.isEmpty()) {
                            a.assign(getValue(expr, valueKeeper));
                        } else {
                            a.assign(getInstance(a.field.getType()));
                        }
                    }
                });
    }

    private Object getValue(String expr, ValueKeeper valueKeeper) {
        if (expr.startsWith("${") && expr.endsWith("}")) {
            String[] a = expr.substring(2, expr.length() - 1).split(":", 2);
            if (a[0] == null || a[0].isEmpty()) {
                throw new RuntimeException("key should not be null or empty");
            }
            Object value = valueKeeper.get(a[0]);
            if (value != null) {
                return value;
            } else if (a.length == 2) {
                return a[1];
            } else {
                throw new RuntimeException("Failed to find key `" + a[0] + "`");
            }
        } else {
            return expr;
        }
    }

    private Object getInstance(Class<?> clazz) {
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

    public void invokePostConstructs() {
        cache.values().stream()
                .flatMap(instance -> {
                    Managed managedClassAnnotation = instance.getClass().getAnnotation(Managed.class);
                    return Arrays.stream(instance.getClass().getDeclaredMethods())
                            .map(method -> {
                                if (method.getAnnotation(PostConstruct.class) == null) {
                                    return null;
                                }
                                PrioritizedClassPostConstruct a = new PrioritizedClassPostConstruct();
                                a.instance = instance;
                                a.method = method;
                                a.classAnnotation = managedClassAnnotation;
                                return a;
                            });
                })
                .filter(Objects::nonNull)
                .sorted(PrioritizedClassPostConstruct.comparator)
                .forEachOrdered(PrioritizedClassPostConstruct::invoke);
    }

    public void clear() {
        cache.clear();
    }
}
