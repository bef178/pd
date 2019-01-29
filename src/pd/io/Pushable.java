package pd.io;

public interface Pushable {

    public static final int E_EOF = Pullable.E_EOF;

    public static final int E_ARG = -2;

    public static Pushable wrap(final StringBuilder sb) {

        return new Pushable() {

            @Override
            public void push(int ch) {
                sb.appendCodePoint(ch);
            }
        };
    }

    public void push(int value);
}
