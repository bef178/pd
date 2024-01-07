package pd.file;

import java.util.List;

/**
 * abstract file management operations
 */
public interface FileAccessor {

    boolean isRegularFile(String path);

    /**
     * List next level key prefixes (stop after `/`) and keys.<br/>
     * `keyPrefix` could be empty.<br/>
     * Result values are sorted and all start with `keyPrefix`.<br/>
     * e.g.<br/>
     * - "d" => ["d/"]<br/>
     * - "d/" => ["d/d/", "d/f"]<br/>
     * - "f" => ["f"]<br/>
     * - "lo" => ["lo/", "long/", "lower"]<br/>
     */
    List<String> list(String keyPrefix);

    /**
     * List all keys starting with `keyPrefix`.<br/>
     */
    List<String> listAll(String keyPrefix);

    FileStat stat(String key);

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
