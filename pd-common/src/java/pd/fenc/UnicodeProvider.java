package pd.fenc;

import java.util.PrimitiveIterator;

import pd.util.AsciiExtension;

public class UnicodeProvider implements Int32Provider {

    private final Int32Provider src;

    private final Recent recent = new Recent(2);

    private int nBack;

    public UnicodeProvider(CharSequence cs) {
        this(Int32Provider.wrap(cs));
    }

    public UnicodeProvider(Int32Provider src) {
        this.src = src;
        this.nBack = 0;
    }

    @Override
    public boolean hasNext() {
        if (nBack > 0) {
            return true;
        } else {
            return src.hasNext();
        }
    }

    @Override
    public int next() {
        if (nBack > 0) {
            return recent.get(-nBack--);
        }
        int value = src.next(); // let it throw if no next value
        if (value < 0 || value > 0x10FFFF) {
            throw new ParsingException(String.format("E: upstream should value in [0,0x10FFFF], actual `%d`", value));
        }
        recent.add(value);
        return value;
    }

    @Override
    public int position() {
        return src.position() - nBack;
    }

    public void back() {
        if (!tryBack()) {
            throw new ParsingException("E: back() beyond capacity");
        }
    }

    public boolean tryBack() {
        if (nBack + 1 > recent.capacity()) {
            return false;
        }
        nBack++;
        return true;
    }

    public int peek() {
        if (hasNext()) {
            int result = next();
            back();
            return result;
        } else {
            return EOF;
        }
    }

    public void eat(int expected) {
        if (!tryEat(expected)) {
            throw new ParsingException(String.format(
                    "E: expected `%s`, actual `%s`", Util.codepointToString(expected), Util.codepointToString(peek())));
        }
    }

    /**
     * will stop in front of unexpected value
     */
    public boolean tryEat(int expected) {
        if (hasNext()) {
            if (next() == expected) {
                return true;
            } else {
                back();
                return false;
            }
        } else {
            return expected == EOF;
        }
    }

    public boolean tryEatAll(String s) {
        return tryEatAll(s.codePoints().iterator());
    }

    public boolean tryEatAll(PrimitiveIterator.OfInt ofInt) {
        while (ofInt.hasNext()) {
            int expected = ofInt.nextInt();
            if (!tryEat(expected)) {
                return false;
            }
        }
        return true;
    }

    public void eatWhitespacesIfAny() {
        while (hasNext()) {
            int ch = next();
            if (!AsciiExtension.isWhitespace(ch)) {
                back();
                return;
            }
        }
    }
}
