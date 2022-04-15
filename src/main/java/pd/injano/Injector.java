package pd.injano;

import org.reflections.Reflections;

import pd.injano.annotation.Managed;

public class Injector {

    private final PropertyHolder propertyHolder = new PropertyHolder();

    private final InstanceHolder instanceHolder = new InstanceHolder(propertyHolder);

    private final String applicationPackageName;

    public Injector(Class<?> applicationClass) {
        assert applicationClass != null;
        this.applicationPackageName = applicationClass.getPackage().getName();
    }

    public void loadProperties() {
        String name = "application.properties";
        propertyHolder.load(name);
        String activeProfile = System.getenv("INJANO_ACTIVE_PROFILE");
        if (activeProfile == null) {
            activeProfile = propertyHolder.getProperty("injano.active_profile");
        }
        if (activeProfile != null) {
            propertyHolder.load("application-" + activeProfile + ".properties");
        }
    }

    public void scan() {
        Reflections reflections = new Reflections(applicationPackageName);
        instanceHolder.scan(reflections.getTypesAnnotatedWith(Managed.class, true));
    }

    public void inject(Object target) {
        instanceHolder.inject(target);
    }

    public void dispose() {
        instanceHolder.clear();
        propertyHolder.clear();
    }
}
