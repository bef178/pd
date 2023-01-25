package pd.codec.json.json2object;

import pd.codec.json.IJson;

@FunctionalInterface
public interface IJsonToTypeFunc<T> {
    Class<? extends T> map(Class<T> targetClass, String path, IJson json);
}
