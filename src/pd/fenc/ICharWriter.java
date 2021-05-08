package pd.fenc;

public interface ICharWriter {

    public static ICharWriter wrap(StringBuilder sb) {

        return new ICharWriter() {

            @Override
            public void append(int ch) {
                sb.appendCodePoint(ch);
            }
        };
    }

    /**
     * consume an unicode character
     */
    public void append(int ch);
}
