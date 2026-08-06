package pd.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Test_FileOps {

    private final FileOps fileOps = FileOps.singleton;

    private static void writeFile(Path p, String content) throws IOException {
        Files.createDirectories(p.getParent());
        Files.write(p, content.getBytes());
    }

    private static void mkdir(Path p) throws IOException {
        Files.createDirectories(p);
    }

    // returns false (so the caller can skip) if symbolic links are not supported here
    private static boolean createSymbolicLink(Path link, Path target) throws IOException {
        try {
            Files.createSymbolicLink(link, target);
            return true;
        } catch (UnsupportedOperationException | IOException e) {
            return false;
        }
    }

    // self-constructed tree (not derived from any source):
    //   docs/readme.md, docs/img/a.png, src/Main.java, src/util/U.java, .gitignore, empty/
    private static Path buildTree(Path root) throws IOException {
        mkdir(root);
        writeFile(root.resolve("docs/readme.md"), "readme");
        writeFile(root.resolve("docs/img/a.png"), "png");
        writeFile(root.resolve("src/Main.java"), "main");
        writeFile(root.resolve("src/util/U.java"), "u");
        writeFile(root.resolve(".gitignore"), "git");
        mkdir(root.resolve("empty"));
        return root;
    }

    // ---- FileOpsCore methods ----

    @Nested
    class listDirectory {

        @Test
        void listsDirectChildrenWithTrailingSlashForDirectories(@TempDir Path tmp) throws IOException {
            Path root = buildTree(tmp.resolve("root"));

            List<String> result = fileOps.listDirectory(root.resolve("docs").toString());

            assertEquals(2, result.size());
            assertEquals(root.resolve("docs/img").toString() + "/", result.get(0));
            assertEquals(root.resolve("docs/readme.md").toString(), result.get(1));
        }

        @Test
        void returnsNullWhenDirectoryDoesNotExist(@TempDir Path tmp) {
            assertNull(fileOps.listDirectory(tmp.resolve("nope").toString()));
        }

        @Test
        void returnsNullWhenPathIsAFile(@TempDir Path tmp) throws IOException {
            Path root = buildTree(tmp.resolve("root"));

            // a file is not a directory -> null
            assertNull(fileOps.listDirectory(root.resolve(".gitignore").toString()));
        }

        @Test
        void returnsEmptyForEmptyDirectory(@TempDir Path tmp) throws IOException {
            Path root = buildTree(tmp.resolve("root"));

            List<String> result = fileOps.listDirectory(root.resolve("empty").toString());

            assertTrue(result.isEmpty());
        }

        @Test
        void throwsWhenDirectoryIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> fileOps.listDirectory(""));
        }
    }

    @Nested
    class createDirectory {

        @Test
        void createsSingleLevelWhenParentsFalse(@TempDir Path tmp) {
            assertTrue(fileOps.createDirectory(tmp.resolve("d").toString(), false));
            assertTrue(Files.isDirectory(tmp.resolve("d")));
        }

        @Test
        void returnsFalseWhenAlreadyExists(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("d");
            mkdir(d);

            assertFalse(fileOps.createDirectory(d.toString(), false));
        }

        @Test
        void returnsFalseWhenParentMissingAndParentsFalse(@TempDir Path tmp) {
            assertFalse(fileOps.createDirectory(tmp.resolve("missing/d").toString(), false));
            assertFalse(Files.exists(tmp.resolve("missing")));
        }

        @Test
        void createsIntermediateParentsWhenParentsTrue(@TempDir Path tmp) {
            assertTrue(fileOps.createDirectory(tmp.resolve("a/b/c").toString(), true));
            assertTrue(Files.isDirectory(tmp.resolve("a/b/c")));
        }

        @Test
        void isIdempotentWhenAlreadyExistsAndParentsTrue(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("d");
            mkdir(d);

            assertTrue(fileOps.createDirectory(d.toString(), true));
        }

        @Test
        void returnsFalseWhenPathIsAnExistingFile(@TempDir Path tmp) throws IOException {
            Path f = tmp.resolve("f");
            writeFile(f, "x");

            assertFalse(fileOps.createDirectory(f.toString(), false));
            assertTrue(Files.isRegularFile(f));
        }

        @Test
        void throwsWhenPathIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> fileOps.createDirectory("", false));
        }

        @Test
        void createsDirectoryWhenNotExistsSingleArg(@TempDir Path tmp) {
            assertTrue(fileOps.createDirectory(tmp.resolve("d").toString()));
            assertTrue(Files.isDirectory(tmp.resolve("d")));
        }

        @Test
        void returnsFalseWhenAlreadyExistsSingleArg(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("d");
            mkdir(d);

            assertFalse(fileOps.createDirectory(d.toString()));
        }

        @Test
        void returnsFalseWhenParentMissingSingleArg(@TempDir Path tmp) {
            assertFalse(fileOps.createDirectory(tmp.resolve("missing/d").toString()));
        }

        @Test
        void doesNotFollowSymlinkForCreate(@TempDir Path tmp) throws IOException {
            Path target = tmp.resolve("target");
            mkdir(target);
            Path link = tmp.resolve("link");
            Assumptions.assumeTrue(createSymbolicLink(link, target));

            assertFalse(fileOps.createDirectory(link.toString()));
        }

        @Test
        void throwsWhenPathIsEmptySingleArgCreate() {
            assertThrows(IllegalArgumentException.class, () -> fileOps.createDirectory(""));
        }
    }

    @Nested
    class removeDirectory {

        @Test
        void removesExistingEmptyDirectory(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("d");
            mkdir(d);

            assertTrue(fileOps.removeDirectory(d.toString()));
            assertFalse(Files.exists(d));
        }

        @Test
        void returnsFalseWhenNotExists(@TempDir Path tmp) {
            assertFalse(fileOps.removeDirectory(tmp.resolve("nope").toString()));
        }

        @Test
        void returnsFalseWhenNotEmpty(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("d");
            mkdir(d);
            writeFile(d.resolve("f"), "f");

            assertFalse(fileOps.removeDirectory(d.toString()));
            assertTrue(Files.exists(d));
        }

        @Test
        void returnsFalseForSymlinkToDirectory(@TempDir Path tmp) throws IOException {
            Path target = tmp.resolve("target");
            mkdir(target);
            Path link = tmp.resolve("link");
            Assumptions.assumeTrue(createSymbolicLink(link, target));

            assertFalse(fileOps.removeDirectory(link.toString()));
        }

        @Test
        void throwsWhenPathIsEmptySingleArg() {
            assertThrows(IllegalArgumentException.class, () -> fileOps.removeDirectory(""));
        }

        @Test
        void removesEmptyDirectoryWhenNotRecursive(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("d");
            mkdir(d);

            assertTrue(fileOps.removeDirectory(d.toString(), false, false, null, null));
            assertFalse(Files.exists(d));
        }

        @Test
        void returnsFalseForNonEmptyWhenNotRecursive(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("d");
            mkdir(d);
            writeFile(d.resolve("f"), "f");

            assertFalse(fileOps.removeDirectory(d.toString(), false, false, null, null));
            assertTrue(Files.exists(d));
            assertTrue(Files.exists(d.resolve("f")));
        }

        @Test
        void removesTreeWhenRecursive(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("d");
            mkdir(d.resolve("sub"));
            writeFile(d.resolve("f"), "f");
            writeFile(d.resolve("sub/g"), "g");

            assertTrue(fileOps.removeDirectory(d.toString(), true, false, null, null));
            assertFalse(Files.exists(d));
        }

        @Test
        void returnsFalseWhenTargetDoesNotExist(@TempDir Path tmp) {
            assertFalse(fileOps.removeDirectory(tmp.resolve("nope").toString(), false, false, null, null));
        }

        @Test
        void removesEmptyAncestorsWhenParentsTrue(@TempDir Path tmp) throws IOException {
            Path leaf = tmp.resolve("p/q/r");
            mkdir(leaf);

            assertTrue(fileOps.removeDirectory(leaf.toString(), false, true, null, null));

            assertFalse(Files.exists(tmp.resolve("p")));
        }

        @Test
        void stopsAtNonEmptyAncestorWhenParentsTrue(@TempDir Path tmp) throws IOException {
            // p holds a file, so it must survive removing the empty p/q/r chain
            Path leaf = tmp.resolve("p/q/r");
            mkdir(leaf);
            writeFile(tmp.resolve("p/keep"), "k");

            assertTrue(fileOps.removeDirectory(leaf.toString(), false, true, null, null));
            assertTrue(Files.exists(tmp.resolve("p")));
            assertFalse(Files.exists(tmp.resolve("p/q")));
            assertTrue(Files.exists(tmp.resolve("p/keep")));
        }

        @Test
        void returnsFalseAndKeepsTreeWhenAbortRequested(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("d");
            mkdir(d.resolve("sub"));
            writeFile(d.resolve("f"), "f");
            writeFile(d.resolve("sub/g"), "g");

            assertFalse(fileOps.removeDirectory(d.toString(), true, false, new AtomicBoolean(true), null));
            // aborted before any deletion: the tree is intact
            assertTrue(Files.exists(d));
            assertTrue(Files.exists(d.resolve("f")));
            assertTrue(Files.exists(d.resolve("sub/g")));
        }

        @Test
        void abortStopsParentChainRemoval(@TempDir Path tmp) throws IOException {
            // p/q/r: r is removed, then abort stops the upward sweep at q
            Path leaf = tmp.resolve("p/q/r");
            mkdir(leaf);
            AtomicBoolean abort = new AtomicBoolean(false);

            // cannot inject mid-call; pre-setting abort would stop r itself. So assert that
            // with abort pre-set, the leaf is not removed at all (entry check).
            abort.set(true);
            assertFalse(fileOps.removeDirectory(leaf.toString(), false, true, abort, null));
            assertTrue(Files.exists(leaf));
        }

        @Test
        void removesSymbolicLinkItselfNotTarget(@TempDir Path tmp) throws IOException {
            // removeDirectory on a symlink-to-directory deletes the link itself, not the target:
            // the target directory and all its content are left untouched
            Path dir = tmp.resolve("dir");
            mkdir(dir.resolve("sub"));
            writeFile(dir.resolve("f"), "f");
            Path link = tmp.resolve("link");
            Assumptions.assumeTrue(createSymbolicLink(link, dir));

            assertTrue(fileOps.removeDirectory(link.toString(), true, false, null, null));
            // the symlink is gone...
            assertFalse(Files.exists(link, LinkOption.NOFOLLOW_LINKS));
            // ...and the target directory is fully intact (not followed into)
            assertTrue(Files.exists(dir));
            assertTrue(Files.exists(dir.resolve("f")));
            assertTrue(Files.exists(dir.resolve("sub")));
        }

        @Test
        void removesSymlinkChildItselfNotTargetWhenRecursive(@TempDir Path tmp) throws IOException {
            // when removing a parent tree, a symlink-to-directory child is removed as the link
            // itself; its target (and the target's content) is left untouched
            Path parent = tmp.resolve("p");
            mkdir(parent);
            Path ext = tmp.resolve("ext");
            mkdir(ext);
            writeFile(ext.resolve("e"), "e");
            Path sl = parent.resolve("sl");
            Assumptions.assumeTrue(createSymbolicLink(sl, ext));
            writeFile(parent.resolve("file"), "x");

            assertTrue(fileOps.removeDirectory(parent.toString(), true, false, null, null));
            assertFalse(Files.exists(parent));
            // the external target directory and its content survive (not followed)
            assertTrue(Files.exists(ext));
            assertTrue(Files.exists(ext.resolve("e")));
        }

        @Test
        void throwsWhenPathIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> fileOps.removeDirectory("", false, false, null, null));
        }

        @Test
        void listenerNotifiedForEachRemovedEntry(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("d");
            mkdir(d.resolve("sub"));
            writeFile(d.resolve("f"), "f");
            writeFile(d.resolve("sub/g"), "g");

            List<String> removed = new java.util.ArrayList<>();
            FileOps.OnRemovedListener listener = (path, ok) -> removed.add(path);

            assertTrue(fileOps.removeDirectory(d.toString(), true, false, null, listener));
            assertEquals(4, removed.size());
            assertEquals(d.resolve("f").toString(), removed.get(0));
            assertEquals(d.resolve("sub/g").toString(), removed.get(1));
            assertEquals(d.resolve("sub").toString(), removed.get(2));
            assertEquals(d.toString(), removed.get(3));
            assertFalse(Files.exists(d));
        }

        @Test
        void listenerNotifiedForAncestorsWhenParentsTrue(@TempDir Path tmp) throws IOException {
            Path leaf = tmp.resolve("p/q/r");
            mkdir(leaf);

            List<String> removed = new java.util.ArrayList<>();
            FileOps.OnRemovedListener listener = (path, ok) -> removed.add(path);

            assertTrue(fileOps.removeDirectory(leaf.toString(), false, true, null, listener));
            assertTrue(removed.contains(leaf.toString()));
            assertTrue(removed.contains(tmp.resolve("p/q").toString()));
            assertTrue(removed.contains(tmp.resolve("p").toString()));
            assertFalse(Files.exists(tmp.resolve("p")));
        }

        @Test
        void listenerNotNotifiedWhenAbortRequested(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("d");
            mkdir(d);
            writeFile(d.resolve("f"), "f");

            List<String> removed = new java.util.ArrayList<>();
            FileOps.OnRemovedListener listener = (path, ok) -> removed.add(path);

            assertFalse(fileOps.removeDirectory(d.toString(), true, false, new AtomicBoolean(true), listener));
            assertTrue(removed.isEmpty());
        }
    }

    @Nested
    class removeFile {

        @Test
        void removesExistingFile(@TempDir Path tmp) throws IOException {
            Path f = tmp.resolve("a.txt");
            writeFile(f, "x");

            assertTrue(fileOps.removeFile(f.toString()));
            assertFalse(Files.exists(f));
        }

        @Test
        void returnsFalseWhenFileDoesNotExist(@TempDir Path tmp) {
            assertFalse(fileOps.removeFile(tmp.resolve("nope").toString()));
        }

        @Test
        void returnsFalseForDirectory(@TempDir Path tmp) throws IOException {
            // removeFile only deletes files; a directory (empty or not) is left untouched
            Path empty = tmp.resolve("empty");
            mkdir(empty);
            assertFalse(fileOps.removeFile(empty.toString()));
            assertTrue(Files.exists(empty));

            Path nonEmpty = tmp.resolve("d");
            mkdir(nonEmpty);
            writeFile(nonEmpty.resolve("f"), "f");
            assertFalse(fileOps.removeFile(nonEmpty.toString()));
            assertTrue(Files.exists(nonEmpty));
            assertTrue(Files.exists(nonEmpty.resolve("f")));
        }

        @Test
        void removesSymbolicLinkToFile(@TempDir Path tmp) throws IOException {
            Path target = tmp.resolve("target");
            writeFile(target, "x");
            Path link = tmp.resolve("link");
            Assumptions.assumeTrue(createSymbolicLink(link, target));

            assertTrue(fileOps.removeFile(link.toString()));
            assertFalse(Files.exists(link));
            // the link target survives
            assertTrue(Files.exists(target));
        }

        @Test
        void removesSymbolicLinkToDirectory(@TempDir Path tmp) throws IOException {
            // a symlink to a directory is not itself a directory; removeFile deletes the link, not the target
            Path dir = tmp.resolve("dir");
            mkdir(dir);
            Path link = tmp.resolve("linkdir");
            Assumptions.assumeTrue(createSymbolicLink(link, dir));

            assertTrue(fileOps.removeFile(link.toString()));
            assertFalse(Files.exists(link));
            assertTrue(Files.exists(dir));
        }

        @Test
        void throwsWhenPathIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> fileOps.removeFile(""));
        }
    }

    @Nested
    class load {

        @Test
        void loadsContentOfExistingFile(@TempDir Path tmp) throws IOException {
            Path f = tmp.resolve("a.txt");
            writeFile(f, "hello");

            byte[] result = fileOps.load(f.toString());

            assertArrayEquals("hello".getBytes(), result);
        }

        @Test
        void loadsEmptyFile(@TempDir Path tmp) throws IOException {
            Path f = tmp.resolve("empty");
            writeFile(f, "");

            byte[] result = fileOps.load(f.toString());

            assertEquals(0, result.length);
        }

        @Test
        void returnsNullWhenFileDoesNotExist(@TempDir Path tmp) {
            assertNull(fileOps.load(tmp.resolve("nope").toString()));
        }

        @Test
        void returnsNullForDirectory(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("d");
            mkdir(d);

            assertNull(fileOps.load(d.toString()));
        }

        @Test
        void followsSymbolicLinkToFile(@TempDir Path tmp) throws IOException {
            // Files.exists and Files.readAllBytes follow symlinks, so load reads the target content
            Path target = tmp.resolve("target");
            writeFile(target, "content");
            Path link = tmp.resolve("link");
            Assumptions.assumeTrue(createSymbolicLink(link, target));

            byte[] result = fileOps.load(link.toString());

            assertArrayEquals("content".getBytes(), result);
        }

        @Test
        void returnsNullForBrokenSymbolicLink(@TempDir Path tmp) throws IOException {
            // Files.exists follows symlinks; a broken symlink → exists=false → null
            Path target = tmp.resolve("missing");
            Path link = tmp.resolve("link");
            Assumptions.assumeTrue(createSymbolicLink(link, target));

            assertNull(fileOps.load(link.toString()));
        }

        @Test
        void throwsWhenPathIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> fileOps.load(""));
        }
    }

    @Nested
    class save {

        @Test
        void savesBytesToNewFile(@TempDir Path tmp) throws IOException {
            Path f = tmp.resolve("a.txt");

            assertTrue(fileOps.save(f.toString(), "hello".getBytes()));
            assertArrayEquals("hello".getBytes(), Files.readAllBytes(f));
        }

        @Test
        void overwritesExistingFile(@TempDir Path tmp) throws IOException {
            Path f = tmp.resolve("a.txt");
            writeFile(f, "old");

            assertTrue(fileOps.save(f.toString(), "new".getBytes()));
            assertArrayEquals("new".getBytes(), Files.readAllBytes(f));
        }

        @Test
        void createsParentDirectories(@TempDir Path tmp) throws IOException {
            Path f = tmp.resolve("a/b/c.txt");

            assertTrue(fileOps.save(f.toString(), "x".getBytes()));
            assertTrue(Files.exists(f));
            assertArrayEquals("x".getBytes(), Files.readAllBytes(f));
        }

        @Test
        void returnsFalseForDirectory(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("d");
            mkdir(d);

            assertFalse(fileOps.save(d.toString(), "x".getBytes()));
        }

        @Test
        void writesThroughSymbolicLinkToFile(@TempDir Path tmp) throws IOException {
            // Files.write follows symlinks, so the target content is updated
            Path target = tmp.resolve("target");
            writeFile(target, "old");
            Path link = tmp.resolve("link");
            Assumptions.assumeTrue(createSymbolicLink(link, target));

            assertTrue(fileOps.save(link.toString(), "new".getBytes()));
            assertArrayEquals("new".getBytes(), Files.readAllBytes(target));
        }

        @Test
        void throwsWhenPathIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> fileOps.save("", "x".getBytes()));
        }
    }

    @Nested
    class stat {

        @Test
        void returnsFileTypeWithContentLengthForRegularFile(@TempDir Path tmp) throws IOException {
            Path f = tmp.resolve("a.txt");
            writeFile(f, "hello");

            FileStat result = fileOps.stat(f.toString());

            assertNotNull(result);
            assertEquals("f", result.type);
            assertEquals(5, result.contentLength);
            assertEquals(f.toString(), result.path);
        }

        @Test
        void returnsDirectoryTypeForDirectory(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("d");
            mkdir(d);

            FileStat result = fileOps.stat(d.toString());

            assertNotNull(result);
            assertEquals("d", result.type);
        }

        @Test
        void returnsNullWhenPathDoesNotExist(@TempDir Path tmp) {
            assertNull(fileOps.stat(tmp.resolve("nope").toString()));
        }

        @Test
        void returnsSymlinkTypeAndOwnAttributesForSymlinkToFile(@TempDir Path tmp) throws IOException {
            // symlink to file: type = "lf", contentLength/lastModified are the link's own
            Path target = tmp.resolve("target");
            writeFile(target, "content");
            Path link = tmp.resolve("link");
            Assumptions.assumeTrue(createSymbolicLink(link, target));

            FileStat result = fileOps.stat(link.toString());

            assertNotNull(result);
            assertEquals("lf", result.type);
            // symlink's own size = length of the target path it stores
            assertEquals(target.toString().length(), result.contentLength,
                    String.format("E: symlink own size, target path: `%s`", target));
        }

        @Test
        void returnsSymlinkTypeAndOwnAttributesForSymlinkToDirectory(@TempDir Path tmp) throws IOException {
            // symlink to directory: type = "ld", attributes are the link's own
            Path dir = tmp.resolve("dir");
            mkdir(dir);
            Path link = tmp.resolve("link");
            Assumptions.assumeTrue(createSymbolicLink(link, dir));

            FileStat result = fileOps.stat(link.toString());

            assertNotNull(result);
            assertEquals("ld", result.type);
            // symlink's own size = length of the target path it stores
            assertEquals(dir.toString().length(), result.contentLength,
                    String.format("E: symlink own size, target path: `%s`", dir));
        }

        @Test
        void returnsSymlinkOnlyForBrokenSymlink(@TempDir Path tmp) throws IOException {
            // broken symlink: type = "l" (no target type), attributes are the link's own
            Path target = tmp.resolve("missing");
            Path link = tmp.resolve("link");
            Assumptions.assumeTrue(createSymbolicLink(link, target));

            FileStat result = fileOps.stat(link.toString());

            assertNotNull(result);
            assertEquals("l", result.type);
            // symlink's own size = length of the stored target path
            assertEquals(target.toString().length(), result.contentLength,
                    String.format("E: symlink own size, target path: `%s`", target));
        }

        @Test
        void throwsWhenPathIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> fileOps.stat(""));
        }
    }

    // ---- FileOps methods ----

    @Nested
    class list {

        @Test
        void listsDirectChildrenWithTrailingSlashForDirectories(@TempDir Path tmp) throws IOException {
            Path root = buildTree(tmp.resolve("root"));

            List<String> result = fileOps.list(root.resolve("docs").toString() + "/");

            assertEquals(2, result.size());
            assertEquals(root.resolve("docs/img").toString() + "/", result.get(0));
            assertEquals(root.resolve("docs/readme.md").toString(), result.get(1));
        }

        @Test
        void returnsEntryItselfWhenPrefixIsADirectory(@TempDir Path tmp) throws IOException {
            Path root = buildTree(tmp.resolve("root"));

            List<String> result = fileOps.list(root.resolve("docs").toString());

            assertEquals(1, result.size());
            assertEquals(root.resolve("docs").toString() + "/", result.get(0));
        }

        @Test
        void returnsEntryItselfWhenPrefixIsAFile(@TempDir Path tmp) throws IOException {
            Path root = buildTree(tmp.resolve("root"));

            List<String> result = fileOps.list(root.resolve(".gitignore").toString());

            assertEquals(1, result.size());
            assertEquals(root.resolve(".gitignore").toString(), result.get(0));
        }

        @Test
        void returnsEmptyForEmptyDirectory(@TempDir Path tmp) throws IOException {
            Path root = buildTree(tmp.resolve("root"));

            List<String> result = fileOps.list(root.resolve("empty").toString() + "/");

            assertTrue(result.isEmpty());
        }

        @Test
        void returnsEmptyForNonExistentPrefix(@TempDir Path tmp) throws IOException {
            Path root = buildTree(tmp.resolve("root"));

            List<String> result = fileOps.list(root.resolve("nope").toString());

            assertTrue(result.isEmpty());
        }

        @Test
        void listsByPrefixAcrossDirectoryAndFiles(@TempDir Path tmp) throws IOException {
            // mirrors the documented example: prefix "lo" matches lo/, long, lower/
            Path root = tmp.resolve("root");
            mkdir(root.resolve("lo"));
            mkdir(root.resolve("lower"));
            writeFile(root.resolve("long"), "long");

            List<String> result = fileOps.list(root.resolve("lo").toString());

            assertEquals(3, result.size());
            assertEquals(root.resolve("lo").toString() + "/", result.get(0));
            assertEquals(root.resolve("long").toString(), result.get(1));
            assertEquals(root.resolve("lower").toString() + "/", result.get(2));
        }

        @Test
        void matchesDocumentedDirectoryPrefixExamples(@TempDir Path tmp) throws IOException {
            // mirrors the documented examples:
            //   list("d")  => ["d/"]
            //   list("d/") => ["d/d/", "d/f"]
            Path root = tmp.resolve("root");
            mkdir(root.resolve("d/d"));
            writeFile(root.resolve("d/f"), "f");

            List<String> asPrefix = fileOps.list(root.resolve("d").toString());
            assertEquals(1, asPrefix.size());
            assertEquals(root.resolve("d").toString() + "/", asPrefix.get(0));

            List<String> asDirectory = fileOps.list(root.resolve("d").toString() + "/");
            assertEquals(2, asDirectory.size());
            assertEquals(root.resolve("d/d").toString() + "/", asDirectory.get(0));
            assertEquals(root.resolve("d/f").toString(), asDirectory.get(1));
        }

        @Test
        void listsOnlyEntriesWhoseNameStartsWithDot(@TempDir Path tmp) throws IOException {
            // list(absDir + "/."): equivalent to list(".") — only entries whose name starts with "." pass
            Path root = buildTree(tmp.resolve("root"));

            List<String> result = fileOps.list(root.toString() + "/.");

            // only .gitignore starts with "."
            assertEquals(1, result.size());
            assertEquals(root.resolve(".gitignore").toString(), result.get(0));
        }
    }

    @Nested
    class listAll {

        @Test
        void listsAllFilesRecursively(@TempDir Path tmp) throws IOException {
            Path root = buildTree(tmp.resolve("root"));

            List<String> result = fileOps.listAll(root.resolve("docs").toString());

            assertEquals(2, result.size());
            assertTrue(result.contains(root.resolve("docs/img/a.png").toString()));
            assertTrue(result.contains(root.resolve("docs/readme.md").toString()));
        }

        @Test
        void returnsFileItselfWhenPrefixIsAFile(@TempDir Path tmp) throws IOException {
            Path root = buildTree(tmp.resolve("root"));

            List<String> result = fileOps.listAll(root.resolve(".gitignore").toString());

            assertEquals(1, result.size());
            assertEquals(root.resolve(".gitignore").toString(), result.get(0));
        }

        @Test
        void returnsEmptyForNonExistentPrefix(@TempDir Path tmp) throws IOException {
            Path root = buildTree(tmp.resolve("root"));

            List<String> result = fileOps.listAll(root.resolve("nope").toString());

            assertTrue(result.isEmpty());
        }

        @Test
        void returnsEmptyForEmptyDirectory(@TempDir Path tmp) throws IOException {
            Path root = tmp.resolve("root");
            mkdir(root);

            List<String> result = fileOps.listAll(root.toString());

            assertTrue(result.isEmpty());
        }

        @Test
        void returnsEmptyForDirectoryWithOnlySubdirectories(@TempDir Path tmp) throws IOException {
            // listAll only returns files; a directory with only subdirectories yields empty
            Path root = tmp.resolve("root");
            mkdir(root.resolve("a"));
            mkdir(root.resolve("b"));

            List<String> result = fileOps.listAll(root.toString());

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class listDirectoryWithDepth {

        @Test
        void listsOneLevelDeep(@TempDir Path tmp) throws IOException {
            Path root = buildTree(tmp.resolve("root"));

            List<String> result = fileOps.listDirectory(root.resolve("docs").toString(), 1, null, null);

            assertEquals(2, result.size());
            assertEquals(root.resolve("docs/img").toString() + "/", result.get(0));
            assertEquals(root.resolve("docs/readme.md").toString(), result.get(1));
        }

        @Test
        void listsTwoLevelsDeep(@TempDir Path tmp) throws IOException {
            Path root = buildTree(tmp.resolve("root"));

            List<String> result = fileOps.listDirectory(root.resolve("docs").toString(), 2, null, null);

            assertEquals(3, result.size());
            assertEquals(root.resolve("docs/img").toString() + "/", result.get(0));
            assertEquals(root.resolve("docs/img/a.png").toString(), result.get(1));
            assertEquals(root.resolve("docs/readme.md").toString(), result.get(2));
        }

        @Test
        void returnsEmptyForEmptyDirectory(@TempDir Path tmp) throws IOException {
            Path root = buildTree(tmp.resolve("root"));

            List<String> result = fileOps.listDirectory(root.resolve("empty").toString(), 1, null, null);

            assertTrue(result.isEmpty());
        }

        @Test
        void returnsEmptyWhenDepthIsZero(@TempDir Path tmp) throws IOException {
            Path root = buildTree(tmp.resolve("root"));

            List<String> result = fileOps.listDirectory(root.resolve("docs").toString(), 0, null, null);

            assertTrue(result.isEmpty());
        }

        @Test
        void returnsNullWhenDirectoryDoesNotExist(@TempDir Path tmp) {
            List<String> result = fileOps.listDirectory(tmp.resolve("nope").toString(), 1, null, null);

            assertNull(result);
        }

        @Test
        void returnsNullWhenAbortAlreadyRequested(@TempDir Path tmp) throws IOException {
            Path root = buildTree(tmp.resolve("root"));

            List<String> result = fileOps.listDirectory(root.resolve("docs").toString(), 1, new AtomicBoolean(true), null);

            assertNull(result);
        }

        @Test
        void throwsWhenDirectoryIsEmpty() {
            // empty string is rejected; callers must pass "." for the current directory
            assertThrows(IllegalArgumentException.class, () -> fileOps.listDirectory("", 1, null, null));
        }

        @Test
        void listsDirectoriesBeforeFiles(@TempDir Path tmp) throws IOException {
            // dir-first: b/ (and its child) before a.txt and c.txt
            Path root = tmp.resolve("root");
            mkdir(root.resolve("d/b"));
            writeFile(root.resolve("d/a.txt"), "a");
            writeFile(root.resolve("d/b/c.txt"), "c");
            writeFile(root.resolve("d/c.txt"), "c");

            List<String> result = fileOps.listDirectory(root.resolve("d").toString(), 2, null, null);

            assertEquals(4, result.size());
            assertEquals(root.resolve("d/b").toString() + "/", result.get(0));
            assertEquals(root.resolve("d/b/c.txt").toString(), result.get(1));
            assertEquals(root.resolve("d/a.txt").toString(), result.get(2));
            assertEquals(root.resolve("d/c.txt").toString(), result.get(3));
        }
    }

    @Nested
    class copyDirectory {

        @Test
        void copiesSingleFile(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve(".gitignore");
            writeFile(src, "git");
            Path dst = tmp.resolve(".gitignore.copy");

            assertTrue(fileOps.copyDirectory(src.toString(), dst.toString(), null, null));
            assertArrayEquals("git".getBytes(), Files.readAllBytes(dst));
        }

        @Test
        void copiesDirectoryTree(@TempDir Path tmp) throws IOException {
            Path src = buildTree(tmp.resolve("root"));
            Path dst = tmp.resolve("root.copy");

            assertTrue(fileOps.copyDirectory(src.toString(), dst.toString(), null, null));

            assertArrayEquals("readme".getBytes(), Files.readAllBytes(dst.resolve("docs/readme.md")));
            assertArrayEquals("png".getBytes(), Files.readAllBytes(dst.resolve("docs/img/a.png")));
            assertArrayEquals("main".getBytes(), Files.readAllBytes(dst.resolve("src/Main.java")));
            assertTrue(Files.isDirectory(dst.resolve("empty")));
        }

        @Test
        void copiesEmptyDirectory(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("empty");
            mkdir(src);
            Path dst = tmp.resolve("empty.copy");

            assertTrue(fileOps.copyDirectory(src.toString(), dst.toString(), null, null));
            assertTrue(Files.isDirectory(dst));
            assertFalse(Files.list(dst).findAny().isPresent());
        }

        @Test
        void returnsFalseWhenSrcDoesNotExist(@TempDir Path tmp) {
            Path dst = tmp.resolve("dst");

            assertFalse(fileOps.copyDirectory(tmp.resolve("nope").toString(), dst.toString(), null, null));
            assertFalse(Files.exists(dst));
        }

        @Test
        void returnsFalseWhenDstAlreadyExists(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("a");
            writeFile(src, "x");
            Path dst = tmp.resolve("b");
            Files.createFile(dst);

            assertFalse(fileOps.copyDirectory(src.toString(), dst.toString(), null, null));
        }

        @Test
        void returnsFalseWhenAbortAlreadyRequested(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("a");
            writeFile(src, "x");
            Path dst = tmp.resolve("b");

            assertFalse(fileOps.copyDirectory(src.toString(), dst.toString(), new AtomicBoolean(true), null));
            assertFalse(Files.exists(dst));
        }

        @Test
        void returnsFalseWhenDstParentDoesNotExist(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("d");
            mkdir(src);
            writeFile(src.resolve("f"), "f");
            // dst's parent does not exist; Files.createDirectory(dst) throws -> false
            Path dst = tmp.resolve("missing").resolve("dst");

            assertFalse(fileOps.copyDirectory(src.toString(), dst.toString(), null, null));
        }

        @Test
        void throwsWhenSrcIsEmpty(@TempDir Path tmp) throws IOException {
            Path dst = tmp.resolve("b");
            assertThrows(IllegalArgumentException.class, () -> fileOps.copyDirectory("", dst.toString(), null, null));
        }

        @Test
        void throwsWhenDstIsEmpty(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("a");
            writeFile(src, "x");
            assertThrows(IllegalArgumentException.class, () -> fileOps.copyDirectory(src.toString(), "", null, null));
        }

        @Test
        void copiesSymbolicLinkAsLink(@TempDir Path tmp) throws IOException {
            // a symlink is copied as a symlink (the node itself), not as a copy of its target
            Path dir = tmp.resolve("dir");
            mkdir(dir);
            writeFile(dir.resolve("f"), "f");
            Path link = tmp.resolve("link");
            Assumptions.assumeTrue(createSymbolicLink(link, dir));
            Path dst = tmp.resolve("link.copy");

            assertTrue(fileOps.copyDirectory(link.toString(), dst.toString(), null, null));
            assertTrue(Files.isSymbolicLink(dst));
            // the link resolves to the original target, so f is reachable through the copied link
            assertTrue(Files.exists(dst.resolve("f")));
        }

        @Test
        void copiesSymlinkChildAsLinkWithinTree(@TempDir Path tmp) throws IOException {
            // when copying a tree, a symlink-to-directory child is copied as a link, not followed
            Path src = tmp.resolve("root");
            mkdir(src);
            Path dir = tmp.resolve("dir");
            mkdir(dir);
            writeFile(dir.resolve("f"), "f");
            Path sl = src.resolve("sl");
            Assumptions.assumeTrue(createSymbolicLink(sl, dir));
            Path dst = tmp.resolve("root.copy");

            assertTrue(fileOps.copyDirectory(src.toString(), dst.toString(), null, null));
            // the child was copied as a symlink, not as a real directory
            assertTrue(Files.isSymbolicLink(dst.resolve("sl")));
            assertFalse(Files.isDirectory(dst.resolve("sl"), LinkOption.NOFOLLOW_LINKS));
        }
    }

    @Nested
    class copyFile {

        @Test
        void copiesFileContent(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("a");
            writeFile(src, "hello");
            Path dst = tmp.resolve("a.copy");

            assertTrue(fileOps.copyFile(src.toString(), dst.toString(), null));
            assertArrayEquals("hello".getBytes(), Files.readAllBytes(dst));
        }

        @Test
        void copiesEmptyFile(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("empty");
            writeFile(src, "");
            Path dst = tmp.resolve("empty.copy");

            assertTrue(fileOps.copyFile(src.toString(), dst.toString(), null));
            assertEquals(0, Files.size(dst));
        }

        @Test
        void followsSymbolicLinkAndCopiesTargetContent(@TempDir Path tmp) throws IOException {
            // a symlink to a regular file is followed: the target's content is copied,
            // and the destination is a regular file (not a symlink)
            Path target = tmp.resolve("target");
            writeFile(target, "content");
            Path link = tmp.resolve("link");
            Assumptions.assumeTrue(createSymbolicLink(link, target));
            Path dst = tmp.resolve("link.copy");

            assertTrue(fileOps.copyFile(link.toString(), dst.toString(), null));
            assertFalse(Files.isSymbolicLink(dst));
            assertArrayEquals("content".getBytes(), Files.readAllBytes(dst));
        }

        @Test
        void returnsFalseForSymlinkToDirectory(@TempDir Path tmp) throws IOException {
            // a symlink to a directory is not a regular file; copyFile rejects it
            Path dir = tmp.resolve("dir");
            mkdir(dir);
            Path link = tmp.resolve("link");
            Assumptions.assumeTrue(createSymbolicLink(link, dir));
            Path dst = tmp.resolve("link.copy");

            assertFalse(fileOps.copyFile(link.toString(), dst.toString(), null));
            assertFalse(Files.exists(dst));
        }

        @Test
        void returnsFalseForDirectory(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("d");
            mkdir(d);
            Path dst = tmp.resolve("d.copy");

            assertFalse(fileOps.copyFile(d.toString(), dst.toString(), null));
            assertFalse(Files.exists(dst));
        }

        @Test
        void returnsFalseWhenSrcDoesNotExist(@TempDir Path tmp) {
            Path dst = tmp.resolve("dst");

            assertFalse(fileOps.copyFile(tmp.resolve("nope").toString(), dst.toString(), null));
            assertFalse(Files.exists(dst));
        }

        @Test
        void returnsFalseWhenDstAlreadyExists(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("a");
            writeFile(src, "x");
            Path dst = tmp.resolve("b");
            Files.createFile(dst);

            assertFalse(fileOps.copyFile(src.toString(), dst.toString(), null));
        }

        @Test
        void returnsFalseWhenAbortAlreadyRequested(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("a");
            writeFile(src, "x");
            Path dst = tmp.resolve("b");

            assertFalse(fileOps.copyFile(src.toString(), dst.toString(), new AtomicBoolean(true)));
            assertFalse(Files.exists(dst));
        }

        @Test
        void deletesPartialFileWhenAbortedMidCopy(@TempDir Path tmp) throws Exception {
            // a large source spans multiple read chunks; aborting mid-copy must leave no dst behind
            Path src = tmp.resolve("big");
            byte[] data = new byte[1_000_000];
            Files.write(src, data);
            Path dst = tmp.resolve("big.copy");
            AtomicBoolean abort = new AtomicBoolean(false);

            // flip the flag shortly after copying starts, so the in-loop check hits it
            Thread flipper = new Thread(() -> {
                try { Thread.sleep(1); } catch (InterruptedException ignored) { }
                abort.set(true);
            });
            flipper.start();

            assertFalse(fileOps.copyFile(src.toString(), dst.toString(), abort));
            flipper.join();
            assertFalse(Files.exists(dst));
        }

        @Test
        void throwsWhenSrcIsEmpty(@TempDir Path tmp) {
            assertThrows(IllegalArgumentException.class, () -> fileOps.copyFile("", "dst", null));
        }

        @Test
        void throwsWhenDstIsEmpty(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("a");
            writeFile(src, "x");
            assertThrows(IllegalArgumentException.class, () -> fileOps.copyFile(src.toString(), "", null));
        }
    }

    @Nested
    class moveFile {

        @Test
        void movesRegularFile(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("a.txt");
            writeFile(src, "hello");
            Path dst = tmp.resolve("b.txt");

            assertTrue(fileOps.moveFile(src.toString(), dst.toString(), null));
            assertFalse(Files.exists(src));
            assertTrue(Files.exists(dst));
            assertArrayEquals("hello".getBytes(), Files.readAllBytes(dst));
        }

        @Test
        void movesSymlinkToFile(@TempDir Path tmp) throws IOException {
            Path target = tmp.resolve("target");
            writeFile(target, "content");
            Path link = tmp.resolve("link");
            Assumptions.assumeTrue(createSymbolicLink(link, target));
            Path dst = tmp.resolve("link.moved");

            assertTrue(fileOps.moveFile(link.toString(), dst.toString(), null));
            assertFalse(Files.exists(link));
            assertTrue(Files.isSymbolicLink(dst));
            assertEquals(target.toString(), Files.readSymbolicLink(dst).toString());
        }

        @Test
        void returnsFalseWhenSrcDoesNotExist(@TempDir Path tmp) {
            assertFalse(fileOps.moveFile(tmp.resolve("nope").toString(), tmp.resolve("dst").toString(), null));
        }

        @Test
        void returnsFalseWhenSrcIsDirectory(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("d");
            mkdir(src);

            assertFalse(fileOps.moveFile(src.toString(), tmp.resolve("dst").toString(), null));
        }

        @Test
        void returnsFalseWhenDstAlreadyExists(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("a");
            writeFile(src, "x");
            Path dst = tmp.resolve("b");
            writeFile(dst, "y");

            assertFalse(fileOps.moveFile(src.toString(), dst.toString(), null));
            assertTrue(Files.exists(src));
        }

        @Test
        void returnsFalseWhenAbortRequested(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("a");
            writeFile(src, "x");

            assertFalse(fileOps.moveFile(src.toString(), tmp.resolve("b").toString(), new AtomicBoolean(true)));
            assertTrue(Files.exists(src));
        }

        @Test
        void throwsWhenSrcIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> fileOps.moveFile("", "dst", null));
        }

        @Test
        void throwsWhenDstIsEmpty(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("a");
            writeFile(src, "x");
            assertThrows(IllegalArgumentException.class, () -> fileOps.moveFile(src.toString(), "", null));
        }
    }

    @Nested
    class moveDirectory {

        @Test
        void movesEmptyDirectory(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("d");
            mkdir(src);
            Path dst = tmp.resolve("d.moved");

            assertTrue(fileOps.moveDirectory(src.toString(), dst.toString(), null, null, null, null));
            assertFalse(Files.exists(src));
            assertTrue(Files.isDirectory(dst));
        }

        @Test
        void movesDirectoryTree(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("root");
            mkdir(src.resolve("sub"));
            writeFile(src.resolve("f"), "f");
            writeFile(src.resolve("sub/g"), "g");
            Path dst = tmp.resolve("root.moved");

            assertTrue(fileOps.moveDirectory(src.toString(), dst.toString(), null, null, null, null));
            assertFalse(Files.exists(src));
            assertTrue(Files.isDirectory(dst));
            assertArrayEquals("f".getBytes(), Files.readAllBytes(dst.resolve("f")));
            assertArrayEquals("g".getBytes(), Files.readAllBytes(dst.resolve("sub/g")));
        }

        @Test
        void returnsFalseWhenSrcDoesNotExist(@TempDir Path tmp) {
            assertFalse(fileOps.moveDirectory(tmp.resolve("nope").toString(), tmp.resolve("dst").toString(), null, null, null, null));
        }

        @Test
        void returnsFalseWhenSrcIsAFile(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("f");
            writeFile(src, "x");

            assertFalse(fileOps.moveDirectory(src.toString(), tmp.resolve("dst").toString(), null, null, null, null));
        }

        @Test
        void returnsFalseWhenDstAlreadyExists(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("d");
            mkdir(src);
            Path dst = tmp.resolve("existing");
            mkdir(dst);

            assertFalse(fileOps.moveDirectory(src.toString(), dst.toString(), null, null, null, null));
            assertTrue(Files.exists(src));
        }

        @Test
        void returnsFalseWhenAbortRequested(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("d");
            mkdir(src);

            assertFalse(fileOps.moveDirectory(src.toString(), tmp.resolve("dst").toString(), new AtomicBoolean(true), null, null, null));
            assertTrue(Files.exists(src));
        }

        @Test
        void movesSymlinkToDirectory(@TempDir Path tmp) throws IOException {
            Path target = tmp.resolve("target");
            mkdir(target);
            writeFile(target.resolve("f"), "f");
            Path link = tmp.resolve("link");
            Assumptions.assumeTrue(createSymbolicLink(link, target));
            Path dst = tmp.resolve("link.moved");

            assertTrue(fileOps.moveDirectory(link.toString(), dst.toString(), null, null, null, null));
            assertFalse(Files.exists(link, LinkOption.NOFOLLOW_LINKS));
            assertTrue(Files.isSymbolicLink(dst));
            assertTrue(Files.exists(dst.resolve("f")));
        }

        @Test
        void onMovedCalledForAtomicRename(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("d");
            mkdir(src);
            Path dst = tmp.resolve("d.moved");

            List<String> moved = new java.util.ArrayList<>();
            FileOps.OnMovedListener onMoved = (s, d) -> moved.add(s + " -> " + d);

            assertTrue(fileOps.moveDirectory(src.toString(), dst.toString(), null, null, null, onMoved));
            assertEquals(1, moved.size());
            assertEquals(src.toString() + " -> " + dst.toString(), moved.get(0));
        }

        @Test
        void throwsWhenSrcIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> fileOps.moveDirectory("", "dst", null, null, null, null));
        }

        @Test
        void throwsWhenDstIsEmpty(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("d");
            mkdir(src);
            assertThrows(IllegalArgumentException.class, () -> fileOps.moveDirectory(src.toString(), "", null, null, null, null));
        }
    }

    @Nested
    class loadString {

        @Test
        void loadsStringFromFile(@TempDir Path tmp) throws IOException {
            Path f = tmp.resolve("f.txt");
            Files.write(f, "hello".getBytes(StandardCharsets.UTF_8));

            assertEquals("hello", fileOps.loadString(f.toString()));
        }

        @Test
        void returnsNullWhenFileDoesNotExist(@TempDir Path tmp) {
            assertNull(fileOps.loadString(tmp.resolve("nope").toString()));
        }

        @Test
        void throwsWhenPathIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> fileOps.loadString(""));
        }
    }

    @Nested
    class saveString {

        @Test
        void savesStringToFile(@TempDir Path tmp) throws IOException {
            Path f = tmp.resolve("f.txt");

            assertTrue(fileOps.saveString(f.toString(), "hello"));
            assertArrayEquals("hello".getBytes(), Files.readAllBytes(f));
        }

        @Test
        void returnsFalseForDirectory(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("d");
            mkdir(d);

            assertFalse(fileOps.saveString(d.toString(), "x"));
        }

        @Test
        void throwsWhenPathIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> fileOps.saveString("", "x"));
        }
    }

    // ---- relative-path integration tests ----

    @Nested
    class relativePath {

        // relative paths resolve against the JVM cwd (the project root). We stage data under
        // `target/fileops-<id>/` (a relative path guaranteed to exist under cwd) and clean up.

        private String uniqueRel() {
            return "target/fileops-" + Thread.currentThread().getName() + "-" + System.nanoTime();
        }

        private void rm(Path p) throws IOException {
            if (Files.isDirectory(p)) {
                try (Stream<Path> s = Files.list(p)) {
                    for (Path c : (Iterable<Path>) s::iterator) {
                        rm(c);
                    }
                }
            }
            Files.deleteIfExists(p);
        }

        @Test
        void listDirectoryPrefix() throws IOException {
            String rel = uniqueRel();
            Path root = Paths.get(rel);
            try {
                Files.createDirectories(root.resolve("d/d"));
                Files.write(root.resolve("d/f"), "f".getBytes());

                // prefix "d" matches the directory itself
                List<String> result = fileOps.list(rel + "/d");
                assertEquals(1, result.size());
                assertEquals(rel + "/d/", result.get(0));
            } finally {
                rm(root);
            }
        }

        @Test
        void listDirectoryWithTrailingSlash() throws IOException {
            String rel = uniqueRel();
            Path root = Paths.get(rel);
            try {
                Files.createDirectories(root.resolve("d/d"));
                Files.write(root.resolve("d/f"), "f".getBytes());

                List<String> result = fileOps.list(rel + "/d/");
                assertEquals(2, result.size());
                assertEquals(rel + "/d/d/", result.get(0));
                assertEquals(rel + "/d/f", result.get(1));
            } finally {
                rm(root);
            }
        }

        @Test
        void listFilePrefix() throws IOException {
            String rel = uniqueRel();
            Path root = Paths.get(rel);
            try {
                Files.createDirectories(root);
                Files.write(root.resolve("f"), "f".getBytes());

                List<String> result = fileOps.list(rel + "/f");
                assertEquals(1, result.size());
                assertEquals(rel + "/f", result.get(0));
            } finally {
                rm(root);
            }
        }

        @Test
        void listPrefixMatchesMultiple() throws IOException {
            String rel = uniqueRel();
            Path root = Paths.get(rel);
            try {
                Files.createDirectories(root.resolve("lo"));
                Files.createDirectories(root.resolve("lower"));
                Files.write(root.resolve("long"), "long".getBytes());

                List<String> result = fileOps.list(rel + "/lo");
                assertEquals(3, result.size());
                assertEquals(rel + "/lo/", result.get(0));
                assertEquals(rel + "/long", result.get(1));
                assertEquals(rel + "/lower/", result.get(2));
            } finally {
                rm(root);
            }
        }

        @Test
        void listDotSlashReturnsEmpty() {
            // list("./"): pathToString strips "./" prefix from each entry,
            // so filter startsWith("./") never matches → empty result
            List<String> result = fileOps.list("./");
            assertTrue(result.isEmpty());
        }

        @Test
        void listDotDotReturnsParentEntries() {
            // list(".."): lists parent of cwd; all entries start with "../" so all pass filter
            List<String> result = fileOps.list("..");
            assertFalse(result.isEmpty());
            for (String s : result) {
                assertTrue(s.startsWith("../"), String.format("E: entry `%s` should start with ../", s));
            }
        }

        @Test
        void listRelativePathWithDotSuffix() throws IOException {
            // list(rel + "/."): only entries whose name starts with "." pass the filter
            String rel = uniqueRel();
            Path root = Paths.get(rel);
            try {
                mkdir(root.resolve(".hiddenDir"));
                writeFile(root.resolve(".hiddenFile"), "x");
                writeFile(root.resolve("visible"), "y");

                List<String> result = fileOps.list(rel + "/.");

                assertEquals(2, result.size());
                assertTrue(result.contains(rel + "/.hiddenDir/"));
                assertTrue(result.contains(rel + "/.hiddenFile"));
            } finally {
                rm(root);
            }
        }

        @Test
        void listAllReturnsAllFiles() throws IOException {
            String rel = uniqueRel();
            Path root = Paths.get(rel);
            try {
                Files.createDirectories(root.resolve("d/d"));
                Files.write(root.resolve("d/f"), "f".getBytes());
                Files.write(root.resolve("top"), "t".getBytes());

                List<String> result = fileOps.listAll(rel);
                assertEquals(2, result.size());
                assertTrue(result.contains(rel + "/d/f"));
                assertTrue(result.contains(rel + "/top"));
            } finally {
                rm(root);
            }
        }

        @Test
        void copyDirectoryCopiesTree() throws IOException {
            String rel = uniqueRel();
            Path root = Paths.get(rel);
            try {
                Files.createDirectories(root.resolve("d"));
                Files.write(root.resolve("d/f"), "f".getBytes());
                Files.write(root.resolve("top"), "t".getBytes());

                assertTrue(fileOps.copyDirectory(rel, rel + ".copy", null, null));
                assertTrue(Files.exists(Paths.get(rel + ".copy/d/f")));
                assertTrue(Files.exists(Paths.get(rel + ".copy/top")));
                assertArrayEquals("f".getBytes(), Files.readAllBytes(Paths.get(rel + ".copy/d/f")));
            } finally {
                rm(root);
                rm(Paths.get(rel + ".copy"));
            }
        }

        @Test
        void removeDirectoryRemovesTree() throws IOException {
            String rel = uniqueRel();
            Path root = Paths.get(rel);
            try {
                Files.createDirectories(root.resolve("d"));
                Files.write(root.resolve("d/f"), "f".getBytes());

                assertTrue(fileOps.removeDirectory(rel, true, false, null, null));
                assertFalse(Files.exists(root));
            } finally {
                rm(root);
            }
        }
    }
}
