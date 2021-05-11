package pd.fenc;

public interface ICharWriter {

    public static ICharWriter wrap(final StringBuilder sb) {

        return new ICharWriter() {

            private int pos = 0;

            @Override
            public int position() {
                return pos;
            }

            @Override
            public ICharWriter push(int ch) {
                sb.appendCodePoint(ch);
                return this;
            }
        };
    }

    public int position();

    /**
     * consume an Unicode character
     */
    public ICharWriter push(int ch);
}
