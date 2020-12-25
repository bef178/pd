package pd.fenc;

import java.util.Arrays;

/**
 * A scanner is used to feed a parser, providing int32 values(or CachedReader?),<br/>
 * <br/>
 * Generally a stream never moves backward, while a parser sometimes needs "pre-read"
 * several symbols to speculate which token parser should be invoked.
 * And, the pre-reads and the rest of the stream would be passed back and forth.
 * It requires clean APIs; I like the C way.<br/>
 */
public class Int32Scanner implements IReader {

    public static final int EOF = -1;

    private final int[] recents;

    private final IReader src;

    private int position;

    private int offset;

    public Int32Scanner() {
        recents = null;
        src = null;
    }

    public Int32Scanner(int first, IReader rest) {
        this(new int[] { first }, rest);
    }

    public Int32Scanner(int[] known, int i, int j, IReader rest) {
        this(Arrays.copyOfRange(known, i, j), rest);
    }

    private Int32Scanner(int[] recents, IReader rest) {
        this.recents = recents;
        this.src = rest;
        this.position = recents.length;
        this.offset = -recents.length;
    }

    public Int32Scanner(IReader src) {
        this.src = src;
        this.position = 0;
        this.offset = 0;
        this.recents = new int[] { 0 };
    }

    private int getRecent(int numBackSteps) {
        assert numBackSteps > 0 && numBackSteps <= recents.length && numBackSteps <= position;
        int i = position - numBackSteps;
        return recents[i % recents.length];
    }

    @Override
    public boolean hasNext() {
        if (offset < 0) {
            return true;
        } else {
            return src.hasNext();
        }
    }

    /**
     * return num of steps it actually takes
     */
    public int move(int numSteps) {
        int i = offset + numSteps;
        if (i > 0) {
            i = 0;
        } else {
            if (i < -recents.length) {
                i = -recents.length;
            }
            if (i < -position) {
                i = -position;
            }
        }
        int diff = i - offset;
        offset = i;
        return diff;
    }

    public int moveBack() {
        return moveBack(1);
    }

    /**
     * return num of back steps it actually takes
     */
    public int moveBack(int numSteps) {
        return -move(-numSteps);
    }

    @Override
    public int next() {
        if (offset < 0) {
            return getRecent(-offset++);
        } else if (src.hasNext()) {
            int value = src.next();
            putRecent(value);
            return value;
        } else {
            return EOF;
        }
    }

    public int position() {
        return position + offset;
    }

    private void putRecent(int value) {
        recents[position++ % recents.length] = value;
    }
}
