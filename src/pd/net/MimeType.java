package pd.net;

/**
 * http://en.wikipedia.org/wiki/MIME
 */
public final class MimeType {

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

    public String toString() {
        return major + '/' + minor;
    }

    /**
     * text/* covers text/json, but not vice versa
     * 
     */
    public boolean covers(MimeType another) {
        return (major.equals("*") || major.equals(another.major))
                && (minor.equals("*") || minor.equals(another.minor));
    }

    public boolean isVideo() {
        return major.equals("video");
    }

    public boolean isAudio() {
        return major.equals("audio");
    }

    public boolean isImage() {
        return major.equals("image");
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
}
