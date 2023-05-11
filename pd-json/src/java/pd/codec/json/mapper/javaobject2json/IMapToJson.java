package pd.codec.json.mapper.javaobject2json;

import pd.codec.json.datatype.IJson;

public interface IMapToJson {
    IJson map(Class<?> declaredClass, Object instance);
}
