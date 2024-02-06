package pd.fun.reflection;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import pd.fun.Cat;

public class Test_ReflectionDemo {

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
        for (Field field : ReflectionDemo.getOwnFields(Cat.class)) {
            String key = field.getName();
            System.out.println("Field: " + key);
        }
    }

    @Test
    public void test_getAllFields() {
        System.out.println("test_getAllFields");
        for (Field field : ReflectionDemo.getAllFields(Cat.class)) {
            String key = field.getName();
            System.out.println("Field: " + key);
        }
    }
}
