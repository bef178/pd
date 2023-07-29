package pd.codec.json.deserializer.json2java;

import pd.codec.json.datatype.Json;

@FunctionalInterface
public interface FindJavaTypeFunc<T> {
    Class<? extends T> map(Json json, String path, Class<T> targetClass);
}
