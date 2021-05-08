package pd.fenc;

public interface ICharWriter {

    public static ICharWriter wrap(final StringBuilder sb) {

        return new ICharWriter() {

            @Override
            public ICharWriter append(int ch) {
                sb.appendCodePoint(ch);
                return this;
            }
        };
    }

    /**
     * consume an unicode character
     */
    public ICharWriter append(int ch);
}
