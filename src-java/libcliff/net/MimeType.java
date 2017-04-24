package libcliff.net;

import java.io.File;
import java.util.Locale;

import libcore.net.MimeUtils;

public class MimeType {

    private enum Category {
        REGULAR_FILE, IMAGE, AUDIO, VIDEO;

        private static Category from(String mimeType) {
            String major = splitMimeType(mimeType)[0];
            Category c = null;
            if (major != null) {
                c = Category.fromMajor(major);
                if (c == null) {
                    c = Category.REGULAR_FILE;
                }
            }
            return c;
        }

        public static Category fromMajor(String major) {
            for (Category c : Category.values()) {
                if (c.name().equalsIgnoreCase(major)) {
                    return c;
                }
            }
            return REGULAR_FILE;
        }
    }

    // Document.MIME_TYPE_DIR; "vnd.android.document/directory";
    // "application/x-directory"
    public static final String MIME_DIRECTORY = "inode/directory";

    public static boolean isImage(String mimeType) {
        return Category.from(mimeType) == Category.IMAGE;
    }

    public static boolean isVideo(String mimeType) {
        return Category.from(mimeType) == Category.VIDEO;
    }

    /**
     * MIME, see http://en.wikipedia.org/wiki/MIME<br/>
     * <code>null</code> matches nothing<br/>
     */
    public static boolean matches(String acceptableMimeType, String test) {
        if (acceptableMimeType == null || test == null) {
            return false;
        }
        String[] a1 = splitMimeType(acceptableMimeType);
        String[] a2 = splitMimeType(test);
        return segMatches(a1[0], a2[0]) && segMatches(a1[1], a2[1]);
    }

    /**
     * <code>null</code> matches nothing<br/>
     */
    public static boolean matchesAny(String[] acceptables, String[] tests) {
        if (acceptables != null && tests != null) {
            for (String p : acceptables) {
                for (String s : tests) {
                    if (matches(p, s)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static String guessByFile(File file) {
        if (file.isDirectory()) {
            return MIME_DIRECTORY;
        }

        String path = file.getName();
        int i = path.lastIndexOf('.');
        if (i >= 0) {
            String ext = path.substring(i + 1).toLowerCase(Locale.ROOT);
            return MimeUtils.guessMimeTypeFromExtension(ext);
        }
        return null;
    }

    /**
     * <code>null</code> matches nothing<br/>
     */
    private static boolean segMatches(String p, String s) {
        if (p == null) {
            return false;
        }
        return p.equals("*") || p.equalsIgnoreCase(s);
    }

    /**
     * "use strict";<br/>
     * return [null, null] if illegal grammar<br/>
     * always return an array of two
     */
    private static String[] splitMimeType(String mimeType) {
        if (mimeType != null) {
            int i = mimeType.indexOf('/');
            if (i > 0 && i < mimeType.length() - 1) {
                return new String[] {
                        mimeType.substring(0, i),
                        mimeType.substring(i + 1)
                };
            }
        }
        return new String[] {
                null, null
        };
    }
}
