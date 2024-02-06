package pd.fstore;

import java.util.List;

/**
 * abstract file management operations
 */
public interface FileAccessor {

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
     * remove single abstract key<br/>
     * return `true` if the operation succeeds<br/>
     */
    boolean remove(String key);

    /**
     * Remove all keys starting with `keyPrefix`.<br/>
     */
    boolean removeAll(String keyPrefix);

    byte[] load(String key);

    boolean save(String key, byte[] bytes);
}
