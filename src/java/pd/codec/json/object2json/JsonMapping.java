package pd.codec.json.object2json;

import java.util.LinkedHashMap;

public class JsonMapping {

    public final LinkedHashMap<Class<?>, IObjectToJsonFunc> refs = new LinkedHashMap<>();
}
