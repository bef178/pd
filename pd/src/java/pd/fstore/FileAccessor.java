package pd.fstore;

import java.util.List;

import lombok.NonNull;

/**
 * abstract file management operations
 */
public interface FileAccessor {

    /**
     * List keys starting with `prefix`, truncated at the next '/'.<br/>
     * Results are sorted.<br/>
     * Assuming objects: ["d/d/", "d/f", "f", "lo/", "long", "lower/"]<br/>
     * - "d" => ["d/"]<br/>
     * - "d/" => ["d/d/", "d/f"]<br/>
     * - "f" => ["f"]<br/>
     * - "lo" => ["lo/", "lower/", "long"]<br/>
     */
    List<String> list(@NonNull String prefix);

    /**
     * List all keys starting with `prefix`.<br/>
     */
    List<String> listAll(@NonNull String prefix);

    /**
     * Remove all keys starting with `prefix`.<br/>
     */
    boolean removeAll(@NonNull String prefix);

    FileStat stat(@NonNull String key);

    /**
     * Remove a single key.<br/>
     * Return `true` if the operation succeeds.<br/>
     */
    boolean remove(@NonNull String key);

    byte[] load(@NonNull String key);

    boolean save(@NonNull String key, byte[] bytes);
}
