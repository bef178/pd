package pd.fenc;

public interface IWriter {

    /**
     * UnicodeConsumer
     */
    public static IWriter wrap(final StringBuilder sb) {

        return new IWriter() {

            @Override
            public IWriter append(int ch) {
                sb.appendCodePoint(ch);
                return this;
            }
        };
    }

    public IWriter append(int value);
}
