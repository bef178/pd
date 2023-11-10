package pd.codec.json.generalizer;

import pd.codec.json.datatype.Json;

public interface MapToJsonFunc {

    Json map(Class<?> targetClass, Object instance);
}
