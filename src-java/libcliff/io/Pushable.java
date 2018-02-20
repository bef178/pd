package libcliff.io;

public interface Pushable {

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

    /**
     * return the number of "units" pushed into the stream
     */
    public int push(int ch);
}
