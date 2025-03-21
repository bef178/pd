package pd.injector;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import pd.injector.annotation.Managed;

import static pd.util.ResourceExtension.resourceAsString;

@Slf4j
public class Injector {

    private final ValueKeeper valueKeeper = new ValueKeeper();

    private final InstanceKeeper instanceKeeper = new InstanceKeeper();

    public void loadValuesFromResource(String resourceName) {
        if (resourceName.endsWith(".properties")) {
            String s = resourceAsString(resourceName);
            valueKeeper.loadProperties(s);
            return;
        } else if (resourceName.endsWith(".yaml") || resourceName.endsWith(".yml")) {
            String s = resourceAsString(resourceName);
            valueKeeper.loadYaml(s);
            return;
        }
        log.error("support only properties file and yaml file");
    }

    public Object putValue(String key, Object value) {
        return valueKeeper.put(key, value);
    }

    public void putValues(Map<String, Object> map) {
        valueKeeper.putAll(map);
    }

    public void scan(Class<?> applicationClass) {
        scan(applicationClass.getPackage().getName());
    }

    public void scan(String basePackage) {
        if (basePackage == null || basePackage.isEmpty()) {
            throw new RuntimeException("`basePackage` should not be null or empty");
        }
        Set<Class<?>> managedClasses = new Reflections(basePackage).getTypesAnnotatedWith(Managed.class, true);
        scanManagedClasses(managedClasses);
    }

    public void scan(Set<String> classNames) {
        List<Class<?>> managedClasses = getManagedClasses(classNames, className -> {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                log.error("Failed to load class `{}`", className, e);
            }
            return null;
        });
        scanManagedClasses(managedClasses);
    }

    public void scan(Set<String> classNames, ClassLoader classLoader) {
        List<Class<?>> managedClasses = getManagedClasses(classNames, className -> {
            try {
                return classLoader.loadClass(className);
            } catch (ClassNotFoundException e) {
                log.error("Failed to load class `{}`", className, e);
            }
            return null;
        });
        scanManagedClasses(managedClasses);
    }

    private List<Class<?>> getManagedClasses(Set<String> classNames, Function<String, Class<?>> f) {
        return classNames.stream()
                .map(f::apply)
                .filter(Objects::nonNull)
                .map(a -> a.getAnnotation(Managed.class) == null ? null : a)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void scanManagedClasses(Collection<Class<?>> managedClasses) {
        List<Class<?>> sorted = managedClasses.stream()
                .map(clazz -> {
                    Managed annotation = clazz.getAnnotation(Managed.class);
                    PrioritizedClass a = new PrioritizedClass();
                    a.clazz = clazz;
                    a.priority = annotation.priority();
                    return a;
                })
                .sorted(PrioritizedClass.comparator)
                .map(a -> a.clazz)
                .collect(Collectors.toList());
        instanceKeeper.instantiateClasses(sorted);
        instanceKeeper.injectClassFields(valueKeeper);
        instanceKeeper.invokeCallbacks();
    }

    public void injectClassFields(Object target) {
        instanceKeeper.injectClassFields(Collections.singletonList(target), valueKeeper);
    }

    public void dispose() {
        instanceKeeper.clear();
        valueKeeper.clear();
    }
}
