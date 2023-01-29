package pd.codec.json.object2json;

import pd.codec.json.IJson;

public interface IObjectToJsonFunc {
    IJson convert(Object object);
}
