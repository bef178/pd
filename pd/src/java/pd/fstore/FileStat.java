package pd.fstore;

public class FileStat {

    public static final int TYPE_FILE = 'f';
    public static final int TYPE_DIRECTORY_LIKE = 'd';

    public int type;

    public String key;

    public long contentLength;

    public long lastModified;
}
