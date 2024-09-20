package pd.json.specializer;

import pd.json.datatype.Json;

@FunctionalInterface
public interface MapToJavaTypeFunc<T> {

    Class<? extends T> map(Json json, String path, Class<T> targetClass);
}
