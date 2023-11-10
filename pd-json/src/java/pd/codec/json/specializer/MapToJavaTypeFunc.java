package pd.codec.json.specializer;

import pd.codec.json.datatype.Json;

@FunctionalInterface
public interface MapToJavaTypeFunc<T> {

    Class<? extends T> map(Json json, String path, Class<T> targetClass);
}
