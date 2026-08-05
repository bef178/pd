package pd.util;

public class FileStat {

    public static final String TYPE_FILE = "f";
    public static final String TYPE_DIRECTORY = "d";
    public static final String TYPE_UNKNOWN = "?";

    public String type;
    public String path;
    public long contentLength;
    public long lastModified;
}
