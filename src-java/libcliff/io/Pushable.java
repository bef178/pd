package libcliff.io;

public interface Pushable {

    static final int E_EOF = Pullable.E_EOF;
    static final int E_ARG = -2;

    public static Pushable wrap(final StringBuilder sb) {

        return new Pushable() {

            @Override
            public int push(int ch) {
                int n = sb.length();
                sb.appendCodePoint(ch);
                return sb.length() - n;
            }
        };
    }

    public int push(int ch);
}
