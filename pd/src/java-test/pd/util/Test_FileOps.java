package pd.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    }

    @Nested
    class listDirectory {

        @Test
        void listsOneLevelDeep(@TempDir Path tmp) throws IOException {
            Path root = buildTree(tmp.resolve("root"));

            List<String> result = fileOps.listDirectory(root.resolve("docs").toString(), 1, null);

            assertEquals(2, result.size());
            assertEquals(root.resolve("docs/readme.md").toString(), result.get(0));
            assertEquals(root.resolve("docs/img").toString() + "/", result.get(1));
        }

        @Test
        void listsTwoLevelsDeep(@TempDir Path tmp) throws IOException {
            Path root = buildTree(tmp.resolve("root"));

            List<String> result = fileOps.listDirectory(root.resolve("docs").toString(), 2, null);

            assertEquals(3, result.size());
            assertEquals(root.resolve("docs/readme.md").toString(), result.get(0));
            assertEquals(root.resolve("docs/img").toString() + "/", result.get(1));
            assertEquals(root.resolve("docs/img/a.png").toString(), result.get(2));
        }

        @Test
        void returnsEmptyForEmptyDirectory(@TempDir Path tmp) throws IOException {
            Path root = buildTree(tmp.resolve("root"));

            List<String> result = fileOps.listDirectory(root.resolve("empty").toString(), 1, null);

            assertTrue(result.isEmpty());
        }

        @Test
        void returnsEmptyWhenDepthIsZero(@TempDir Path tmp) throws IOException {
            Path root = buildTree(tmp.resolve("root"));

            List<String> result = fileOps.listDirectory(root.resolve("docs").toString(), 0, null);

            assertTrue(result.isEmpty());
        }

        @Test
        void returnsNullWhenDirectoryDoesNotExist(@TempDir Path tmp) {
            List<String> result = fileOps.listDirectory(tmp.resolve("nope").toString(), 1, null);

            assertNull(result);
        }

        @Test
        void returnsNullWhenAbortAlreadyRequested(@TempDir Path tmp) throws IOException {
            Path root = buildTree(tmp.resolve("root"));

            List<String> result = fileOps.listDirectory(root.resolve("docs").toString(), 1, new AtomicBoolean(true));

            assertNull(result);
        }

        @Test
        void throwsWhenDirectoryIsEmpty() {
            // empty string is rejected; callers must pass "." for the current directory
            assertThrows(IllegalArgumentException.class, () -> fileOps.listDirectory("", 1, null));
        }
    }

    @Nested
    class listDirectorySingleArg {

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
    class copyRecursively {

        @Test
        void copiesSingleFile(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve(".gitignore");
            writeFile(src, "git");
            Path dst = tmp.resolve(".gitignore.copy");

            assertTrue(fileOps.copyRecursively(src.toString(), dst.toString(), null));
            assertArrayEquals("git".getBytes(), Files.readAllBytes(dst));
        }

        @Test
        void copiesDirectoryTree(@TempDir Path tmp) throws IOException {
            Path src = buildTree(tmp.resolve("root"));
            Path dst = tmp.resolve("root.copy");

            assertTrue(fileOps.copyRecursively(src.toString(), dst.toString(), null));

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

            assertTrue(fileOps.copyRecursively(src.toString(), dst.toString(), null));
            assertTrue(Files.isDirectory(dst));
            assertFalse(Files.list(dst).findAny().isPresent());
        }

        @Test
        void returnsFalseWhenSrcDoesNotExist(@TempDir Path tmp) {
            Path dst = tmp.resolve("dst");

            assertFalse(fileOps.copyRecursively(tmp.resolve("nope").toString(), dst.toString(), null));
            assertFalse(Files.exists(dst));
        }

        @Test
        void returnsFalseWhenDstAlreadyExists(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("a");
            writeFile(src, "x");
            Path dst = tmp.resolve("b");
            Files.createFile(dst);

            assertFalse(fileOps.copyRecursively(src.toString(), dst.toString(), null));
        }

        @Test
        void returnsFalseWhenAbortAlreadyRequested(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("a");
            writeFile(src, "x");
            Path dst = tmp.resolve("b");

            assertFalse(fileOps.copyRecursively(src.toString(), dst.toString(), new AtomicBoolean(true)));
            assertFalse(Files.exists(dst));
        }

        @Test
        void returnsFalseWhenDstParentDoesNotExist(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("d");
            mkdir(src);
            writeFile(src.resolve("f"), "f");
            // dst's parent does not exist; Files.createDirectory(dst) throws -> false
            Path dst = tmp.resolve("missing").resolve("dst");

            assertFalse(fileOps.copyRecursively(src.toString(), dst.toString(), null));
        }

        @Test
        void throwsWhenSrcIsEmpty(@TempDir Path tmp) throws IOException {
            Path dst = tmp.resolve("b");
            assertThrows(IllegalArgumentException.class, () -> fileOps.copyRecursively("", dst.toString(), null));
        }

        @Test
        void throwsWhenDstIsEmpty(@TempDir Path tmp) throws IOException {
            Path src = tmp.resolve("a");
            writeFile(src, "x");
            assertThrows(IllegalArgumentException.class, () -> fileOps.copyRecursively(src.toString(), "", null));
        }
    }

    @Nested
    class removeRecursively {

        @Test
        void removesDirectoryTree(@TempDir Path tmp) throws IOException {
            Path root = buildTree(tmp.resolve("root"));

            assertTrue(fileOps.removeRecursively(root.toString(), null));
            assertFalse(Files.exists(root));
            assertFalse(Files.exists(root.resolve("docs/img/a.png")));
        }

        @Test
        void removesSingleFile(@TempDir Path tmp) throws IOException {
            Path f = tmp.resolve("a");
            writeFile(f, "x");

            assertTrue(fileOps.removeRecursively(f.toString(), null));
            assertFalse(Files.exists(f));
        }

        @Test
        void removesEmptyDirectory(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("empty");
            mkdir(d);

            assertTrue(fileOps.removeRecursively(d.toString(), null));
            assertFalse(Files.exists(d));
        }

        @Test
        void returnsFalseWhenTargetDoesNotExist(@TempDir Path tmp) {
            assertFalse(fileOps.removeRecursively(tmp.resolve("nope").toString(), null));
        }

        @Test
        void returnsFalseWhenAbortAlreadyRequested(@TempDir Path tmp) throws IOException {
            Path f = tmp.resolve("a");
            writeFile(f, "x");

            assertFalse(fileOps.removeRecursively(f.toString(), new AtomicBoolean(true)));
            assertTrue(Files.exists(f));
        }

        @Test
        void throwsWhenDirectoryIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> fileOps.removeRecursively("", null));
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
        void removesEmptyDirectory(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("empty");
            mkdir(d);

            assertTrue(fileOps.removeFile(d.toString()));
            assertFalse(Files.exists(d));
        }

        @Test
        void returnsFalseForNonEmptyDirectory(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("d");
            mkdir(d);
            writeFile(d.resolve("f"), "f");

            assertFalse(fileOps.removeFile(d.toString()));
            // the directory and its content remain
            assertTrue(Files.exists(d));
            assertTrue(Files.exists(d.resolve("f")));
        }

        @Test
        void throwsWhenPathIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> fileOps.removeFile(""));
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
        void throwsWhenPathIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> fileOps.createDirectory("", false));
        }
    }

    @Nested
    class removeDirectory {

        @Test
        void removesEmptyDirectoryWhenNotRecursive(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("d");
            mkdir(d);

            assertTrue(fileOps.removeDirectory(d.toString(), false, false));
            assertFalse(Files.exists(d));
        }

        @Test
        void returnsFalseForNonEmptyWhenNotRecursive(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("d");
            mkdir(d);
            writeFile(d.resolve("f"), "f");

            assertFalse(fileOps.removeDirectory(d.toString(), false, false));
            assertTrue(Files.exists(d));
            assertTrue(Files.exists(d.resolve("f")));
        }

        @Test
        void removesTreeWhenRecursive(@TempDir Path tmp) throws IOException {
            Path d = tmp.resolve("d");
            mkdir(d.resolve("sub"));
            writeFile(d.resolve("f"), "f");
            writeFile(d.resolve("sub/g"), "g");

            assertTrue(fileOps.removeDirectory(d.toString(), true, false));
            assertFalse(Files.exists(d));
        }

        @Test
        void returnsFalseWhenTargetDoesNotExist(@TempDir Path tmp) {
            assertFalse(fileOps.removeDirectory(tmp.resolve("nope").toString(), false, false));
        }

        @Test
        void removesEmptyAncestorsWhenParentsTrue(@TempDir Path tmp) throws IOException {
            Path leaf = tmp.resolve("p/q/r");
            mkdir(leaf);

            assertTrue(fileOps.removeDirectory(leaf.toString(), false, true));
            assertFalse(Files.exists(tmp.resolve("p")));
        }

        @Test
        void stopsAtNonEmptyAncestorWhenParentsTrue(@TempDir Path tmp) throws IOException {
            // p holds a file, so it must survive removing the empty p/q/r chain
            Path leaf = tmp.resolve("p/q/r");
            mkdir(leaf);
            writeFile(tmp.resolve("p/keep"), "k");

            assertTrue(fileOps.removeDirectory(leaf.toString(), false, true));
            assertTrue(Files.exists(tmp.resolve("p")));
            assertFalse(Files.exists(tmp.resolve("p/q")));
            assertTrue(Files.exists(tmp.resolve("p/keep")));
        }

        @Test
        void throwsWhenPathIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> fileOps.removeDirectory("", false, false));
        }
    }

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
        void copyRecursivelyCopiesTree() throws IOException {
            String rel = uniqueRel();
            Path root = Paths.get(rel);
            try {
                Files.createDirectories(root.resolve("d"));
                Files.write(root.resolve("d/f"), "f".getBytes());
                Files.write(root.resolve("top"), "t".getBytes());

                assertTrue(fileOps.copyRecursively(rel, rel + ".copy", null));
                assertTrue(Files.exists(Paths.get(rel + ".copy/d/f")));
                assertTrue(Files.exists(Paths.get(rel + ".copy/top")));
                assertArrayEquals("f".getBytes(), Files.readAllBytes(Paths.get(rel + ".copy/d/f")));
            } finally {
                rm(root);
                rm(Paths.get(rel + ".copy"));
            }
        }

        @Test
        void removeRecursivelyRemovesTree() throws IOException {
            String rel = uniqueRel();
            Path root = Paths.get(rel);
            try {
                Files.createDirectories(root.resolve("d"));
                Files.write(root.resolve("d/f"), "f".getBytes());

                assertTrue(fileOps.removeRecursively(rel, null));
                assertFalse(Files.exists(root));
            } finally {
                rm(root);
            }
        }
    }
}
