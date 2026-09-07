package pd.util;

public class FileStat {

    public static final int TYPE_FILE = 0x08;
    public static final int TYPE_DIRECTORY = 0x04;
    public static final int TYPE_SYMLINK = 0x10;
    public static final int TYPE_SPECIAL = 0x01 | 0x02 | 0x20 | 0x40; // don't ask why

    public final String path;
    public int type;
    public Long size; // apply to file and symlink
    public Long mtime; // in milliseconds

    public FileStat(String path) {
        this.path = path;
    }

    public boolean exists() {
        return type != 0;
    }

    public boolean isSymlink() {
        return (type & TYPE_SYMLINK) != 0;
    }

    public boolean isDanglingSymlink() {
        return type == TYPE_SYMLINK;
    }

    public boolean isFile(boolean followSymlinks) {
        return followSymlinks ? (type & ~TYPE_SYMLINK) == TYPE_FILE : type == TYPE_FILE;
    }

    public boolean isDirectory(boolean followSymlinks) {
        return followSymlinks ? (type & ~TYPE_SYMLINK) == TYPE_DIRECTORY : type == TYPE_DIRECTORY;
    }

    public boolean isAnyTypeOf(String... typeStrings) {
        for (String typeString : typeStrings) {
            if (typeString == null) {
                continue;
            }
            if (typeString.equals("*")) {
                if (type != 0) {
                    return true;
                }
            } else if (typeString.equals("l*")) {
                if ((type & TYPE_SYMLINK) != 0) {
                    return true;
                }
            } else {
                int masks = 0;
                for (int i = 0; i < typeString.length(); i++) {
                    switch (typeString.charAt(i)) {
                        case 'f':
                            masks |= TYPE_FILE;
                            break;
                        case 'd':
                            masks |= TYPE_DIRECTORY;
                            break;
                        case 'l':
                            masks |= TYPE_SYMLINK;
                            break;
                        case '?':
                            masks |= TYPE_SPECIAL;
                            break;
                        case '*':
                            masks |= 0x7F;
                        default:
                            break;
                    }
                }
                if (type == masks) {
                    return true;
                }
            }
        }
        return false;
    }
}
