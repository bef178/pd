package pd.codec.json.json2object;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import pd.codec.json.IJsonObject;
import pd.entity.Cat;
import pd.entity.Dog;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Test_TypeMapping {

    @Test
    public void test_findByClass() {
        TypeMapping typeMapping = new TypeMapping();
        typeMapping.register(List.class, ArrayList.class);
        typeMapping.register(Map.class, LinkedHashMap.class);

        assertEquals(ArrayList.class, typeMapping.find(List.class, "/", null));
        assertEquals(LinkedHashMap.class, typeMapping.find(Map.class, "/", null));
    }

    @Test
    public void test_findByFieldPath() {
        TypeMapping typeMapping = new TypeMapping();
        typeMapping.register(Object.class, IJsonObject.class);
        typeMapping.register("/animals/[]", Dog.class);
        typeMapping.register("/animals/[1]", Cat.class);
        assertEquals(Dog.class, typeMapping.find(Object.class, "/animals/[0]", null));
        assertEquals(Cat.class, typeMapping.find(Object.class, "/animals/[1]", null));
        assertEquals(Dog.class, typeMapping.find(Object.class, "/animals/[2]", null));
    }

    @Test
    public void test_findByBothClassAndFieldPath() {
        TypeMapping typeMapping = new TypeMapping();
        typeMapping.register(Object.class, IJsonObject.class);
        typeMapping.register("/animals/[]", Dog.class);
        typeMapping.register(Object.class,"/animals/[]", Cat.class);
        assertEquals(Cat.class, typeMapping.find(Object.class, "/animals/[0]", null));
    }
}
