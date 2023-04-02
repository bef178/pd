package pd.file;

import java.util.List;

/**
 * Abstracts file management operations.
 */
public interface IFileAccessor {

    boolean exists(String path);

    boolean isDirectory(String path);

    boolean isRegularFile(String path);

    /**
     * Return file basename(s) under this directory.<br/>
     * As `ls` in `bash`<br/>
     * Follows symbol links.
     */
    List<String> listDirectory(String path);

    /**
     * return file name(s) under this directory
     */
    List<String> listDirectory2(String path);

    List<String> listRegularFiles(String path, int level);

    /**
     * mkdir, mkdir -p
     */
    boolean makeDirectory(String path, boolean parents);

    /**
     * rmdir, rmdir -p
     */
    boolean removeDirectory(String path, boolean parents);

    /**
     * rm, rm -rf
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
