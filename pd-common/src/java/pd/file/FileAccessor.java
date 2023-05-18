package pd.file;

import java.util.List;

/**
 * Abstracts file management operations.
 */
public interface FileAccessor {

    boolean isRegularFile(String path);

    /**
     * Extends `pathPrefix`, stopping at either directory separator `/` or EOF.<br/>
     * `pathPrefix` could be empty.<br/>
     * `.` and `..` has no special meaning in result values.<br/>
     * Result values are complete paths, not basenames.<br/>
     * Result values ending with `/` probably represent a directory (AWS S3 is the exception); those not ending with `/` always represent a regular file.<br/>
     * Result values are deduplicated and sorted.<br/>
     * <br/>
     * e.g.<br/>
     * - list2("d") => ["d/"]<br/>
     * - list2("d/") => ["d/d/", "d/f"]<br/>
     * - list2("f") => ["f"]<br/>
     * - list2("lo") => ["lo/", "long/", "lower"]<br/>
     */
    List<String> list2(String pathPrefix);

    /**
     * `path` identifies a regular file or a directory<br/>
     */
    List<String> listAllRegularFiles(String path);

    /**
     * remove a regular file or directory<br/>
     * `path` identifies a regular file or a directory<br/>
     * <br/>
     * a.k.a. `rm -f, rm -rf`
     */
    boolean remove(String path, boolean recursive);

    /**
     * Remove the regular file identified by `path`.<br/>
     * Return `true` iff the regular file finally does not exist.<br/>
     */
    boolean removeRegularFile(String path);

    /**
     * cp -f, cp -rf
     */
    boolean copy(String path, String dstPath, boolean recursive);

    boolean move(String path, String dstPath, boolean recursive);

    byte[] load(String path);

    boolean save(String path, byte[] bytes);
}
