package pd.injector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;
import pd.injector.annotation.FromProperty;
import pd.injector.annotation.Managed;

@Slf4j
class InstanceKeeper {

    // className => classInstance
    private final Map<String, Object> cache = new ConcurrentHashMap<>();

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
                    throw new RuntimeException(
                            "Failed to find no-args constructor \"" + className + "\"", e);
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

    public void injectClassFields(Collection<Object> instances, ValueKeeper valueKeeper) {
        List<PrioritizedMemberField> all = new LinkedList<>();
        for (Object instance : instances) {
            for (Field field : instance.getClass().getDeclaredFields()) {
                FromProperty annoFromProperty = field.getAnnotation(FromProperty.class);
                if (annoFromProperty != null) {
                    PrioritizedMemberField a = new PrioritizedMemberField();
                    a.field = field;
                    a.instance = instance;
                    a.priority = annoFromProperty.priority();
                    all.add(a);
                    continue;
                }

                Managed annoManaged = field.getAnnotation(Managed.class);
                if (annoManaged != null) {
                    PrioritizedMemberField a = new PrioritizedMemberField();
                    a.field = field;
                    a.instance = instance;
                    a.priority = annoManaged.priority();
                    all.add(a);
                    continue;
                }
            }
        }
        all.sort(PrioritizedMemberField.comparator);

        for (PrioritizedMemberField a : all) {
            FromProperty annoFromProperty = a.field.getAnnotation(FromProperty.class);
            if (annoFromProperty != null) {
                String propertyKey = annoFromProperty.value();
                if (!valueKeeper.containsKey(propertyKey)) {
                    throw new IllegalArgumentException("Failed to find property \"" + propertyKey + "\"");
                }
                a.assign(valueKeeper.get(propertyKey));
                continue;
            }

            Managed annoManaged = a.field.getAnnotation(Managed.class);
            if (annoManaged != null) {
                a.assign(findInstance(a.field.getType()));
                continue;
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
