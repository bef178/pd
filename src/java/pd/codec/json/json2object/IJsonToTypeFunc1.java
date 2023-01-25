package pd.codec.json.json2object;

import pd.codec.json.IJson;

@FunctionalInterface
public interface IJsonToTypeFunc1<T> {
    Class<? extends T> map(IJson json);
}
