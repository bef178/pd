package pd.file;

import java.util.List;

/**
 * Abstracts file management operations.
 */
public interface FileAccessor {

    boolean isRegularFile(String path);

    /**
     * List paths starting with `pathPrefix` and not beyond `/`.<br/>
     * `pathPrefix` could be empty.<br/>
     * Result values are full paths, not base names.<br/>
     * Result values are deduplicated and sorted.<br/>
     * If result value ends with `/`, it probably represents a directory; if not, it represents a regular file.<br/>
     * `.` and `..` has no special meanings in result path.<br/>
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

    byte[] load(String path);

    boolean save(String path, byte[] bytes);
}
