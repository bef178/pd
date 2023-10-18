package pd.fenc;

import java.io.InputStream;

/**
 * limited backable int32 provider
 */
public class Int32Feeder extends Int32Provider {

    private final Int32Provider src;

    private final Recent recent = new Recent(2);

    private int nBack;

    public Int32Feeder(InputStream inputStream) {
        this(Int32Provider.wrap(inputStream));
    }

    public Int32Feeder(CharSequence cs) {
        this(Int32Provider.wrap(cs));
    }

    Int32Feeder(Int32Provider src) {
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
}
