package pd.fun.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectionDemo {

    public static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null) {
            fields.addAll(getOwnFields(clazz));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    public static List<Field> getOwnFields(Class<?> clazz) {
        assert clazz != null;
        return Arrays.asList(clazz.getDeclaredFields());
    }

    public static void staticMethod(String arg) {
        System.out.println("static method invoked with arg: " + arg);
    }

    public void memberMethod(int arg) {
        System.out.println("member method invoked with arg: " + arg);
    }

    public static void main(String[] args) {
        try {
            reflectionCallStaticMethod();

            ReflectionDemo example = new ReflectionDemo();
            example.reflectionCallMemberMethod();
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }

    }

    static void reflectionCallStaticMethod() throws SecurityException, NoSuchMethodException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Class<?> thisClass = ReflectionDemo.class;
        Method method = thisClass.getDeclaredMethod("staticMethod", String.class);
        method.invoke(thisClass, "hello static");
    }

    void reflectionCallMemberMethod() throws SecurityException, NoSuchMethodException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Class<?> thisClass = this.getClass();
        Method method = thisClass.getDeclaredMethod("memberMethod", int.class);
        method.invoke(this, 42);
    }
}
