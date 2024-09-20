package pd.json.generalizer;

import pd.json.datatype.Json;

public interface MapToJsonFunc {

    Json map(Class<?> targetClass, Object instance);
}
