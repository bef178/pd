package pd.json.specializer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import pd.fun.Cat;
import pd.fun.Dog;
import pd.util.PathExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSpecializer {

    @Test
    public void test_deduceMappedClassFromConfig_default() {
        Specializer specializer = new Specializer(new SpecializingConfig());

        assertEquals(ArrayList.class, specializer.deduceMappedClassFromConfig(null, "/", List.class));
        assertEquals(LinkedHashMap.class, specializer.deduceMappedClassFromConfig(null, "/", Map.class));
    }

    @Test
    public void test_deduceMappedClassFromConfig_custom() {
        Specializer specializer = new Specializer(new SpecializingConfig());
        specializer.config.register(Object.class, (json, p, c) -> {
            if (p.equals("/animals/[1]")) {
                return Cat.class;
            } else if (PathExtension.matches(p, "/animals/[*]")) {
                return Dog.class;
            }
            return Object.class;
        });
        assertEquals(Dog.class, specializer.deduceMappedClassFromConfig(null, "/animals/[0]", Object.class));
        assertEquals(Cat.class, specializer.deduceMappedClassFromConfig(null, "/animals/[1]", Object.class));
        assertEquals(Dog.class, specializer.deduceMappedClassFromConfig(null, "/animals/[2]", Object.class));
    }
}
