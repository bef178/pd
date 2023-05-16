package pd.file;

import java.util.List;

/**
 * Abstracts file management operations.
 */
public interface FileAccessor {

    boolean isRegularFile(String path);

    /**
     * find paths of those directory files and regular files starting with `pathPrefix`; will stop at first directory separator<br/>
     * <br/>
     * e.g.<br/>
     * - list2("d") => ["d/"]<br/>
     * - list2("d/") => ["d/d/", "d/f"]<br/>
     * - list2("f") => ["f"]<br/>
     * - list2("lo") => ["lo/", "long/", "lower"]<br/>
     */
    List<String> list2(String pathPrefix);

    /**
     * find -type f
     */
    List<String> listAllRegularFiles(String pathPrefix);

    /**
     * mkdir, mkdir -p
     */
    boolean makeDirectory(String path, boolean parents);

    /**
     * rmdir, rmdir -p
     */
    boolean removeDirectory(String path, boolean parents);

    /**
     * return `true` if the file does not exist or is removed<br/>
     * <br/>
     * rm -f, rm -rf
     */
    boolean remove(String path, boolean recursive);

    /**
     * cp, cp -rf
     */
    boolean copy(String path, String dstPath, boolean recursive);

    boolean move(String path, String dstPath);

    byte[] load(String path);

    boolean save(String path, byte[] bytes);
}
