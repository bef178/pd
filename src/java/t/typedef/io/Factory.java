package t.typedef.io;

public final class Factory {

    /**
     * a scalar appears as a quoted String
     */
    public static CharSequence fromScalar(final InstallmentByteBuffer.Reader r) {
        int ch = r.next();
        if (ch != '"') {
            throw new ParsingException('"', ch);
        }

        StringBuilder sb = new StringBuilder();
        while (true) {
            ch = r.next();
            if (ch == '"') {
                return sb;
            }

            if (ch == '\\') {
                r.putBack();
                ch = FormatCodec.PrivateContract.decode(r);
            }
            sb.appendCodePoint(ch);
        }
    }

    private Factory() {
        // dummy
    }
}
