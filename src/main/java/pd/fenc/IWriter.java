package pd.fenc;

public interface IWriter {

    public static IWriter unicodeStream(StringBuilder sb) {

        return new IWriter() {

            private int pos = 0;

            @Override
            public int position() {
                return pos;
            }

            @Override
            public IWriter push(int ch) {
                sb.appendCodePoint(ch);
                return this;
            }
        };
    }

    public int position();

    public IWriter push(int value);
}
