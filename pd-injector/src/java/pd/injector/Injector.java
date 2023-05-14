package pd.injector;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.reflections.Reflections;

import pd.injector.annotation.Managed;

public class Injector {

    private final PropertyKeeper propertyKeeper = new PropertyKeeper();

    private final InstanceKeeper instanceKeeper = new InstanceKeeper();

    private final String basePackageName;

    public Injector(Class<?> applicationClass) {
        this(applicationClass.getPackage().getName());
    }

    public Injector(String basePackageName) {
        assert basePackageName != null && !basePackageName.isEmpty();
        this.basePackageName = basePackageName;
    }

    public void loadProperties() {
        String name = "application.properties";
        propertyKeeper.load(name);
        String activeProfile = System.getenv("INJANO_ACTIVE_PROFILE");
        if (activeProfile == null) {
            activeProfile = propertyKeeper.getProperty("injano.active_profile");
        }
        if (activeProfile != null) {
            propertyKeeper.load("application-" + activeProfile + ".properties");
        }
    }

    public void scan() {
        Set<Class<?>> managedClasses = new Reflections(basePackageName).getTypesAnnotatedWith(Managed.class, true);
        scan(sort(managedClasses));
    }

    private void scan(Collection<Class<?>> managedClasses) {
        instanceKeeper.instantiateClasses(managedClasses);
        instanceKeeper.injectClassFields(propertyKeeper);
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
        instanceKeeper.injectClassFields(Collections.singletonList(target), propertyKeeper);
    }

    public void dispose() {
        instanceKeeper.clear();
        propertyKeeper.clear();
    }
}
