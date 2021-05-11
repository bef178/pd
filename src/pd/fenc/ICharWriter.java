package pd.fenc;

public interface ICharWriter {

    public static ICharWriter wrap(final StringBuilder sb) {

        return new ICharWriter() {

            private int pos = 0;

            @Override
            public ICharWriter append(int ch) {
                sb.appendCodePoint(ch);
                return this;
            }

            @Override
            public int position() {
                return pos;
            }
        };
    }

    /**
     * consume an Unicode character
     */
    public ICharWriter append(int ch);

    public int position();
}
