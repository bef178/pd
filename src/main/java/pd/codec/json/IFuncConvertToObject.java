package pd.codec.json;

public interface IFuncConvertToObject<T> {
    T convert(IJson json, String path);
}
