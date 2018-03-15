package libcliff.io;

public interface Pushable {

    static final int E_EOF = Pullable.EOF;
    static final int E_ARG = -2;

    public static Pushable wrap(final StringBuilder sb) {

        return new Pushable() {

            @Override
            public int push(int ch) {
                sb.appendCodePoint(ch);
                return 1;
            }
        };
    }

    public int push(int ch);
}
