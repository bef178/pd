package pd.util;

public class FileStat {

    public static final int TYPE_FILE = 'f';
    public static final int TYPE_DIRECTORY = 'd';

    public int type;
    public String path;
    public long contentLength;
    public long lastModified;
}
