package pd.net.serv.rpc;

import java.util.HashMap;

public class ServiceRegistry {

    public static String getInterfaceClassName(Class<?> interfaceClass) {
        return interfaceClass.getCanonicalName();
    }

    private boolean isFrozen = false;

    private final HashMap<String, Class<?>> registry = new HashMap<>();

    public void freeze() {
        isFrozen = true;
    }

    public Class<?> getImplementationClass(String interfaceClassName) {
        return registry.get(interfaceClassName);
    }

    public boolean register(Class<?> interfaceClass, Class<?> implementationClass) {
        if (isFrozen) {
            return false;
        }
        registry.put(getInterfaceClassName(interfaceClass), implementationClass);
        return true;
    }
}
