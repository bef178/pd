package pd.net;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * http://en.wikipedia.org/wiki/MIME
 */
public final class MimeType {

    public static final Map<String, String> ext2mime;

    static {
        HashMap<String, String> m = new HashMap<>();
        m.put("bin", "application/octet-stream");
        m.put("css", "text/css");
        m.put("gif", "image/gif");
        m.put("html", "text/html");
        m.put("ico", "image/x-icon");
        m.put("jpg", "image/jpeg");
        m.put("js", "text/javascript");
        m.put("json", "application/json"); // RFC 4627
        m.put("pdf", "application/pdf");
        m.put("png", "image/png");
        m.put("svg", "image/svg+xml");
        m.put("swf", "application/x-shockwave-flash");
        m.put("txt", "text/plain");
        m.put("wav", "audio/x-wav");
        m.put("wma", "audio/x-ms-wma");
        m.put("wmv", "video/x-ms-wmv");
        m.put("xml", "text/xml");
        ext2mime = Collections.unmodifiableMap(m);
    }

    public static MimeType fromString(String mimeString) {
        String[] a = mimeString.split("/");
        if (a.length != 2) {
            throw new IllegalArgumentException();
        }
        return new MimeType(a[0], a[1]);
    }

    public final String major;

    public final String minor;

    public MimeType(String major, String minor) {
        if (major == null || major.isEmpty() || minor == null || minor.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.major = major.toLowerCase();
        this.minor = minor.toLowerCase();
    }

    /**
     * text/* covers text/json, but not vice versa
     */
    public boolean covers(MimeType another) {
        return (major.equals("*") || major.equals(another.major))
                && (minor.equals("*") || minor.equals(another.minor));
    }

    public boolean isAudio() {
        return major.equals("audio");
    }

    /**
     * there is no normalized mime type for directory, but several are in practice:<br/>
     * "inode/directory"<br/>
     * "application/x-directory"<br/>
     * "vnd.android.document/directory"<br/>
     * for android, see <code>Document.MIME_TYPE_DIR</code> constant<br/>
     */
    public boolean isDirectory() {
        String mimeString = toString();
        return mimeString.equals("inode/directory")
                || mimeString.equals("application/x-directory")
                || mimeString.equals("vnd.android.document/directory");
    }

    public boolean isImage() {
        return major.equals("image");
    }

    public boolean isVideo() {
        return major.equals("video");
    }

    @Override
    public String toString() {
        return major + '/' + minor;
    }
}
