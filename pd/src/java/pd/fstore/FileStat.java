package pd.fstore;

public class FileStat {

    public static final int TYPE_FILE = 'f';
    public static final int TYPE_DIRECTORY = 'd';
    public static final int TYPE_SYMLINK = 'l';

    public String key;
    public int type;
    public long contentLength;
    public long lastModified;
}
