package pd.codec.json;

public interface IFuncConvertToJson<T> {
    IJson convert(T object);
}
