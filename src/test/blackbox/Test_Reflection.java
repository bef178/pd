package blackbox;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import pd.entity.Cat;

public class Test_Reflection {

    @Test
    public void test_getExtendedClassPublicFields() {
        System.out.println("test_getExtendedClassPublicFields");
        for (Field field : Cat.class.getFields()) {
            String key = field.getName();
            System.out.println("Field: " + key);
        }
    }

    @Test
    public void test_getOwnFields() {
        System.out.println("test_getOwnFields");
        for (Field field : getOwnFields(Cat.class)) {
            String key = field.getName();
            System.out.println("Field: " + key);
        }
    }

    @Test
    public void test_getAllFields() {
        System.out.println("test_getAllFields");
        for (Field field : getAllFields(Cat.class)) {
            String key = field.getName();
            System.out.println("Field: " + key);
        }
    }

    public List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null) {
            fields.addAll(getOwnFields(clazz));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    public List<Field> getOwnFields(Class<?> clazz) {
        assert clazz != null;
        return Arrays.asList(clazz.getDeclaredFields());
    }
}
