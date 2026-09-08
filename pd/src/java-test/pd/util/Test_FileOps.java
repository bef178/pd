package pd.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
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

            a = FileOps.singleton.list("src", 0);
            assertNull(a);

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
        public void list_brokenSymbolicLink(@TempDir Path root) throws IOException {
            Path symlink = root.resolve("symlink");
            Files.createSymbolicLink(symlink, root.resolve("missing"));

            // follow: a broken symlink resolves to nothing, hence invisible
            assertNull(FileOps.singleton.list(symlink.toString(), 2));
            // not follow: the symlink itself is a node
            assertTrue(FileOps.singleton.list(symlink.toString(), 2, false).isEmpty());
        }

        @Test
        public void list_followSymlinks(@TempDir Path tmp) throws IOException {
            Path root = tmp.resolve("root");
            mkdir(root.resolve("d/full"));
            writeFile(root.resolve("d/full/x"), "x");
            mkdir(root.resolve("d/empty"));
            writeFile(root.resolve("d/b.txt"), "x");
            if (!createSymbolicLink(root.resolve("d/lkN"), root.resolve("d/full"))) {
                return;
            }
            if (!createSymbolicLink(root.resolve("d/lkB"), root.resolve("d/missing"))) {
                return;
            }
            String d = root.resolve("d").toString();

            // follow (default): a symlink is its target; a broken one is invisible
            assertArrayEquals(new String[] {d + "/empty/", d + "/full/x", d + "/lkN/x", d + "/b.txt"},
                    FileOps.singleton.list(d, 3).toArray());

            // not follow: a symlink is a leaf node
            assertArrayEquals(new String[] {d + "/empty/", d + "/full/x", d + "/b.txt", d + "/lkB", d + "/lkN"},
                    FileOps.singleton.list(d, 3, false).toArray());
        }

        @Test
        public void list_symlinkBoundaryAndAnchor(@TempDir Path tmp) throws IOException {
            Path root = tmp.resolve("root");
            mkdir(root.resolve("d/full"));
            writeFile(root.resolve("d/full/x"), "x");
            mkdir(root.resolve("d/empty"));
            writeFile(root.resolve("d/b.txt"), "x");
            if (!createSymbolicLink(root.resolve("d/lkN"), root.resolve("d/full"))) {
                return;
            }
            if (!createSymbolicLink(root.resolve("d/lkE"), root.resolve("d/empty"))) {
                return;
            }
            if (!createSymbolicLink(root.resolve("d/lkB"), root.resolve("d/missing"))) {
                return;
            }
            // broken symlink nested inside a directory: exercises the DFS-level filter
            if (!createSymbolicLink(root.resolve("d/full/lkB"), root.resolve("d/missing"))) {
                return;
            }
            String d = root.resolve("d").toString();

            // boundary (depth=1): a symlink to a directory ends with "/"; a broken one is invisible
            assertArrayEquals(new String[] {d + "/empty/", d + "/full/", d + "/lkE/", d + "/lkN/", d + "/b.txt"},
                    FileOps.singleton.list(d + "/", 1).toArray());
            assertArrayEquals(new String[] {d + "/empty/", d + "/full/", d + "/b.txt", d + "/lkB", d + "/lkE", d + "/lkN"},
                    FileOps.singleton.list(d + "/", 1, false).toArray());

            // a symlink to an empty directory is reported like the empty directory itself;
            // the nested broken symlink never appears
            assertArrayEquals(new String[] {d + "/empty/", d + "/full/x", d + "/lkE/", d + "/lkN/x", d + "/b.txt"},
                    FileOps.singleton.list(d, 3).toArray());

            // prefix anchored at a symlink to a directory: a directory when following, a file otherwise
            assertArrayEquals(new String[] {d + "/lkN/x"},
                    FileOps.singleton.list(d + "/lkN", 3).toArray());
            assertTrue(FileOps.singleton.list(d + "/lkN", 3, false).isEmpty());

            // a trailing "/" declares a directory even when not following symlinks
            assertArrayEquals(new String[] {d + "/lkN/x"},
                    FileOps.singleton.list(d + "/lkN/", 3).toArray());
            assertArrayEquals(new String[] {d + "/lkN/lkB", d + "/lkN/x"},
                    FileOps.singleton.list(d + "/lkN/", 3, false).toArray());
        }

        @Test
        public void list_symlinkToFile(@TempDir Path tmp) throws IOException {
            Path root = tmp.resolve("root");
            writeFile(root.resolve("d/target.txt"), "t");
            if (!createSymbolicLink(root.resolve("d/lkF"), root.resolve("d/target.txt"))) {
                return;
            }
            String d = root.resolve("d").toString();

            // a symlink to an existing file is a file in both modes
            // (following: the exists filter must not mistake it for a broken link)
            assertArrayEquals(new String[] {d + "/lkF", d + "/target.txt"},
                    FileOps.singleton.list(d, 2).toArray());
            assertArrayEquals(new String[] {d + "/lkF", d + "/target.txt"},
                    FileOps.singleton.list(d, 2, false).toArray());

            // prefix anchored at it: a file in both modes
            assertTrue(FileOps.singleton.list(d + "/lkF", 2).isEmpty());
            assertTrue(FileOps.singleton.list(d + "/lkF", 2, false).isEmpty());
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
        writeFile(root.resolve("empty-file"), "");
        mkdir(root.resolve("empty"));
        mkdir(root.resolve("empty-directory"));
        return root;
    }

    // ---- FileOpsCore methods ----

    @Nested
    class createEmptyDirectory {

        @Test
        void createsDirectoryWhenParentExists(@TempDir Path tmp) {
            assertTrue(fileOps.createEmptyDirectory(tmp.resolve("d").toString()));
            assertTrue(Files.isDirectory(tmp.resolve("d")));
        }

        @Test
        void returnsFalseWhenAlreadyExists(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("d");
            mkdir(d);

            assertFalse(fileOps.createEmptyDirectory(d.toString()));
        }

        @Test
        void returnsFalseWhenPathIsAnExistingFile(@TempDir Path tmp) throws IOException {
            Path f = tmp.resolve("f");
            writeFile(f, "x");

            assertFalse(fileOps.createEmptyDirectory(f.toString()));
            assertTrue(Files.isRegularFile(f));
        }

        @Test
        void returnsFalseWhenParentDoesNotExist(@TempDir Path tmp) {
            assertFalse(fileOps.createEmptyDirectory(tmp.resolve("missing/d").toString()));
            assertFalse(Files.exists(tmp.resolve("missing")));
        }

        @Test
        void returnsFalseWhenPathIsSymbolicLink(@TempDir Path tmp) {
            // a symlink at the target path already "exists" (NOFOLLOW_LINKS), even if broken
            Path link = tmp.resolve("link");
            Assumptions.assumeTrue(createSymbolicLink(link, tmp.resolve("missing")));

            assertFalse(fileOps.createEmptyDirectory(link.toString()));
        }

        @Test
        void throwsWhenPathIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> fileOps.createEmptyDirectory(""));
        }
    }

    @Nested
    class removeEmptyDirectory {

        @Test
        void removesEmptyDirectory(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("d");
            mkdir(d);

            assertTrue(fileOps.removeEmptyDirectory(d.toString()));
            assertFalse(Files.exists(d));
        }

        @Test
        void returnsFalseWhenNotEmpty(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("d");
            mkdir(d);
            writeFile(d.resolve("f"), "f");

            assertFalse(fileOps.removeEmptyDirectory(d.toString()));
            assertTrue(Files.exists(d.resolve("f")));
        }

        @Test
        void returnsFalseWhenDoesNotExist(@TempDir Path tmp) {
            assertFalse(fileOps.removeEmptyDirectory(tmp.resolve("nope").toString()));
        }

        @Test
        void returnsFalseForRegularFile(@TempDir Path tmp) throws IOException {
            Path f = tmp.resolve("f");
            writeFile(f, "x");

            assertFalse(fileOps.removeEmptyDirectory(f.toString()));
            assertTrue(Files.exists(f));
        }

        @Test
        void returnsFalseForSymbolicLinkToDirectory(@TempDir Path tmp) throws IOException {
            // a symlink to a directory is not itself a directory (NOFOLLOW_LINKS); rejected, target untouched
            Path dir = tmp.resolve("dir");
            mkdir(dir);
            Path link = tmp.resolve("link");
            Assumptions.assumeTrue(createSymbolicLink(link, dir));

            assertFalse(fileOps.removeEmptyDirectory(link.toString()));
            assertTrue(Files.exists(link, LinkOption.NOFOLLOW_LINKS));
            assertTrue(Files.exists(dir));
        }

        @Test
        void throwsWhenPathIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> fileOps.removeEmptyDirectory(""));
        }
    }

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
    class removeDirectory {

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
            // aborted before any removal: the tree is intact
            assertTrue(Files.exists(d));
            assertTrue(Files.exists(d.resolve("f")));
            assertTrue(Files.exists(d.resolve("sub/g")));
        }

        @Test
        void returnsFalseAndKeepsEmptyDirectoryWhenAbortRequestedBeforeStart(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("d");
            mkdir(d);

            assertFalse(fileOps.removeDirectory(d.toString(), true, false, new AtomicBoolean(true), null));
            assertTrue(Files.exists(d));
        }

        @Test
        void abortStopsRemovalBeforeAnyRemovalStarts(@TempDir Path tmp) throws IOException {
            Path leaf = tmp.resolve("p/q/r");
            mkdir(leaf);
            AtomicBoolean abort = new AtomicBoolean(true);

            assertFalse(fileOps.removeDirectory(leaf.toString(), false, true, abort, null));
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

            assertFalse(fileOps.removeDirectory(link.toString(), true, false, null, null));
            assertTrue(Files.exists(link, LinkOption.NOFOLLOW_LINKS));
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
        void removesChildrenInLeafOrderWithSymlink(@TempDir Path tmp) throws IOException {
            // children sort by their own strings, not following symlinks:
            // the real directory first, then the symlink among the leaves by name
            Path d = tmp.resolve("d");
            mkdir(d.resolve("sub"));
            writeFile(d.resolve("sub/x"), "x");
            writeFile(d.resolve("a.txt"), "a");
            Path ext = tmp.resolve("ext");
            mkdir(ext);
            Path link = d.resolve("zlink");
            if (!createSymbolicLink(link, ext)) {
                return;
            }

            List<String> removed = new LinkedList<>();
            FileOps.OnActionListener listener = (action, from, to, succeeded) -> {
                if (action == FileOps.Action.REMOVE && succeeded != null) {
                    removed.add(from);
                }
            };

            assertTrue(fileOps.removeDirectory(d.toString(), true, false, null, listener));
            assertEquals(5, removed.size());
            assertEquals(d.resolve("sub/x").toString(), removed.get(0));
            // directories are reported with a trailing "/"
            assertEquals(d.resolve("sub") + "/", removed.get(1));
            assertEquals(d.resolve("a.txt").toString(), removed.get(2));
            assertEquals(link.toString(), removed.get(3));
            assertEquals(d + "/", removed.get(4));
            assertTrue(Files.exists(ext));
        }

        @Test
        void listenerNotifiedForEachRemovedEntry(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("d");
            mkdir(d.resolve("sub"));
            writeFile(d.resolve("f"), "f");
            writeFile(d.resolve("sub/g"), "g");

            List<String> removed = new LinkedList<>();
            FileOps.OnActionListener listener = (action, from, to, succeeded) -> {
                if (action == FileOps.Action.REMOVE && succeeded != null) {
                    removed.add(from);
                }
            };

            assertTrue(fileOps.removeDirectory(d.toString(), true, false, null, listener));
            assertEquals(4, removed.size());
            assertEquals(d.resolve("sub/g").toString(), removed.get(0));
            // directories are reported with a trailing "/"
            assertEquals(d.resolve("sub") + "/", removed.get(1));
            assertEquals(d.resolve("f").toString(), removed.get(2));
            assertEquals(d + "/", removed.get(3));
            assertFalse(Files.exists(d));
        }

        @Test
        void listenerNotifiedForAncestorsWhenParentsTrue(@TempDir Path tmp) throws IOException {
            Path leaf = tmp.resolve("p/q/r");
            mkdir(leaf);

            List<String> removed = new LinkedList<>();
            FileOps.OnActionListener listener = (action, from, to, succeeded) -> {
                if (action == FileOps.Action.REMOVE && succeeded != null) {
                    removed.add(from);
                }
            };

            assertTrue(fileOps.removeDirectory(leaf.toString(), false, true, null, listener));
            assertTrue(removed.contains(leaf + "/"));
            assertTrue(removed.contains(tmp.resolve("p/q") + "/"));
            assertTrue(removed.contains(tmp.resolve("p") + "/"));
            assertFalse(Files.exists(tmp.resolve("p")));
        }

        @Test
        void listenerNotNotifiedWhenAbortRequested(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("d");
            mkdir(d);
            writeFile(d.resolve("f"), "f");

            List<String> removed = new LinkedList<>();
            FileOps.OnActionListener listener = (action, from, to, succeeded) -> {
                if (action == FileOps.Action.REMOVE && succeeded != null) {
                    removed.add(from);
                }
            };

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

            assertTrue(fileOps.removeFile(f.toString(), null));
            assertFalse(Files.exists(f));
        }

        @Test
        void returnsFalseWhenFileDoesNotExist(@TempDir Path tmp) {
            assertFalse(fileOps.removeFile(tmp.resolve("nope").toString(), null));
        }

        @Test
        void returnsFalseForDirectory(@TempDir Path tmp) throws IOException {
            // removeFile only removes files; a directory (empty or not) is left untouched
            Path empty = tmp.resolve("empty");
            mkdir(empty);
            assertFalse(fileOps.removeFile(empty.toString(), null));
            assertTrue(Files.exists(empty));

            Path nonEmpty = tmp.resolve("d");
            mkdir(nonEmpty);
            writeFile(nonEmpty.resolve("f"), "f");
            assertFalse(fileOps.removeFile(nonEmpty.toString(), null));
            assertTrue(Files.exists(nonEmpty));
            assertTrue(Files.exists(nonEmpty.resolve("f")));
        }

        @Test
        void removesSymbolicLinkToFile(@TempDir Path tmp) throws IOException {
            Path target = tmp.resolve("target");
            writeFile(target, "x");
            Path link = tmp.resolve("link");
            Assumptions.assumeTrue(createSymbolicLink(link, target));

            assertTrue(fileOps.removeFile(link.toString(), null));
            assertFalse(Files.exists(link));
            // the link target survives
            assertTrue(Files.exists(target));
        }

        @Test
        void removesSymbolicLinkToDirectory(@TempDir Path tmp) throws IOException {
            // a symlink to a directory is not itself a directory; removeFile removes the link, not the target
            Path dir = tmp.resolve("dir");
            mkdir(dir);
            Path link = tmp.resolve("linkdir");
            Assumptions.assumeTrue(createSymbolicLink(link, dir));

            assertTrue(fileOps.removeFile(link.toString(), null));
            assertFalse(Files.exists(link));
            assertTrue(Files.exists(dir));
        }

        @Test
        void throwsWhenPathIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> fileOps.removeFile("", null));
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
        void returnsFileTypeWithSizeForRegularFile(@TempDir Path tmp) throws IOException {
            Path f = tmp.resolve("a.txt");
            writeFile(f, "hello");

            FileStat result = fileOps.stat(f.toString());

            assertNotNull(result);
            assertEquals(FileStat.TYPE_FILE, result.type);
            assertEquals(5, result.size.longValue());
            assertEquals(f.toString(), result.path);
        }

        @Test
        void returnsDirectoryTypeForDirectory(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("d");
            mkdir(d);

            FileStat result = fileOps.stat(d.toString());

            assertNotNull(result);
            assertEquals(FileStat.TYPE_DIRECTORY, result.type);
            assertNull(result.size);
        }

        @Test
        void returnsNonexistentStatForMissingPath(@TempDir Path tmp) {
            FileStat result = fileOps.stat(tmp.resolve("nope").toString());

            assertNotNull(result);
            assertFalse(result.exists(false));
            assertEquals(tmp.resolve("nope").toString(), result.path);
        }

        @Test
        void returnsSymlinkTypeAndOwnAttributesForSymlinkToFile(@TempDir Path tmp) throws IOException {
            // symlink to file: type = SYMLINK | FILE, size/mtime are the link's own
            Path target = tmp.resolve("target");
            writeFile(target, "content");
            Path link = tmp.resolve("link");
            Assumptions.assumeTrue(createSymbolicLink(link, target));

            FileStat result = fileOps.stat(link.toString());

            assertNotNull(result);
            assertEquals(FileStat.TYPE_SYMLINK | FileStat.TYPE_FILE, result.type);
            // symlink's own size = length of the target path it stores
            assertEquals(target.toString().length(), result.size.longValue(),
                    String.format("E: symlink own size, target path: `%s`", target));
        }

        @Test
        void returnsSymlinkTypeAndOwnAttributesForSymlinkToDirectory(@TempDir Path tmp) throws IOException {
            // symlink to directory: type = SYMLINK | DIRECTORY, attributes are the link's own
            Path dir = tmp.resolve("dir");
            mkdir(dir);
            Path link = tmp.resolve("link");
            Assumptions.assumeTrue(createSymbolicLink(link, dir));

            FileStat result = fileOps.stat(link.toString());

            assertNotNull(result);
            assertEquals(FileStat.TYPE_SYMLINK | FileStat.TYPE_DIRECTORY, result.type);
            // symlink's own size = length of the target path it stores
            assertEquals(dir.toString().length(), result.size.longValue(),
                    String.format("E: symlink own size, target path: `%s`", dir));
        }

        @Test
        void returnsSymlinkOnlyForBrokenSymlink(@TempDir Path tmp) {
            // broken symlink: type = SYMLINK only (no target type), attributes are the link's own
            Path target = tmp.resolve("missing");
            Path link = tmp.resolve("link");
            Assumptions.assumeTrue(createSymbolicLink(link, target));

            FileStat result = fileOps.stat(link.toString());

            assertNotNull(result);
            assertEquals(FileStat.TYPE_SYMLINK, result.type);
            assertTrue(result.isDanglingSymlink());
            // symlink's own size = length of the stored target path
            assertEquals(target.toString().length(), result.size.longValue(),
                    String.format("E: symlink own size, target path: `%s`", target));
        }

        @Test
        void throwsWhenPathIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> fileOps.stat(""));
        }
    }

    @Nested
    class listDirectory {

        // asserts the call succeeded and returns the nodes met via callback
        private List<String> listAndCollect(String directory, int depth) {
            List<String> a = new LinkedList<>();
            boolean succeeded = fileOps.listDirectory(directory, depth, true, null, (action, from, to, callbackSucceeded) -> {
                if (action == FileOps.Action.MEET) {
                    a.add(from);
                }
            });
            assertTrue(succeeded, "expected listDirectory to succeed");
            return a;
        }

        @Test
        void listDirectory_baseline(@TempDir Path root) throws IOException {
            buildTree(root);

            assertFalse(FileOps.singleton.listDirectory(root.toString(), 0, true, null, null));

            assertThrows(IllegalArgumentException.class, () -> fileOps.listDirectory("", 1, true, null, null));

            assertFalse(fileOps.listDirectory(root.resolve("not-exist").toString(), 1, true, null, null));

            assertFalse(fileOps.listDirectory(root.resolve("empty-file").toString(), 1, true, null, null));

            List<String> a = listAndCollect(root.resolve("empty").toString(), 1);
            assertTrue(a.isEmpty());

            a = listAndCollect(root.resolve("docs").toString(), 1);
            assertArrayEquals(new String[] {root.resolve("docs/img/") + "/", root.resolve("docs/readme.md").toString()}, a.toArray());

            a = listAndCollect(root.resolve("docs").toString(), 2);
            assertArrayEquals(new String[] {root.resolve("docs/img/") + "/", root.resolve("docs/img/a.png").toString(), root.resolve("docs/readme.md").toString()}, a.toArray());
        }

        @Test
        void returnsFalseWhenAbortAlreadyRequested(@TempDir Path tmp) throws IOException {
            Path root = buildTree(tmp.resolve("root"));

            List<String> met = new LinkedList<>();
            FileOps.OnActionListener listener = (action, from, to, succeeded) -> {
                if (action == FileOps.Action.MEET) {
                    met.add(from);
                }
            };

            assertFalse(fileOps.listDirectory(root.resolve("docs").toString(), 1, true, new AtomicBoolean(true), listener));
            assertTrue(met.isEmpty());
        }

        @Test
        void reportsSymlinkToDirectoryWithTrailingSlash(@TempDir Path tmp) throws IOException {
            Path root = buildTree(tmp.resolve("root"));
            Path link = root.resolve("docs/link");
            if (!createSymbolicLink(link, root.resolve("docs/img"))) {
                return;
            }
            Path broken = root.resolve("docs/broken");
            if (!createSymbolicLink(broken, root.resolve("docs/missing"))) {
                return;
            }
            String img = root.resolve("docs/img") + "/";
            String readme = root.resolve("docs/readme.md").toString();

            // follow (default): a symlink to a directory is a directory, reported with "/";
            // a broken symlink is invisible
            List<String> result = listAndCollect(root.resolve("docs").toString(), 1);
            assertArrayEquals(new String[] {img, link + "/", readme}, result.toArray());

            // depth=2: the symlink is traversed into, its content reported under the link path
            result = listAndCollect(root.resolve("docs").toString(), 2);
            assertArrayEquals(new String[] {img, img + "a.png", link + "/", link + "/a.png", readme}, result.toArray());

            // not follow: symlinks are leaf nodes; a broken one is a node too
            List<String> notFollowed = new LinkedList<>();
            boolean succeeded = fileOps.listDirectory(root.resolve("docs").toString(), 1, false, null,
                    (action, from, to, callbackSucceeded) -> {
                        if (action == FileOps.Action.MEET) {
                            notFollowed.add(from);
                        }
                    });
            assertTrue(succeeded);
            assertArrayEquals(new String[] {img, broken.toString(), link.toString(), readme}, notFollowed.toArray());

            // depth=2: real directories still expand, the symlink does not
            notFollowed.clear();
            succeeded = fileOps.listDirectory(root.resolve("docs").toString(), 2, false, null,
                    (action, from, to, callbackSucceeded) -> {
                        if (action == FileOps.Action.MEET) {
                            notFollowed.add(from);
                        }
                    });
            assertTrue(succeeded);
            assertArrayEquals(new String[] {img, img + "a.png", broken.toString(), link.toString(), readme}, notFollowed.toArray());
        }

        @Test
        void listsDirectoriesBeforeFiles(@TempDir Path tmp) throws IOException {
            // dir-first: b/ (and its child) before a.txt and c.txt
            Path root = tmp.resolve("root");
            mkdir(root.resolve("d/b"));
            writeFile(root.resolve("d/a.txt"), "a");
            writeFile(root.resolve("d/b/c.txt"), "c");
            writeFile(root.resolve("d/c.txt"), "c");

            List<String> result = listAndCollect(root.resolve("d").toString(), 2);

            assertEquals(4, result.size());
            assertEquals(root.resolve("d/b") + "/", result.get(0));
            assertEquals(root.resolve("d/b/c.txt").toString(), result.get(1));
            assertEquals(root.resolve("d/a.txt").toString(), result.get(2));
            assertEquals(root.resolve("d/c.txt").toString(), result.get(3));
        }

        @Test
        void followsSymlinkIntoDirectoryPerMode(@TempDir Path tmp) throws IOException {
            Path root = tmp.resolve("root");
            mkdir(root.resolve("d/full"));
            writeFile(root.resolve("d/full/x"), "x");
            mkdir(root.resolve("d/empty"));
            writeFile(root.resolve("d/b.txt"), "x");
            if (!createSymbolicLink(root.resolve("d/lkN"), root.resolve("d/full"))) {
                return;
            }
            if (!createSymbolicLink(root.resolve("d/lkB"), root.resolve("d/missing"))) {
                return;
            }
            String d = root.resolve("d").toString();

            // follow: the symlink is traversed into; a broken symlink is invisible;
            // an empty directory is met once with "/"
            assertArrayEquals(new String[] {d + "/empty/", d + "/full/", d + "/full/x", d + "/lkN/", d + "/lkN/x", d + "/b.txt"},
                    listAndCollect(d, 2).toArray());

            // not follow: the symlink is a leaf; a broken symlink is a node too;
            // real directories still expand
            List<String> met = new LinkedList<>();
            boolean succeeded = fileOps.listDirectory(d, 2, false, null, (action, from, to, callbackSucceeded) -> {
                if (action == FileOps.Action.MEET) {
                    met.add(from);
                }
            });
            assertTrue(succeeded);
            assertArrayEquals(new String[] {d + "/empty/", d + "/full/", d + "/full/x", d + "/b.txt", d + "/lkB", d + "/lkN"},
                    met.toArray());
        }

        @Test
        void stopsMidWalkWhenAbortRequested(@TempDir Path tmp) throws IOException {
            Path root = buildTree(tmp.resolve("root"));

            AtomicBoolean abort = new AtomicBoolean(false);
            List<String> met = new LinkedList<>();
            boolean succeeded = fileOps.listDirectory(root.resolve("docs").toString(), 3, true, abort,
                    (action, from, to, callbackSucceeded) -> {
                        if (action == FileOps.Action.MEET) {
                            met.add(from);
                            // request the abort right after the first node is met
                            abort.set(true);
                        }
                    });

            assertFalse(succeeded);
            assertEquals(1, met.size());
        }

        @Test
        void reportsEmptyDirectoryChildOnce(@TempDir Path tmp) throws IOException {
            Path root = tmp.resolve("root");
            mkdir(root.resolve("d/sub"));
            writeFile(root.resolve("d/sub/x"), "x");
            mkdir(root.resolve("d/empty"));
            String d = root.resolve("d").toString();

            List<String> result = listAndCollect(d, 2);

            // an empty directory is met once with "/", nothing below it
            assertArrayEquals(new String[] {d + "/empty/", d + "/sub/", d + "/sub/x"}, result.toArray());
        }
    }

    @Nested
    class pathToString {

        @Test
        void appendsSlashToDirectories(@TempDir Path tmp) throws IOException {
            Path dir = tmp.resolve("d");
            mkdir(dir);

            assertEquals(dir + "/", fileOps.pathToString(dir, true));
            assertEquals(dir + "/", fileOps.pathToString(dir, false));
        }

        @Test
        void fileHasNoSlash(@TempDir Path tmp) throws IOException {
            Path f = tmp.resolve("f");
            writeFile(f, "x");

            assertEquals(f.toString(), fileOps.pathToString(f, true));
            assertEquals(f.toString(), fileOps.pathToString(f, false));
        }

        @Test
        void symlinkToDirectoryFollowsTheFlag(@TempDir Path tmp) throws IOException {
            Path dir = tmp.resolve("d");
            mkdir(dir);
            Path link = tmp.resolve("link");
            if (!createSymbolicLink(link, dir)) {
                return;
            }

            // following: the link is its target (a directory); not following: a leaf
            assertEquals(link + "/", fileOps.pathToString(link, true));
            assertEquals(link.toString(), fileOps.pathToString(link, false));
        }

        @Test
        void brokenSymlink(@TempDir Path tmp) throws IOException {
            Path link = tmp.resolve("link");
            if (!createSymbolicLink(link, tmp.resolve("missing"))) {
                return;
            }

            // following: a broken symlink resolves to nothing, hence invisible
            assertNull(fileOps.pathToString(link, true));
            // not following: the symlink itself is a node
            assertEquals(link.toString(), fileOps.pathToString(link, false));
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

            assertTrue(fileOps.copyFile(src.toString(), dst.toString(), true, null, null));
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

        @Test
        void copiesChildrenInLeafOrderWithSymlink(@TempDir Path tmp) throws IOException {
            // children sort by their own strings, not following symlinks:
            // the real directory first, then the symlink among the leaves by name
            Path src = tmp.resolve("root");
            mkdir(src.resolve("sub"));
            writeFile(src.resolve("sub/x"), "x");
            writeFile(src.resolve("a.txt"), "a");
            Path dir = tmp.resolve("dir");
            mkdir(dir);
            Path link = src.resolve("zlink");
            if (!createSymbolicLink(link, dir)) {
                return;
            }
            Path dst = tmp.resolve("root.copy");

            List<String> copied = new LinkedList<>();
            FileOps.OnActionListener listener = (action, from, to, succeeded) -> {
                if (action == FileOps.Action.CREATE && succeeded != null && from != null) {
                    copied.add(from);
                }
            };

            assertTrue(fileOps.copyDirectory(src.toString(), dst.toString(), null, listener));
            // only actual per-entry operations are reported, no per-directory summary:
            // sub/x (leaf), a.txt (leaf), zlink (symlink copied); directories are dst-side
            // events (from = null) and not collected here
            assertEquals(3, copied.size());
            assertEquals(src.resolve("sub/x").toString(), copied.get(0));
            assertEquals(src.resolve("a.txt").toString(), copied.get(1));
            assertEquals(link.toString(), copied.get(2));
            // the child was copied as a symlink
            assertTrue(Files.isSymbolicLink(dst.resolve("zlink")));
        }
    }

    @Nested
    class copyFile {

        @Test
        void copiesFileContent(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("a");
            writeFile(src, "hello");
            Path dst = tmp.resolve("a.copy");

            assertTrue(fileOps.copyFile(src.toString(), dst.toString(), true, null, null));
            assertArrayEquals("hello".getBytes(), Files.readAllBytes(dst));
        }

        @Test
        void copiesLargeFileAcrossTransferChunks(@TempDir Path tmp) throws IOException {
            // larger than a single 4MB transferTo chunk, with an odd tail:
            // exercises the chunk loop and position tracking
            int size = 4 * 1024 * 1024 + 12345;
            byte[] data = new byte[size];
            for (int i = 0; i < size; i++) {
                data[i] = (byte) i;
            }
            Path src = tmp.resolve("big");
            Files.write(src, data);
            Path dst = tmp.resolve("big.copy");

            assertTrue(fileOps.copyFile(src.toString(), dst.toString(), true, null, null));
            assertArrayEquals(data, Files.readAllBytes(dst));
        }

        @Test
        void copiesEmptyFile(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("empty");
            writeFile(src, "");
            Path dst = tmp.resolve("empty.copy");

            assertTrue(fileOps.copyFile(src.toString(), dst.toString(), true, null, null));
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

            assertTrue(fileOps.copyFile(link.toString(), dst.toString(), true, null, null));
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

            assertFalse(fileOps.copyFile(link.toString(), dst.toString(), true, null, null));
            assertFalse(Files.exists(dst));
        }

        @Test
        void returnsFalseForDirectory(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("d");
            mkdir(d);
            Path dst = tmp.resolve("d.copy");

            assertFalse(fileOps.copyFile(d.toString(), dst.toString(), true, null, null));
            assertFalse(Files.exists(dst));
        }

        @Test
        void returnsFalseWhenSrcDoesNotExist(@TempDir Path tmp) {
            Path dst = tmp.resolve("dst");

            assertFalse(fileOps.copyFile(tmp.resolve("nope").toString(), dst.toString(), true, null, null));
            assertFalse(Files.exists(dst));
        }

        @Test
        void returnsFalseWhenDstAlreadyExists(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("a");
            writeFile(src, "x");
            Path dst = tmp.resolve("b");
            Files.createFile(dst);

            assertFalse(fileOps.copyFile(src.toString(), dst.toString(), true, null, null));
        }

        @Test
        void returnsFalseWhenAbortAlreadyRequested(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("a");
            writeFile(src, "x");
            Path dst = tmp.resolve("b");

            assertFalse(fileOps.copyFile(src.toString(), dst.toString(), true, new AtomicBoolean(true), null));
            assertFalse(Files.exists(dst));
        }

        @Test
        void returnsFalseWhenAbortAlreadyRequestedForEmptyFile(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("empty");
            writeFile(src, "");
            Path dst = tmp.resolve("empty.copy");

            assertFalse(fileOps.copyFile(src.toString(), dst.toString(), true, new AtomicBoolean(true), null));
            assertFalse(Files.exists(dst));
        }

        @Test
        void removesPartialFileWhenAbortedMidCopy(@TempDir Path tmp) throws Exception {
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

            assertFalse(fileOps.copyFile(src.toString(), dst.toString(), true, abort, null));
            flipper.join();
            assertFalse(Files.exists(dst));
        }

        @Test
        void copiesSymlinkItselfWhenNotFollowing(@TempDir Path tmp) throws IOException {
            // follow=false: the symlink itself is copied; dst is a link to the same target
            Path target = tmp.resolve("target");
            writeFile(target, "content");
            Path link = tmp.resolve("link");
            Assumptions.assumeTrue(createSymbolicLink(link, target));
            Path dst = tmp.resolve("link.copy");

            assertTrue(fileOps.copyFile(link.toString(), dst.toString(), false, null, null));
            assertTrue(Files.isSymbolicLink(dst));
            assertEquals(target.toString(), Files.readSymbolicLink(dst).toString());
            assertArrayEquals("content".getBytes(), Files.readAllBytes(dst));
        }

        @Test
        void copiesBrokenSymlinkItselfWhenNotFollowing(@TempDir Path tmp) throws IOException {
            // follow=false: a broken symlink is still a node; the dangling link is copied
            Path link = tmp.resolve("link");
            Assumptions.assumeTrue(createSymbolicLink(link, tmp.resolve("missing")));
            Path dst = tmp.resolve("link.copy");

            assertTrue(fileOps.copyFile(link.toString(), dst.toString(), false, null, null));
            assertTrue(Files.isSymbolicLink(dst));
            assertFalse(Files.exists(dst));
        }

        @Test
        void copiesSymlinkToDirectoryAsLinkWhenNotFollowing(@TempDir Path tmp) throws IOException {
            // follow=false: a symlink to a directory is copied as the link itself, not dereferenced
            Path dir = tmp.resolve("dir");
            mkdir(dir);
            writeFile(dir.resolve("f"), "f");
            Path link = tmp.resolve("link");
            Assumptions.assumeTrue(createSymbolicLink(link, dir));
            Path dst = tmp.resolve("link.copy");

            assertTrue(fileOps.copyFile(link.toString(), dst.toString(), false, null, null));
            assertTrue(Files.isSymbolicLink(dst));
            assertEquals(dir.toString(), Files.readSymbolicLink(dst).toString());
            // the target directory is not duplicated
            assertTrue(Files.exists(dir.resolve("f")));
        }

        @Test
        void returnsFalseForBrokenSymlinkWhenFollowing(@TempDir Path tmp) throws IOException {
            // follow=true: a broken symlink resolves to nothing; rejected, nothing created
            Path link = tmp.resolve("link");
            Assumptions.assumeTrue(createSymbolicLink(link, tmp.resolve("missing")));
            Path dst = tmp.resolve("link.copy");

            assertFalse(fileOps.copyFile(link.toString(), dst.toString(), true, null, null));
            assertFalse(Files.exists(dst));
        }

        @Test
        void throwsWhenSrcIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> fileOps.copyFile("", "dst", true, null, null));
        }

        @Test
        void throwsWhenDstIsEmpty(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("a");
            writeFile(src, "x");
            assertThrows(IllegalArgumentException.class, () -> fileOps.copyFile(src.toString(), "", true, null, null));
        }
    }

    @Nested
    class move {

        @Test
        void movesRegularFile(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("a.txt");
            writeFile(src, "hello");
            Path dst = tmp.resolve("b.txt");

            assertTrue(fileOps.move(src.toString(), dst.toString(), null));
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

            assertTrue(fileOps.move(link.toString(), dst.toString(), null));
            assertFalse(Files.exists(link));
            assertTrue(Files.isSymbolicLink(dst));
            assertEquals(target.toString(), Files.readSymbolicLink(dst).toString());
        }

        @Test
        void returnsFalseWhenSrcDoesNotExist(@TempDir Path tmp) {
            assertFalse(fileOps.move(tmp.resolve("nope").toString(), tmp.resolve("dst").toString(), null));
        }

        @Test
        void returnsFalseWhenDstAlreadyExists(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("a");
            writeFile(src, "x");
            Path dst = tmp.resolve("b");
            writeFile(dst, "y");

            assertFalse(fileOps.move(src.toString(), dst.toString(), null));
            assertTrue(Files.exists(src));
        }

        @Test
        void throwsWhenSrcIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> fileOps.move("", "dst", null));
        }

        @Test
        void throwsWhenDstIsEmpty(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("a");
            writeFile(src, "x");
            assertThrows(IllegalArgumentException.class, () -> fileOps.move(src.toString(), "", null));
        }

        @Test
        void movesEmptyDirectory(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("d");
            mkdir(src);
            Path dst = tmp.resolve("d.moved");

            assertTrue(fileOps.move(src.toString(), dst.toString(), null));
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

            assertTrue(fileOps.move(src.toString(), dst.toString(), null));
            assertFalse(Files.exists(src));
            assertTrue(Files.isDirectory(dst));
            assertArrayEquals("f".getBytes(), Files.readAllBytes(dst.resolve("f")));
            assertArrayEquals("g".getBytes(), Files.readAllBytes(dst.resolve("sub/g")));
        }

        @Test
        void movesSymlinkToDirectoryAsLinkItself(@TempDir Path tmp) throws IOException {
            // a symlink is moved as the link itself; the target directory is left untouched
            Path target = tmp.resolve("target");
            mkdir(target);
            writeFile(target.resolve("f"), "f");
            Path link = tmp.resolve("link");
            Assumptions.assumeTrue(createSymbolicLink(link, target));
            Path dst = tmp.resolve("link.moved");

            assertTrue(fileOps.move(link.toString(), dst.toString(), null));
            assertFalse(Files.exists(link));
            assertTrue(Files.isSymbolicLink(dst));
            assertTrue(Files.exists(target));
            assertTrue(Files.exists(target.resolve("f")));
        }

        @Test
        void onMoveCalledForAtomicMove(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("d");
            mkdir(src);
            Path dst = tmp.resolve("d.moved");

            List<String> moved = new LinkedList<>();
            FileOps.OnActionListener onAction = (action, from, to, succeeded) -> {
                if (action == FileOps.Action.MOVE && succeeded != null) {
                    moved.add(from + " -> " + to);
                }
            };

            assertTrue(fileOps.move(src.toString(), dst.toString(), onAction));
            assertEquals(1, moved.size());
            // the entry is a directory, reported with a trailing "/" on both sides
            assertEquals(src + "/ -> " + dst + "/", moved.get(0));
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

                assertTrue(fileOps.removeDirectory(rel, true, false, null, null));
                assertFalse(Files.exists(root));
            } finally {
                rm(root);
            }
        }
    }

    // a mutating op reports an action only once, with its outcome; there is no "pre" report
    // (succeeded == null) announcing an action before it runs. MEET in listDirectory is the
    // single exception: discovery has no outcome.
    @Nested
    class reportsOnOutcome {

        @Test
        void noPreReportFromMutatingOps(@TempDir Path tmp) throws IOException {
            Path root = buildTree(tmp.resolve("root"));
            Path dst = tmp.resolve("root.copy");

            List<String> pre = new LinkedList<>();
            FileOps.OnActionListener listener = (action, from, to, succeeded) -> {
                if (succeeded == null) {
                    pre.add(action + ": " + from + (to == null ? "" : " -> " + to));
                }
            };

            assertTrue(fileOps.copyDirectory(root.toString(), dst.toString(), null, listener));
            assertTrue(fileOps.removeDirectory(dst.toString(), true, false, null, listener));
            assertTrue(fileOps.createDirectory(tmp.resolve("p/q").toString(), true, null, listener));

            Path f = tmp.resolve("a.txt");
            writeFile(f, "x");
            assertTrue(fileOps.copyFile(f.toString(), tmp.resolve("a.copy").toString(), true, null, listener));
            assertTrue(fileOps.move(tmp.resolve("a.copy").toString(), tmp.resolve("b.txt").toString(), listener));
            assertTrue(fileOps.removeFile(tmp.resolve("b.txt").toString(), listener));

            assertTrue(pre.isEmpty(), "unexpected pre reports: " + pre);
        }
    }

    // ---- onAction contract (pinned by the fail-fast rework) ----
    // file point ops (copyFile/removeFile/move) report every failed request, even when a
    // precheck rejected it and no syscall ran; directory ops (createDirectory/removeDirectory/
    // copyDirectory) report only mkdir/rmdir attempts - entry precheck failures are silent;
    // abort is silent (the caller watches the flag); a trailing "/" marks a directory path and
    // created directories are reported dst-side (from == null)
    @Nested
    class eventContract {

        // records "ACTION|from|to|ok"
        private FileOps.OnActionListener recorder(List<String> got) {
            return (action, from, to, ok) -> got.add(action + "|" + from + "|" + to + "|" + ok);
        }

        @Test
        void copyFileReportsCreateFalseForEveryFailedRequest(@TempDir Path tmp) throws IOException {
            List<String> got = new LinkedList<>();
            FileOps.OnActionListener listener = recorder(got);

            // missing src
            Path missing = tmp.resolve("missing");
            Path dst1 = tmp.resolve("dst1");
            assertFalse(fileOps.copyFile(missing.toString(), dst1.toString(), true, null, listener));
            assertArrayEquals(new String[] {"CREATE|" + missing + "|" + dst1 + "|false"}, got.toArray());

            // src is a directory
            got.clear();
            Path dir = tmp.resolve("dir");
            mkdir(dir);
            Path dst2 = tmp.resolve("dst2");
            assertFalse(fileOps.copyFile(dir.toString(), dst2.toString(), true, null, listener));
            // copyFile expects a leaf at src, so the decline reports it without "/"
            assertArrayEquals(new String[] {"CREATE|" + dir + "|" + dst2 + "|false"}, got.toArray());

            // dst already exists: rejected, and the pre-existing dst is left untouched
            got.clear();
            Path src = tmp.resolve("src");
            writeFile(src, "x");
            Path dst3 = tmp.resolve("dst3");
            writeFile(dst3, "y");
            assertFalse(fileOps.copyFile(src.toString(), dst3.toString(), true, null, listener));
            assertArrayEquals(new String[] {"CREATE|" + src + "|" + dst3 + "|false"}, got.toArray());
            assertArrayEquals("y".getBytes(), Files.readAllBytes(dst3));
        }

        @Test
        void copyFileReportsCreateTrueOnSuccess(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("src");
            writeFile(src, "x");
            Path dst = tmp.resolve("dst");
            List<String> got = new LinkedList<>();
            FileOps.OnActionListener listener = recorder(got);

            assertTrue(fileOps.copyFile(src.toString(), dst.toString(), true, null, listener));
            assertArrayEquals(new String[] {"CREATE|" + src + "|" + dst + "|true"}, got.toArray());
        }

        @Test
        void copyFileSilentWhenAbortPreset(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("src");
            writeFile(src, "x");
            Path dst = tmp.resolve("dst");
            List<String> got = new LinkedList<>();
            FileOps.OnActionListener listener = recorder(got);

            assertFalse(fileOps.copyFile(src.toString(), dst.toString(), true, new AtomicBoolean(true), listener));
            assertTrue(got.isEmpty());
            assertFalse(Files.exists(dst));
        }

        @Test
        void copyFileDeclinesDanglingSymlinkWhenFollowingWithNonNullFrom(@TempDir Path tmp) throws IOException {
            // follow=true on a broken link: declined, and the event still names the src
            Path link = tmp.resolve("link");
            Assumptions.assumeTrue(createSymbolicLink(link, tmp.resolve("missing")));
            Path dst = tmp.resolve("dst");
            List<String> got = new LinkedList<>();
            FileOps.OnActionListener listener = recorder(got);

            assertFalse(fileOps.copyFile(link.toString(), dst.toString(), true, null, listener));
            assertArrayEquals(new String[] {"CREATE|" + link + "|" + dst + "|false"}, got.toArray());
            assertFalse(Files.exists(dst));
        }

        @Test
        void removeFileReportsRemoveFalseOnPrecheckFailures(@TempDir Path tmp) throws IOException {
            List<String> got = new LinkedList<>();
            FileOps.OnActionListener listener = recorder(got);

            // on a directory: removeFile expects a leaf, so the decline reports it without "/"
            Path dir = tmp.resolve("dir");
            mkdir(dir);
            assertFalse(fileOps.removeFile(dir.toString(), listener));
            assertArrayEquals(new String[] {"REMOVE|" + dir + "|null|false"}, got.toArray());

            // on a missing path
            got.clear();
            Path missing = tmp.resolve("missing");
            assertFalse(fileOps.removeFile(missing.toString(), listener));
            assertArrayEquals(new String[] {"REMOVE|" + missing + "|null|false"}, got.toArray());
        }

        @Test
        void removeFileReportsRemoveTrueOnSuccess(@TempDir Path tmp) throws IOException {
            Path f = tmp.resolve("f");
            writeFile(f, "x");
            List<String> got = new LinkedList<>();
            FileOps.OnActionListener listener = recorder(got);

            assertTrue(fileOps.removeFile(f.toString(), listener));
            assertArrayEquals(new String[] {"REMOVE|" + f + "|null|true"}, got.toArray());
        }

        @Test
        void moveReportsMoveFalseOnMissingSrc(@TempDir Path tmp) {
            Path src = tmp.resolve("missing");
            Path dst = tmp.resolve("dst");
            List<String> got = new LinkedList<>();
            FileOps.OnActionListener listener = recorder(got);

            assertFalse(fileOps.move(src.toString(), dst.toString(), listener));
            assertArrayEquals(new String[] {"MOVE|" + src + "|" + dst + "|false"}, got.toArray());
        }

        @Test
        void createDirectoryReportsOnlyMkdirAttempts(@TempDir Path tmp) throws IOException {
            List<String> got = new LinkedList<>();
            FileOps.OnActionListener listener = recorder(got);

            // already exists: declined, one false event naming the existing directory
            Path d = tmp.resolve("d");
            mkdir(d);
            assertFalse(fileOps.createDirectory(d.toString(), false, null, listener));
            assertArrayEquals(new String[] {"CREATE|null|" + d + "/|false"}, got.toArray());

            // success: one dst-side event with a trailing "/"
            got.clear();
            Path d2 = tmp.resolve("d2");
            assertTrue(fileOps.createDirectory(d2.toString(), false, null, listener));
            assertArrayEquals(new String[] {"CREATE|null|" + d2 + "/|true"}, got.toArray());

            // parent missing (parents=false): mkdir attempted and failed -> reported
            got.clear();
            Path deep = tmp.resolve("missing/deep");
            assertFalse(fileOps.createDirectory(deep.toString(), false, null, listener));
            assertArrayEquals(new String[] {"CREATE|null|" + deep + "/|false"}, got.toArray());
        }

        @Test
        void createDirectorySilentWhenAbortPreset(@TempDir Path tmp) {
            List<String> got = new LinkedList<>();
            FileOps.OnActionListener listener = recorder(got);

            assertFalse(fileOps.createDirectory(tmp.resolve("d").toString(), false, new AtomicBoolean(true), listener));
            assertTrue(got.isEmpty());
        }

        @Test
        void removeDirectoryReportsOnlyRmdirAttempts(@TempDir Path tmp) throws IOException {
            List<String> got = new LinkedList<>();
            FileOps.OnActionListener listener = recorder(got);

            // not a directory: removeDirectory expects a directory, so the decline reports it with "/"
            Path f = tmp.resolve("f");
            writeFile(f, "x");
            assertFalse(fileOps.removeDirectory(f.toString(), true, false, null, listener));
            assertArrayEquals(new String[] {"REMOVE|" + f + "/|null|false"}, got.toArray());

            // empty directory, not recursive: rmdir fired and succeeded
            got.clear();
            Path empty = tmp.resolve("empty");
            mkdir(empty);
            assertTrue(fileOps.removeDirectory(empty.toString(), false, false, null, listener));
            assertArrayEquals(new String[] {"REMOVE|" + empty + "/|null|true"}, got.toArray());

            // non-empty, not recursive: rmdir fired but failed; the directory survives
            got.clear();
            Path nonEmpty = tmp.resolve("nonempty");
            mkdir(nonEmpty);
            writeFile(nonEmpty.resolve("a"), "a");
            assertFalse(fileOps.removeDirectory(nonEmpty.toString(), false, false, null, listener));
            assertArrayEquals(new String[] {"REMOVE|" + nonEmpty + "/|null|false"}, got.toArray());
            assertTrue(Files.exists(nonEmpty));
        }

        @Test
        void removeDirectoryStopsWhenAbortSetFromListener(@TempDir Path tmp) throws IOException {
            // the first REMOVE event sets the abort flag; fail-fast stops the walk before the rmdir
            Path d = tmp.resolve("d");
            mkdir(d);
            writeFile(d.resolve("a"), "a");
            writeFile(d.resolve("b"), "b");
            AtomicBoolean abort = new AtomicBoolean(false);
            List<String> got = new LinkedList<>();
            FileOps.OnActionListener listener = (action, from, to, ok) -> {
                got.add(action + "|" + from + "|" + to + "|" + ok);
                if (action == FileOps.Action.REMOVE) {
                    abort.set(true);
                }
            };

            assertFalse(fileOps.removeDirectory(d.toString(), true, false, abort, listener));
            // the directory listing was reported, then only the first leaf removal ran;
            // the rmdir of d never fired
            assertArrayEquals(new String[] {
                    "LIST|" + d + "/|null|true",
                    "REMOVE|" + d.resolve("a") + "|null|true"
            }, got.toArray());
            assertTrue(Files.exists(d));
            assertFalse(Files.exists(d.resolve("a")));
            assertTrue(Files.exists(d.resolve("b")));
        }

        @Test
        void copyDirectoryReportsPrecheckFailures(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("src");
            mkdir(src);
            writeFile(src.resolve("f"), "f");
            List<String> got = new LinkedList<>();
            FileOps.OnActionListener listener = recorder(got);

            // dst already exists: declined, one false event naming the existing directory
            Path dst = tmp.resolve("dst");
            mkdir(dst);
            assertFalse(fileOps.copyDirectory(src.toString(), dst.toString(), null, listener));
            assertArrayEquals(new String[] {"CREATE|null|" + dst + "/|false"}, got.toArray());

            // src is not a directory: declined, one false event naming the expected dst directory
            got.clear();
            Path f = tmp.resolve("f");
            writeFile(f, "x");
            Path dst2 = tmp.resolve("dst2");
            assertFalse(fileOps.copyDirectory(f.toString(), dst2.toString(), null, listener));
            assertArrayEquals(new String[] {"CREATE|null|" + dst2 + "/|false"}, got.toArray());
        }

        @Test
        void copyDirectorySilentWhenAbortPreset(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("src");
            mkdir(src);
            writeFile(src.resolve("f"), "f");
            List<String> got = new LinkedList<>();
            FileOps.OnActionListener listener = recorder(got);

            assertFalse(fileOps.copyDirectory(src.toString(), tmp.resolve("dst").toString(),
                    new AtomicBoolean(true), listener));
            assertTrue(got.isEmpty());
            assertFalse(Files.exists(tmp.resolve("dst")));
        }

        @Test
        void copyDirectoryReportsDirectoriesDstSideWithSlash(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("src");
            mkdir(src.resolve("sub"));
            writeFile(src.resolve("f.txt"), "f");
            Path dst = tmp.resolve("src.copy");
            List<String> got = new LinkedList<>();
            FileOps.OnActionListener listener = recorder(got);

            assertTrue(fileOps.copyDirectory(src.toString(), dst.toString(), null, listener));
            // mkdir events are dst-side (from == null) with a trailing "/";
            // each source directory read is reported as LIST (succeeded = readable);
            // leaf copies carry both sides without "/"
            assertArrayEquals(new String[] {
                    "CREATE|null|" + dst + "/|true",
                    "LIST|" + src + "/|null|true",
                    "CREATE|null|" + dst + "/sub/|true",
                    "LIST|" + src + "/sub/|null|true",
                    "CREATE|" + src.resolve("f.txt") + "|" + dst.resolve("f.txt") + "|true"
            }, got.toArray());
        }

        @Test
        void copyDirectoryCopiesBrokenSymlinkChildAsLink(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("src");
            mkdir(src);
            Path link = src.resolve("lk");
            Assumptions.assumeTrue(createSymbolicLink(link, src.resolve("missing")));
            Path dst = tmp.resolve("src.copy");

            assertTrue(fileOps.copyDirectory(src.toString(), dst.toString(), null, null));
            assertTrue(Files.isSymbolicLink(dst.resolve("lk")));
            assertFalse(Files.exists(dst.resolve("lk")));
        }

        @Test
        void copyDirectoryCopiesSymlinkToFileChildAsLink(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("src");
            mkdir(src);
            Path target = tmp.resolve("target");
            writeFile(target, "content");
            Path link = src.resolve("lk");
            Assumptions.assumeTrue(createSymbolicLink(link, target));
            Path dst = tmp.resolve("src.copy");

            assertTrue(fileOps.copyDirectory(src.toString(), dst.toString(), null, null));
            assertTrue(Files.isSymbolicLink(dst.resolve("lk")));
            assertEquals(target.toString(), Files.readSymbolicLink(dst.resolve("lk")).toString());
        }
    }
}
