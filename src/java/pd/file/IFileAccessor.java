package pd.file;

import java.util.List;

/**
 * Abstracts file management operations.
 */
public interface IFileAccessor {

    /**
     * Return `true` if a general file exists.<br/>
     */
    boolean exists(String path);

    boolean isDirectory(String path);

    boolean isRegularFile(String path);

    /**
     * Return paths of general files with this prefix; will stop after directory separator.<br/>
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
    List<String> findRegularFiles(String pathPrefix);

    /**
     * mkdir, mkdir -p
     */
    boolean makeDirectory(String path, boolean parents);

    /**
     * rmdir, rmdir -p
     */
    boolean removeDirectory(String path, boolean parents);

    /**
     * Return `true` if the file does not exist or is removed.<br/>
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
