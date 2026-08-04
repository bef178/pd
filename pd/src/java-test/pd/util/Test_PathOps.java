package pd.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Test_PathOps {

    private final PathOps pathOps = PathOps.singleton;

    @Nested
    class basename {

        @Test
        void returnsLastSegment() {
            assertEquals("file", pathOps.basename("file"));
            assertEquals("file", pathOps.basename("a/b/file"));
            assertEquals("file", pathOps.basename("/a/b/file"));
            assertEquals("lib", pathOps.basename("/usr/lib"));
        }

        @Test
        void dropsTrailingSlashes() {
            assertEquals("file", pathOps.basename("file///"));
            assertEquals("file", pathOps.basename("a/b/file///"));
        }

        @Test
        void returnsSlashForRootSlashes() {
            assertEquals("/", pathOps.basename("/"));
            assertEquals("/", pathOps.basename("////"));
        }

        @Test
        void returnsDotForDotSegment() {
            assertEquals(".", pathOps.basename("."));
            assertEquals(".", pathOps.basename("abc//."));
            assertEquals(".", pathOps.basename("//.///"));
        }

        @Test
        void keepsDoubleDotSegment() {
            assertEquals("..", pathOps.basename(".."));
        }

        @Test
        void keepsLeadingDotName() {
            assertEquals(".hidden", pathOps.basename("/x/.hidden"));
        }

        @Test
        void keepsTrailingDotName() {
            assertEquals("a.", pathOps.basename("a."));
        }

        @Test
        void keepsMultiDotName() {
            assertEquals("....", pathOps.basename("...."));
        }

        @Test
        void keepsTripleDotName() {
            assertEquals("...", pathOps.basename("..."));
            assertEquals("...", pathOps.basename("/a/b/..."));
        }

        @Test
        void singleArgEqualsNoSuffix() {
            assertEquals("c.d", pathOps.basename("a/b/c.d"));
            assertEquals("c.d", pathOps.basename("a/b/c.d", null));
            assertEquals("c.d", pathOps.basename("a/b/c.d", ""));
        }

        @Test
        void removesMatchingSuffix() {
            assertEquals("c", pathOps.basename("a/b/c.d", ".d"));
            assertEquals("name", pathOps.basename("name.txt", ".txt"));
            assertEquals("a", pathOps.basename("a.tar.gz", ".tar.gz"));
            assertEquals("c", pathOps.basename("a/b/c.txt////", ".txt"));
        }

        @Test
        void removesOnlyTrailingSuffixOccurrence() {
            assertEquals("a.b", pathOps.basename("a.b.b", ".b"));
        }

        @Test
        void keepsSuffixAtSegmentStart() {
            assertEquals("file", pathOps.basename("file", "file"));
            assertEquals("c.d", pathOps.basename("a/b/c.d", "c.d"));
        }

        @Test
        void ignoresSuffixInDirectoryPart() {
            assertEquals("c", pathOps.basename("a.b/c", ".b"));
        }

        @Test
        void leavesBasenameWhenSuffixUnmatched() {
            assertEquals("file", pathOps.basename("file", ".txt"));
            assertEquals("c", pathOps.basename("a/b/c", ".d"));
            assertEquals("abc", pathOps.basename("abc", "abcd"));
        }

        @Test
        void throwsOnEmptyPath() {
            assertThrows(IllegalArgumentException.class, () -> pathOps.basename(""));
            assertThrows(IllegalArgumentException.class, () -> pathOps.basename("", ".x"));
        }

        @Test
        void throwsNpeOnNullPath() {
            assertThrows(NullPointerException.class, () -> pathOps.basename(null));
            assertThrows(NullPointerException.class, () -> pathOps.basename(null, null));
            assertThrows(NullPointerException.class, () -> pathOps.basename(null, ".x"));
        }
    }

    @Nested
    class dirname {

        @Test
        void returnsEmptyForNoDirectory() {
            assertEquals("", pathOps.dirname("abc"));
        }

        @Test
        void returnsDirectoryBeforeLastSlash() {
            assertEquals("/usr", pathOps.dirname("/usr/lib"));
            assertEquals("a/b", pathOps.dirname("a/b/c"));
            assertEquals("/a/b", pathOps.dirname("/a/b/c"));
        }

        @Test
        void returnsRootSlashForRootPath() {
            assertEquals("/", pathOps.dirname("/usr"));
            assertEquals("/", pathOps.dirname("/usr/"));
        }

        @Test
        void returnsSlashForAllSlashes() {
            assertEquals("/", pathOps.dirname("/"));
            assertEquals("/", pathOps.dirname("////"));
        }

        @Test
        void returnsSlashWhenLastSlashIsRoot() {
            assertEquals("/", pathOps.dirname("/."));
            assertEquals("/", pathOps.dirname("/.."));
            assertEquals("/", pathOps.dirname("/a/"));
        }

        @Test
        void dropsTrailingSlashes() {
            assertEquals("", pathOps.dirname("abc/"));
            assertEquals("", pathOps.dirname("abc///"));
            assertEquals("a", pathOps.dirname("a/b//"));
        }

        @Test
        void returnsEmptyForDotSegment() {
            assertEquals("", pathOps.dirname("."));
            assertEquals("", pathOps.dirname(".."));
            assertEquals("", pathOps.dirname("./"));
        }

        @Test
        void doesNotResolveDotDotAsSegment() {
            assertEquals("abc/..", pathOps.dirname("abc/../def"));
        }

        @Test
        void keepsIntermediateSlashesIntact() {
            assertEquals("a", pathOps.dirname("a//b"));
            assertEquals("a///b", pathOps.dirname("a///b//c"));
            assertEquals("abc", pathOps.dirname("abc//.///"));
        }

        @Test
        void throwsOnEmptyPath() {
            assertThrows(IllegalArgumentException.class, () -> pathOps.dirname(""));
        }

        @Test
        void throwsNpeOnNullPath() {
            assertThrows(NullPointerException.class, () -> pathOps.dirname(null));
        }
    }

    @Nested
    class extname {

        @Test
        void returnsExtensionAfterFirstDotInLastSegment() {
            assertEquals(".cd", pathOps.extname("ab.cd"));
            assertEquals(".b.c", pathOps.extname("a.b.c"));
            assertEquals(".txt", pathOps.extname("/tmp/a.b/c.txt"));
        }

        @Test
        void returnsEmptyForNoExtension() {
            assertEquals("", pathOps.extname("a"));
            assertEquals("", pathOps.extname("/tmp/a"));
            assertEquals("", pathOps.extname("/tmp/a.b/c"));
        }

        @Test
        void returnsEmptyForAllSlashes() {
            assertEquals("", pathOps.extname("/"));
            assertEquals("", pathOps.extname("////"));
        }

        @Test
        void ignoresTrailingSlashes() {
            assertEquals(".txt", pathOps.extname("/tmp/a.b/c.txt////"));
        }

        @Test
        void leadingDotIsNotExtension() {
            assertEquals("", pathOps.extname(".a"));
            assertEquals("", pathOps.extname("/tmp/.a"));
        }

        @Test
        void returnsDotForTrailingDot() {
            assertEquals(".", pathOps.extname("/tmp/a."));
            assertEquals(".", pathOps.extname("/x/y."));
        }

        @Test
        void keepsLeadingDotAfterSkippedOnes() {
            assertEquals(".b", pathOps.extname(".a.b"));
            assertEquals(".z", pathOps.extname("/x/.y.z"));
        }

        @Test
        void keepsConsecutiveDotsInExtension() {
            assertEquals("..b", pathOps.extname("a..b"));
            assertEquals("..", pathOps.extname("/x/y.."));
        }

        @Test
        void keepsTrailingDotInExtension() {
            assertEquals(".b.", pathOps.extname("a.b."));
        }

        @Test
        void keepsTripleDotInExtension() {
            assertEquals("...", pathOps.extname("a..."));
            assertEquals("...", pathOps.extname("/a/b..."));
        }

        @Test
        void throwsOnEmptyPath() {
            assertThrows(IllegalArgumentException.class, () -> pathOps.extname(""));
        }

        @Test
        void throwsNpeOnNullPath() {
            assertThrows(NullPointerException.class, () -> pathOps.extname(null));
        }
    }

    @Nested
    class isAbsolutePath {

        @Test
        void returnsTrueForLeadingSlash() {
            assertTrue(pathOps.isAbsolutePath("/usr"));
            assertTrue(pathOps.isAbsolutePath("/"));
        }

        @Test
        void returnsFalseForRelative() {
            assertFalse(pathOps.isAbsolutePath("usr"));
            assertFalse(pathOps.isAbsolutePath("usr/bin"));
        }

        @Test
        void throwsOnEmptyPath() {
            assertThrows(IllegalArgumentException.class, () -> pathOps.isAbsolutePath(""));
        }

        @Test
        void throwsNpeOnNullPath() {
            assertThrows(NullPointerException.class, () -> pathOps.isAbsolutePath(null));
        }
    }

    @Nested
    class join {

        @Test
        void concatenatesWithSlash() {
            assertEquals("foo/bar/baz", pathOps.join("foo", "bar", "baz"));
        }

        @Test
        void doesNotTrimTrailingSlashOfHead() {
            assertEquals("foo//bar", pathOps.join("foo/", "bar"));
            assertEquals("//a", pathOps.join("/", "a"));
        }

        @Test
        void emptyElementAddsSlash() {
            assertEquals("foo//bar", pathOps.join("foo", "", "bar"));
        }

        @Test
        void returnsPathWhenNoMore() {
            assertEquals("foo", pathOps.join("foo"));
        }

        @Test
        void throwsOnEmptyPath() {
            assertThrows(IllegalArgumentException.class, () -> pathOps.join(""));
        }

        @Test
        void throwsNpeOnNullPath() {
            assertThrows(NullPointerException.class, () -> pathOps.join(null));
        }
    }

    @Nested
    class normalize {

        @Test
        void keepsPlainPath() {
            assertEquals("abc", pathOps.normalize("abc"));
            assertEquals("x/y/z", pathOps.normalize("x/y/./z"));
        }

        @Test
        void removesSingleDotSegment() {
            assertEquals("x", pathOps.normalize("x/."));
            assertEquals("x", pathOps.normalize("./x"));
        }

        @Test
        void returnsDotForCurrentDirectory() {
            // a relative path that collapses to nothing denotes the current directory
            assertEquals(".", pathOps.normalize("."));
            assertEquals(".", pathOps.normalize("././."));
            assertEquals(".", pathOps.normalize("./."));
            assertEquals(".", pathOps.normalize("x/.."));
            assertEquals(".", pathOps.normalize("x/y/../.."));
        }

        @Test
        void resolvesRelativeDotDot() {
            assertEquals("x/z", pathOps.normalize("x/y/../z"));
        }

        @Test
        void keepsLeadingDotDot() {
            assertEquals("../abc", pathOps.normalize("./../abc"));
            assertEquals("../x", pathOps.normalize("../x"));
            assertEquals("..", pathOps.normalize(".."));
        }

        @Test
        void collapsesDeepRedundantSegments() {
            assertEquals("../../..", pathOps.normalize("../.././../abc/.."));
        }

        @Test
        void returnsSlashWhenDotDotLandsAtRoot() {
            assertEquals("/", pathOps.normalize("/x/y/../.."));
            assertEquals("/", pathOps.normalize("/x/.."));
            assertEquals("/c", pathOps.normalize("/a/b/../../c"));
        }

        @Test
        void throwsWhenDotDotGoesBeyondRoot() {
            assertThrows(IllegalArgumentException.class, () -> pathOps.normalize("/.."));
            assertThrows(IllegalArgumentException.class, () -> pathOps.normalize("/x/../.."));
            assertThrows(IllegalArgumentException.class, () -> pathOps.normalize("/../abc"));
        }

        @Test
        void collapsesRepeatedSlashes() {
            assertEquals("a/b/c", pathOps.normalize("a//b//c"));
            assertEquals("/a/b", pathOps.normalize("/a//b"));
        }

        @Test
        void returnsSlashForRoot() {
            assertEquals("/", pathOps.normalize("/"));
            assertEquals("/", pathOps.normalize("//"));
            assertEquals("/", pathOps.normalize("///"));
        }

        @Test
        void keepsTripleDotName() {
            assertEquals("...", pathOps.normalize("..."));
            assertEquals("/...", pathOps.normalize("/..."));
        }

        @Test
        void throwsOnEmptyPath() {
            assertThrows(IllegalArgumentException.class, () -> pathOps.normalize(""));
        }

        @Test
        void throwsNpeOnNullPath() {
            assertThrows(NullPointerException.class, () -> pathOps.normalize(null));
        }
    }

    @Nested
    class relativize {

        @Test
        void returnsDescendantPath() {
            assertEquals("c", pathOps.relativize("/a/b", "/a/b/c"));
            assertEquals("b", pathOps.relativize("a", "a/b"));
        }

        @Test
        void returnsDotForEqualPaths() {
            assertEquals(".", pathOps.relativize("/a", "/a"));
            assertEquals(".", pathOps.relativize("a", "a"));
            assertEquals(".", pathOps.relativize("/", "/"));
        }

        @Test
        void relativizesFromRoot() {
            assertEquals("a", pathOps.relativize("/", "/a"));
            assertEquals("..", pathOps.relativize("/a", "/"));
        }

        @Test
        void returnsParentForAncestor() {
            assertEquals("..", pathOps.relativize("/a/b/c", "/a/b"));
        }

        @Test
        void addsDotDotForDivergence() {
            assertEquals("../c", pathOps.relativize("/a/b", "/a/c"));
            assertEquals("../b/c", pathOps.relativize("/a", "/b/c"));
            assertEquals("../b/c", pathOps.relativize("a", "b/c"));
        }

        @Test
        void throwsWhenAbsoluteAndRelativeMixed() {
            assertThrows(IllegalArgumentException.class, () -> pathOps.relativize("/a", "b"));
            assertThrows(IllegalArgumentException.class, () -> pathOps.relativize("a", "/b"));
        }

        @Test
        void throwsWhenArgumentGoesBeyondRoot() {
            assertThrows(IllegalArgumentException.class, () -> pathOps.relativize("/a/../..", "/x"));
            assertThrows(IllegalArgumentException.class, () -> pathOps.relativize("/x", "/a/../.."));
        }

        @Test
        void throwsOnEmptyFrom() {
            assertThrows(IllegalArgumentException.class, () -> pathOps.relativize("", "x"));
        }

        @Test
        void throwsOnEmptyTo() {
            assertThrows(IllegalArgumentException.class, () -> pathOps.relativize("x", ""));
        }

        @Test
        void throwsNpeOnNullFrom() {
            assertThrows(NullPointerException.class, () -> pathOps.relativize(null, "x"));
        }

        @Test
        void throwsNpeOnNullTo() {
            assertThrows(NullPointerException.class, () -> pathOps.relativize("x", null));
        }
    }

    @Nested
    class resolve {

        @Test
        void appendsRelativeToPath() {
            assertEquals("a/b/c", pathOps.resolve("a", "b", "c"));
            assertEquals("a/b/c", pathOps.resolve("a/b", "c"));
            assertEquals("/a/b", pathOps.resolve("/a", "b"));
        }

        @Test
        void absoluteArgOverrides() {
            assertEquals("/b", pathOps.resolve("a", "/b"));
            assertEquals("/b", pathOps.resolve("a/", "/b"));
        }

        @Test
        void normalizesResult() {
            assertEquals("a/b/c", pathOps.resolve("a//b", "c"));
            assertEquals("a/b", pathOps.resolve("a", "./b"));
            assertEquals("b", pathOps.resolve("a", "../b"));
        }

        @Test
        void throwsWhenDotDotGoesBeyondRoot() {
            assertThrows(IllegalArgumentException.class, () -> pathOps.resolve("/", ".."));
            assertThrows(IllegalArgumentException.class, () -> pathOps.resolve("/a", "../.."));
        }

        @Test
        void skipsEmptyArgs() {
            assertEquals("a", pathOps.resolve("a", ""));
        }

        @Test
        void returnsPathWhenNoMore() {
            assertEquals("a", pathOps.resolve("a"));
        }

        @Test
        void throwsOnEmptyPath() {
            assertThrows(IllegalArgumentException.class, () -> pathOps.resolve(""));
        }

        @Test
        void throwsNpeOnNullPath() {
            assertThrows(NullPointerException.class, () -> pathOps.resolve(null));
        }
    }

    @Nested
    class compare {

        @Test
        void returnsZeroForEqual() {
            assertEquals(0, pathOps.compare("xyz/xyz", "xyz/xyz"));
            assertEquals(0, pathOps.compare("a.b", "a.b"));
            assertEquals(0, pathOps.compare("a", "a"));
        }

        @Test
        void comparesByCodePointAtFirstDifference() {
            assertEquals(-1, pathOps.compare("ba", "ca"));
            assertEquals(1, pathOps.compare("ca", "ba"));
        }

        @Test
        void comparesSupplementaryCodePointsByValue() {
            // U+00E9 (é = 233) > U+007A (z = 122), so "café" > "cafz"
            assertEquals(1, pathOps.compare("café", "cafz"));
        }

        @Test
        void slashSortsBeforeDigits() {
            assertEquals(-1, pathOps.compare("ab/c", "ab0c"));
            assertEquals(1, pathOps.compare("ab0c", "ab/c"));
        }

        @Test
        void slashPriorityBeatsLowerCodePoint() {
            assertEquals(-1, pathOps.compare("a/b", "a b"));
        }

        @Test
        void dotSortsBeforeDigits() {
            assertEquals(-1, pathOps.compare("a.b", "a0b"));
            assertEquals(1, pathOps.compare("a0b", "a.b"));
        }

        @Test
        void dotPriorityBeatsLowerCodePoint() {
            // '.' (46) > ' ' (32), yet dot sorts first -> rule is explicit, not numeric
            assertEquals(-1, pathOps.compare("a.b", "a b"));
        }

        @Test
        void slashSortsBeforeDot() {
            assertEquals(-1, pathOps.compare("a/b", "a.b"));
            assertEquals(1, pathOps.compare("a.b", "a/b"));
        }

        @Test
        void prefixWithSlashInLongerSortsFirst() {
            assertEquals(1, pathOps.compare("ab", "ab/c"));
            assertEquals(-1, pathOps.compare("ab/c", "ab"));
        }

        @Test
        void prefixWithoutSlashComparesByLength() {
            assertEquals(-1, pathOps.compare("ab", "abc"));
        }

        @Test
        void allSlashesCompareByLength() {
            assertEquals(1, pathOps.compare("//", "///"));
        }

        @Test
        void throwsOnEmptyPath() {
            assertThrows(IllegalArgumentException.class, () -> pathOps.compare("", "x"));
        }

        @Test
        void throwsOnEmptyAnother() {
            assertThrows(IllegalArgumentException.class, () -> pathOps.compare("x", ""));
        }

        @Test
        void throwsWhenBothEmpty() {
            assertThrows(IllegalArgumentException.class, () -> pathOps.compare("", ""));
        }

        @Test
        void throwsNpeOnNullPath() {
            assertThrows(NullPointerException.class, () -> pathOps.compare(null, "x"));
        }

        @Test
        void throwsNpeOnNullAnother() {
            assertThrows(NullPointerException.class, () -> pathOps.compare("x", null));
        }
    }
}
