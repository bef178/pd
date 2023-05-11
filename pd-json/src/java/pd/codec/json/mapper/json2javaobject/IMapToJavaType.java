package pd.codec.json.mapper.json2javaobject;

import pd.codec.json.datatype.IJson;

@FunctionalInterface
public interface IMapToJavaType<T> {
    Class<? extends T> map(IJson json, String path, Class<T> targetClass);
}
