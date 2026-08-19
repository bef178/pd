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

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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

    @Nested
    class list {

        @Test
        public void list_baseline() {
            List<String> a = FileOps.singleton.list("");
            assertTrue(a.contains("src/"));
            assertTrue(a.contains("pom.xml"));

            a = FileOps.singleton.list("pom");
            assertEquals(1, a.size());
            assertTrue(a.contains("pom.xml"));

            a = FileOps.singleton.list("pom.xml");
            assertTrue(a.isEmpty());

            a = FileOps.singleton.list("src");
            assertEquals(1, a.size());
            assertTrue(a.contains("src/"));

            a = FileOps.singleton.list("src/");
            assertArrayEquals(new String[] {"src/java/", "src/java-test/"}, a.toArray());

            a = FileOps.singleton.list("src", 4);
            assertTrue(a.contains("src/java/pd/time/"));
            assertTrue(a.contains("src/java/pd/util/"));
        }

        @Test
        public void list_documented(@TempDir Path root) throws IOException {
            Files.createDirectory(root.resolve("d"));
            Files.createDirectory(root.resolve("d/d"));
            Files.write(root.resolve("d/f"), new byte[0]);
            Files.createDirectory(root.resolve("lo"));
            Files.createDirectory(root.resolve("lower"));
            Files.write(root.resolve("long"), new byte[0]);
            Files.createDirectory(root.resolve(".git"));
            Files.write(root.resolve(".gitignore"), new byte[0]);
            Files.write(root.resolve("...a"), new byte[0]);

            String s = root + "/";
            assertArrayEquals(new String[] {s + "d/"}, FileOps.singleton.list(s + "d").toArray());
            assertArrayEquals(new String[] {s + "d/d/", s + "d/f"}, FileOps.singleton.list(s + "d/").toArray());
            assertTrue(FileOps.singleton.list(s + "d/f").isEmpty());
            assertArrayEquals(new String[] {s + "lo/", s + "lower/", s + "long"}, FileOps.singleton.list(s + "lo").toArray());
            assertArrayEquals(new String[] {s + ".git/", s + "...a", s + ".gitignore"}, FileOps.singleton.list(s + ".").toArray());
            assertArrayEquals(new String[] {s + "...a"}, FileOps.singleton.list(s + "..").toArray());
            assertFalse(FileOps.singleton.list(s + "../").isEmpty());
            assertArrayEquals(new String[] {s + ".git/", s + "d/d/", s + "d/f", s + "lo/", s + "lower/", s + "...a", s + ".gitignore", s + "long"}, FileOps.singleton.list(s, 999).toArray());
        }

        @Test
        public void list_returnEmptyForEmptyDirectory(@TempDir Path root) {
            assertTrue(FileOps.singleton.list(root + "/").isEmpty());
        }

        @Test
        public void list_returnNullWhenPrefixMatchesNothing(@TempDir Path root) {
            assertNull(FileOps.singleton.list(root.resolve("a").toString()));
        }

        @Test
        public void list_returnEmptyForBrokenSymbolicLink(@TempDir Path root) throws IOException {
            Path symlink = root.resolve("symlink");
            Files.createSymbolicLink(symlink, root.resolve("missing"));

            assertTrue(FileOps.singleton.list(symlink.toString(), 2).isEmpty());
        }

        @Test
        void list_abortsBeforeTraversingWhenRequested(@TempDir Path root) throws IOException {
            Path dir = root.resolve("dir");
            mkdir(dir);
            writeFile(dir.resolve("child.txt"), "x");

            assertNull(fileOps.listDirectory(dir.toString(), 3, new AtomicBoolean(true), null));
            assertTrue(Files.exists(dir.resolve("child.txt")));
        }

        @Test
        void list_returnEmptyForDot() {
            assertTrue(FileOps.singleton.list(".").isEmpty());
            assertTrue(FileOps.singleton.list("./").contains("./src/"));
        }

        @Test
        void list_returnEmptyForDotDot() {
            assertTrue(FileOps.singleton.list("..").isEmpty());
            assertTrue(FileOps.singleton.list("../").contains("../pd/"));
        }
    }

    private static void writeFile(Path p, String content) throws IOException {
        Files.createDirectories(p.getParent());
        Files.write(p, content.getBytes());
    }

    private static void mkdir(Path p) throws IOException {
        Files.createDirectories(p);
    }

    // returns false (so the caller can skip) if symbolic links are not supported here
    private static boolean createSymbolicLink(Path link, Path target) {
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
    class createDirectory {

        @Test
        void createsSingleLevelWhenParentsFalse(@TempDir Path tmp) {
            assertTrue(fileOps.createDirectory(tmp.resolve("d").toString(), false, null, null));
            assertTrue(Files.isDirectory(tmp.resolve("d")));
        }

        @Test
        void returnsFalseWhenAlreadyExists(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("d");
            mkdir(d);

            assertFalse(fileOps.createDirectory(d.toString(), false, null, null));
        }

        @Test
        void returnsFalseWhenParentMissingAndParentsFalse(@TempDir Path tmp) {
            assertFalse(fileOps.createDirectory(tmp.resolve("missing/d").toString(), false, null, null));
            assertFalse(Files.exists(tmp.resolve("missing")));
        }

        @Test
        void createsIntermediateParentsWhenParentsTrue(@TempDir Path tmp) {
            assertTrue(fileOps.createDirectory(tmp.resolve("a/b/c").toString(), true, null, null));
            assertTrue(Files.isDirectory(tmp.resolve("a/b/c")));
        }

        @Test
        void isIdempotentWhenAlreadyExistsAndParentsTrue(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("d");
            mkdir(d);

            assertFalse(fileOps.createDirectory(d.toString(), true, null, null));
        }

        @Test
        void returnsFalseWhenPathIsAnExistingFile(@TempDir Path tmp) throws IOException {
            Path f = tmp.resolve("f");
            writeFile(f, "x");

            assertFalse(fileOps.createDirectory(f.toString(), false, null, null));
            assertTrue(Files.isRegularFile(f));
        }

        @Test
        void throwsWhenPathIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> fileOps.createDirectory("", false, null, null));
        }
    }

    @Nested
    class deleteDirectory {

        @Test
        void deletesEmptyDirectoryWhenNotRecursive(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("d");
            mkdir(d);

            assertTrue(fileOps.deleteDirectory(d.toString(), false, false, null, null));
            assertFalse(Files.exists(d));
        }

        @Test
        void returnsFalseForNonEmptyWhenNotRecursive(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("d");
            mkdir(d);
            writeFile(d.resolve("f"), "f");

            assertFalse(fileOps.deleteDirectory(d.toString(), false, false, null, null));
            assertTrue(Files.exists(d));
            assertTrue(Files.exists(d.resolve("f")));
        }

        @Test
        void deletesTreeWhenRecursive(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("d");
            mkdir(d.resolve("sub"));
            writeFile(d.resolve("f"), "f");
            writeFile(d.resolve("sub/g"), "g");

            assertTrue(fileOps.deleteDirectory(d.toString(), true, false, null, null));
            assertFalse(Files.exists(d));
        }

        @Test
        void returnsFalseWhenTargetDoesNotExist(@TempDir Path tmp) {
            assertFalse(fileOps.deleteDirectory(tmp.resolve("nope").toString(), false, false, null, null));
        }

        @Test
        void deletesEmptyAncestorsWhenParentsTrue(@TempDir Path tmp) throws IOException {
            Path leaf = tmp.resolve("p/q/r");
            mkdir(leaf);

            assertTrue(fileOps.deleteDirectory(leaf.toString(), false, true, null, null));

            assertFalse(Files.exists(tmp.resolve("p")));
        }

        @Test
        void stopsAtNonEmptyAncestorWhenParentsTrue(@TempDir Path tmp) throws IOException {
            // p holds a file, so it must survive removing the empty p/q/r chain
            Path leaf = tmp.resolve("p/q/r");
            mkdir(leaf);
            writeFile(tmp.resolve("p/keep"), "k");

            assertTrue(fileOps.deleteDirectory(leaf.toString(), false, true, null, null));
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

            assertFalse(fileOps.deleteDirectory(d.toString(), true, false, new AtomicBoolean(true), null));
            // aborted before any deletion: the tree is intact
            assertTrue(Files.exists(d));
            assertTrue(Files.exists(d.resolve("f")));
            assertTrue(Files.exists(d.resolve("sub/g")));
        }

        @Test
        void returnsFalseAndKeepsEmptyDirectoryWhenAbortRequestedBeforeStart(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("d");
            mkdir(d);

            assertFalse(fileOps.deleteDirectory(d.toString(), true, false, new AtomicBoolean(true), null));
            assertTrue(Files.exists(d));
        }

        @Test
        void abortStopsDeleteBeforeAnyDeletionStarts(@TempDir Path tmp) throws IOException {
            Path leaf = tmp.resolve("p/q/r");
            mkdir(leaf);
            AtomicBoolean abort = new AtomicBoolean(true);

            assertFalse(fileOps.deleteDirectory(leaf.toString(), false, true, abort, null));
            assertTrue(Files.exists(leaf));
            assertTrue(Files.exists(tmp.resolve("p/q")));
            assertTrue(Files.exists(tmp.resolve("p")));
        }

        @Test
        void returnsFalseForSymlinkToDirectory(@TempDir Path tmp) throws IOException {
            // removeDirectory rejects a symlink-to-directory; the target is left untouched
            Path dir = tmp.resolve("dir");
            mkdir(dir.resolve("sub"));
            writeFile(dir.resolve("f"), "f");
            Path link = tmp.resolve("link");
            Assumptions.assumeTrue(createSymbolicLink(link, dir));

            assertFalse(fileOps.deleteDirectory(link.toString(), true, false, null, null));
            assertTrue(Files.exists(link, LinkOption.NOFOLLOW_LINKS));
            assertTrue(Files.exists(dir));
            assertTrue(Files.exists(dir.resolve("f")));
            assertTrue(Files.exists(dir.resolve("sub")));
        }

        @Test
        void deletesSymlinkChildItselfNotTargetWhenRecursive(@TempDir Path tmp) throws IOException {
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

            assertTrue(fileOps.deleteDirectory(parent.toString(), true, false, null, null));
            assertFalse(Files.exists(parent));
            // the external target directory and its content survive (not followed)
            assertTrue(Files.exists(ext));
            assertTrue(Files.exists(ext.resolve("e")));
        }

        @Test
        void throwsWhenPathIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> fileOps.deleteDirectory("", false, false, null, null));
        }

        @Test
        void listenerNotifiedForEachRemovedEntry(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("d");
            mkdir(d.resolve("sub"));
            writeFile(d.resolve("f"), "f");
            writeFile(d.resolve("sub/g"), "g");

            List<String> removed = new java.util.ArrayList<>();
            FileOps.OnActionListener listener = (action, from, to, succeeded) -> {
                if (action == FileOps.Action.DELETE && succeeded != null) {
                    removed.add(from);
                }
            };

            assertTrue(fileOps.deleteDirectory(d.toString(), true, false, null, listener));
            assertEquals(4, removed.size());
            assertEquals(d.resolve("sub/g").toString(), removed.get(0));
            assertEquals(d.resolve("sub").toString(), removed.get(1));
            assertEquals(d.resolve("f").toString(), removed.get(2));
            assertEquals(d.toString(), removed.get(3));
            assertFalse(Files.exists(d));
        }

        @Test
        void listenerNotifiedForAncestorsWhenParentsTrue(@TempDir Path tmp) throws IOException {
            Path leaf = tmp.resolve("p/q/r");
            mkdir(leaf);

            List<String> removed = new java.util.ArrayList<>();
            FileOps.OnActionListener listener = (action, from, to, succeeded) -> {
                if (action == FileOps.Action.DELETE && succeeded != null) {
                    removed.add(from);
                }
            };

            assertTrue(fileOps.deleteDirectory(leaf.toString(), false, true, null, listener));
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
            FileOps.OnActionListener listener = (action, from, to, succeeded) -> {
                if (action == FileOps.Action.DELETE && succeeded != null) {
                    removed.add(from);
                }
            };

            assertFalse(fileOps.deleteDirectory(d.toString(), true, false, new AtomicBoolean(true), listener));
            assertTrue(removed.isEmpty());
        }
    }

    @Nested
    class deleteFile {

        @Test
        void deletesExistingFile(@TempDir Path tmp) throws IOException {
            Path f = tmp.resolve("a.txt");
            writeFile(f, "x");

            assertTrue(fileOps.deleteFile(f.toString(), null));
            assertFalse(Files.exists(f));
        }

        @Test
        void returnsFalseWhenFileDoesNotExist(@TempDir Path tmp) {
            assertFalse(fileOps.deleteFile(tmp.resolve("nope").toString(), null));
        }

        @Test
        void returnsFalseForDirectory(@TempDir Path tmp) throws IOException {
            // removeFile only deletes files; a directory (empty or not) is left untouched
            Path empty = tmp.resolve("empty");
            mkdir(empty);
            assertFalse(fileOps.deleteFile(empty.toString(), null));
            assertTrue(Files.exists(empty));

            Path nonEmpty = tmp.resolve("d");
            mkdir(nonEmpty);
            writeFile(nonEmpty.resolve("f"), "f");
            assertFalse(fileOps.deleteFile(nonEmpty.toString(), null));
            assertTrue(Files.exists(nonEmpty));
            assertTrue(Files.exists(nonEmpty.resolve("f")));
        }

        @Test
        void deletesSymbolicLinkToFile(@TempDir Path tmp) throws IOException {
            Path target = tmp.resolve("target");
            writeFile(target, "x");
            Path link = tmp.resolve("link");
            Assumptions.assumeTrue(createSymbolicLink(link, target));

            assertTrue(fileOps.deleteFile(link.toString(), null));
            assertFalse(Files.exists(link));
            // the link target survives
            assertTrue(Files.exists(target));
        }

        @Test
        void deletesSymbolicLinkToDirectory(@TempDir Path tmp) throws IOException {
            // a symlink to a directory is not itself a directory; removeFile deletes the link, not the target
            Path dir = tmp.resolve("dir");
            mkdir(dir);
            Path link = tmp.resolve("linkdir");
            Assumptions.assumeTrue(createSymbolicLink(link, dir));

            assertTrue(fileOps.deleteFile(link.toString(), null));
            assertFalse(Files.exists(link));
            assertTrue(Files.exists(dir));
        }

        @Test
        void throwsWhenPathIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> fileOps.deleteFile("", null));
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
        void returnsNullForBrokenSymbolicLink(@TempDir Path tmp) {
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
        void returnsSymlinkOnlyForBrokenSymlink(@TempDir Path tmp) {
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

    @Nested
    class listDirectoryWithDepth {

        @Test
        void listsOneLevelDeep(@TempDir Path tmp) throws IOException {
            Path root = buildTree(tmp.resolve("root"));

            List<String> result = fileOps.listDirectory(root.resolve("docs").toString(), 1, null, null);

            assertEquals(2, result.size());
            assertEquals(root.resolve("docs/img").toString(), result.get(0));
            assertEquals(root.resolve("docs/readme.md").toString(), result.get(1));
        }

        @Test
        void listsTwoLevelsDeep(@TempDir Path tmp) throws IOException {
            Path root = buildTree(tmp.resolve("root"));

            List<String> result = fileOps.listDirectory(root.resolve("docs").toString(), 2, null, null);

            assertEquals(3, result.size());
            assertEquals(root.resolve("docs/img").toString(), result.get(0));
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
        void returnsNullWhenDepthIsZero(@TempDir Path tmp) throws IOException {
            Path root = buildTree(tmp.resolve("root"));

            List<String> result = fileOps.listDirectory(root.resolve("docs").toString(), 0, null, null);

            assertNull(result);
        }

        @Test
        void returnsNullWhenDirectoryDoesNotExist(@TempDir Path tmp) {
            List<String> result = fileOps.listDirectory(tmp.resolve("nope").toString(), 1, null, null);

            assertNull(result);
        }

        @Test
        void returnsNullWhenPathIsAFile(@TempDir Path tmp) throws IOException {
            Path root = buildTree(tmp.resolve("root"));

            List<String> result = fileOps.listDirectory(root.resolve(".gitignore").toString(), 1, null, null);

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
            assertEquals(root.resolve("d/b").toString(), result.get(0));
            assertEquals(root.resolve("d/b/c.txt").toString(), result.get(1));
            assertEquals(root.resolve("d/a.txt").toString(), result.get(2));
            assertEquals(root.resolve("d/c.txt").toString(), result.get(3));
        }
    }

    @Nested
    class copyDirectory {

        @Test
        void identifiesDescendantPath(@TempDir Path tmp) throws IOException {
            Path ancestor = tmp.resolve("ancestor");
            mkdir(ancestor);

            assertTrue(fileOps.isDescendantOf(ancestor.resolve("child"), ancestor));
            assertFalse(fileOps.isDescendantOf(ancestor, ancestor));
            assertFalse(fileOps.isDescendantOf(tmp.resolve("ancestor-copy"), ancestor));
        }

        @Test
        void copiesSingleFile(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve(".gitignore");
            writeFile(src, "git");
            Path dst = tmp.resolve(".gitignore.copy");

            assertTrue(fileOps.copyFile(src.toString(), dst.toString(), null, null));
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
        void returnsFalseWhenDstIsInsideSrc(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("src");
            mkdir(src);
            writeFile(src.resolve("file"), "x");
            Path dst = src.resolve("copy");

            assertFalse(fileOps.copyDirectory(src.toString(), dst.toString(), null, null));
            assertFalse(Files.exists(dst));
        }

        @Test
        void returnsFalseWhenDstAliasIsInsideSrc(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("src");
            mkdir(src);
            writeFile(src.resolve("file"), "x");
            Path alias = tmp.resolve("alias");
            Assumptions.assumeTrue(createSymbolicLink(alias, src));
            Path dst = alias.resolve("copy");

            assertFalse(fileOps.copyDirectory(src.toString(), dst.toString(), null, null));
            assertFalse(Files.exists(src.resolve("copy")));
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
        void throwsWhenSrcIsEmpty(@TempDir Path tmp) {
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
        void returnsFalseForSymbolicLink(@TempDir Path tmp) throws IOException {
            // copyDirectory does not follow symlinks; a symlink (even to a directory) is rejected
            Path dir = tmp.resolve("dir");
            mkdir(dir);
            writeFile(dir.resolve("f"), "f");
            Path link = tmp.resolve("link");
            Assumptions.assumeTrue(createSymbolicLink(link, dir));
            Path dst = tmp.resolve("link.copy");

            assertFalse(fileOps.copyDirectory(link.toString(), dst.toString(), null, null));
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

            assertTrue(fileOps.copyFile(src.toString(), dst.toString(), null, null));
            assertArrayEquals("hello".getBytes(), Files.readAllBytes(dst));
        }

        @Test
        void copiesEmptyFile(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("empty");
            writeFile(src, "");
            Path dst = tmp.resolve("empty.copy");

            assertTrue(fileOps.copyFile(src.toString(), dst.toString(), null, null));
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

            assertTrue(fileOps.copyFile(link.toString(), dst.toString(), null, null));
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

            assertFalse(fileOps.copyFile(link.toString(), dst.toString(), null, null));
            assertFalse(Files.exists(dst));
        }

        @Test
        void returnsFalseForDirectory(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("d");
            mkdir(d);
            Path dst = tmp.resolve("d.copy");

            assertFalse(fileOps.copyFile(d.toString(), dst.toString(), null, null));
            assertFalse(Files.exists(dst));
        }

        @Test
        void returnsFalseWhenSrcDoesNotExist(@TempDir Path tmp) {
            Path dst = tmp.resolve("dst");

            assertFalse(fileOps.copyFile(tmp.resolve("nope").toString(), dst.toString(), null, null));
            assertFalse(Files.exists(dst));
        }

        @Test
        void returnsFalseWhenDstAlreadyExists(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("a");
            writeFile(src, "x");
            Path dst = tmp.resolve("b");
            Files.createFile(dst);

            assertFalse(fileOps.copyFile(src.toString(), dst.toString(), null, null));
        }

        @Test
        void returnsFalseWhenAbortAlreadyRequested(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("a");
            writeFile(src, "x");
            Path dst = tmp.resolve("b");

            assertFalse(fileOps.copyFile(src.toString(), dst.toString(), new AtomicBoolean(true), null));
            assertFalse(Files.exists(dst));
        }

        @Test
        void returnsFalseWhenAbortAlreadyRequestedForEmptyFile(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("empty");
            writeFile(src, "");
            Path dst = tmp.resolve("empty.copy");

            assertFalse(fileOps.copyFile(src.toString(), dst.toString(), new AtomicBoolean(true), null));
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
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ignored) {
                }
                abort.set(true);
            });
            flipper.start();

            assertFalse(fileOps.copyFile(src.toString(), dst.toString(), abort, null));
            flipper.join();
            assertFalse(Files.exists(dst));
        }

        @Test
        void throwsWhenSrcIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> fileOps.copyFile("", "dst", null, null));
        }

        @Test
        void throwsWhenDstIsEmpty(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("a");
            writeFile(src, "x");
            assertThrows(IllegalArgumentException.class, () -> fileOps.copyFile(src.toString(), "", null, null));
        }
    }

    @Nested
    class rename {

        @Test
        void renamesRegularFile(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("a.txt");
            writeFile(src, "hello");
            Path dst = tmp.resolve("b.txt");

            assertTrue(fileOps.rename(src.toString(), dst.toString(), null));
            assertFalse(Files.exists(src));
            assertTrue(Files.exists(dst));
            assertArrayEquals("hello".getBytes(), Files.readAllBytes(dst));
        }

        @Test
        void renamesSymlinkToFile(@TempDir Path tmp) throws IOException {
            Path target = tmp.resolve("target");
            writeFile(target, "content");
            Path link = tmp.resolve("link");
            Assumptions.assumeTrue(createSymbolicLink(link, target));
            Path dst = tmp.resolve("link.moved");

            assertTrue(fileOps.rename(link.toString(), dst.toString(), null));
            assertFalse(Files.exists(link));
            assertTrue(Files.isSymbolicLink(dst));
            assertEquals(target.toString(), Files.readSymbolicLink(dst).toString());
        }

        @Test
        void returnsFalseWhenSrcDoesNotExist(@TempDir Path tmp) {
            assertFalse(fileOps.rename(tmp.resolve("nope").toString(), tmp.resolve("dst").toString(), null));
        }

        @Test
        void returnsFalseWhenDstAlreadyExists(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("a");
            writeFile(src, "x");
            Path dst = tmp.resolve("b");
            writeFile(dst, "y");

            assertFalse(fileOps.rename(src.toString(), dst.toString(), null));
            assertTrue(Files.exists(src));
        }

        @Test
        void throwsWhenSrcIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> fileOps.rename("", "dst", null));
        }

        @Test
        void throwsWhenDstIsEmpty(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("a");
            writeFile(src, "x");
            assertThrows(IllegalArgumentException.class, () -> fileOps.rename(src.toString(), "", null));
        }

        @Test
        void renamesEmptyDirectory(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("d");
            mkdir(src);
            Path dst = tmp.resolve("d.moved");

            assertTrue(fileOps.rename(src.toString(), dst.toString(), null));
            assertFalse(Files.exists(src));
            assertTrue(Files.isDirectory(dst));
        }

        @Test
        void renamesDirectoryTree(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("root");
            mkdir(src.resolve("sub"));
            writeFile(src.resolve("f"), "f");
            writeFile(src.resolve("sub/g"), "g");
            Path dst = tmp.resolve("root.moved");

            assertTrue(fileOps.rename(src.toString(), dst.toString(), null));
            assertFalse(Files.exists(src));
            assertTrue(Files.isDirectory(dst));
            assertArrayEquals("f".getBytes(), Files.readAllBytes(dst.resolve("f")));
            assertArrayEquals("g".getBytes(), Files.readAllBytes(dst.resolve("sub/g")));
        }

        @Test
        void renamesSymlinkToDirectoryAsLinkItself(@TempDir Path tmp) throws IOException {
            // a symlink is renamed as the link itself; the target directory is left untouched
            Path target = tmp.resolve("target");
            mkdir(target);
            writeFile(target.resolve("f"), "f");
            Path link = tmp.resolve("link");
            Assumptions.assumeTrue(createSymbolicLink(link, target));
            Path dst = tmp.resolve("link.renamed");

            assertTrue(fileOps.rename(link.toString(), dst.toString(), null));
            assertFalse(Files.exists(link));
            assertTrue(Files.isSymbolicLink(dst));
            assertTrue(Files.exists(target));
            assertTrue(Files.exists(target.resolve("f")));
        }

        @Test
        void onRenamedCalledForAtomicRename(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("d");
            mkdir(src);
            Path dst = tmp.resolve("d.moved");

            List<String> moved = new java.util.ArrayList<>();
            FileOps.OnActionListener onAction = (action, from, to, succeeded) -> {
                if (action == FileOps.Action.RENAME && succeeded != null) {
                    moved.add(from + " -> " + to);
                }
            };

            assertTrue(fileOps.rename(src.toString(), dst.toString(), onAction));
            assertEquals(1, moved.size());
            assertEquals(src + " -> " + dst, moved.get(0));
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

                assertTrue(fileOps.deleteDirectory(rel, true, false, null, null));
                assertFalse(Files.exists(root));
            } finally {
                rm(root);
            }
        }
    }
}
