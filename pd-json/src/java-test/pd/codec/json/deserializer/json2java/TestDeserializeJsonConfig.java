package pd.codec.json.deserializer.json2java;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import pd.file.PathPattern;
import pd.fun.Cat;
import pd.fun.Dog;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestDeserializeJsonConfig {

    @Test
    public void test_findByClass() {
        DeserializeJsonConfig config = new DeserializeJsonConfig();
        config.register(List.class, ArrayList.class);
        config.register(Map.class, LinkedHashMap.class);

        assertEquals(ArrayList.class, config.find(null, "/", List.class));
        assertEquals(LinkedHashMap.class, config.find(null, "/", Map.class));
    }

    @Test
    public void test_findByClassWithPathPattern() {
        DeserializeJsonConfig config = new DeserializeJsonConfig();
        config.register(Object.class, (json, p, c) -> {
            if (p.equals("/animals/[1]")) {
                return Cat.class;
            } else if (PathPattern.singleton().matches("/animals/[*]", p)) {
                return Dog.class;
            }
            return Object.class;
        });
        assertEquals(Dog.class, config.find(null, "/animals/[0]", Object.class));
        assertEquals(Cat.class, config.find(null, "/animals/[1]", Object.class));
        assertEquals(Dog.class, config.find(null, "/animals/[2]", Object.class));
    }
}
