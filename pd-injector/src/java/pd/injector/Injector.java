package pd.injector;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import pd.injector.annotation.Managed;

import static pd.util.ResourceExtension.resourceAsString;

@Slf4j
public class Injector {

    private final ValueKeeper valueKeeper = new ValueKeeper();

    private final InstanceKeeper instanceKeeper = new InstanceKeeper();

    private final String basePackageName;

    public Injector(Class<?> applicationClass) {
        this(applicationClass.getPackage().getName());
    }

    public Injector(String basePackageName) {
        assert basePackageName != null && !basePackageName.isEmpty();
        this.basePackageName = basePackageName;
    }

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

    public void scan() {
        Set<Class<?>> managedClasses = new Reflections(basePackageName).getTypesAnnotatedWith(Managed.class, true);
        scan(sort(managedClasses));
    }

    private void scan(Collection<Class<?>> managedClasses) {
        instanceKeeper.instantiateClasses(managedClasses);
        instanceKeeper.injectClassFields(valueKeeper);
        instanceKeeper.invokeCallbacks();
    }

    private List<Class<?>> sort(Set<Class<?>> classes) {
        List<PrioritizedClass> all = new LinkedList<>();
        for (Class<?> clazz : classes) {
            Managed annotation = clazz.getAnnotation(Managed.class);
            if (annotation == null) {
                continue;
            }
            PrioritizedClass a = new PrioritizedClass();
            a.clazz = clazz;
            a.priority = annotation.priority();
            all.add(a);
        }
        all.sort(PrioritizedClass.comparator);
        return all.stream().map(a -> a.clazz).collect(Collectors.toList());
    }

    public void injectClassFields(Object target) {
        instanceKeeper.injectClassFields(Collections.singletonList(target), valueKeeper);
    }

    public void dispose() {
        instanceKeeper.clear();
        valueKeeper.clear();
    }
}
