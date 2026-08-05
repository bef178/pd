package pd.util;

public interface FileRemoveListener {

    void accept(String path, boolean isSucceeded);
}
