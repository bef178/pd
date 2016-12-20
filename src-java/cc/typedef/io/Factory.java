package cc.typedef.io;

import cc.typedef.basic.Blob;
import cc.typedef.primitive.Ctype;

public final class Factory {

    /**
     * a scalar appears as a quoted String
     */
    public static CharSequence buildScalar(InstallmentByteBuffer.Reader r) {
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

    public static void serialize(CharSequence s, InstallmentByteBuffer w) {
        w.append('\"');
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (Ctype.isAlphanum(c)) {
                w.append(c);
                continue;
            }

            int ch = c;
            if (Character.isHighSurrogate(c)) {
                char chLow = s.charAt(++i);
                if (Character.isLowSurrogate(chLow)) {
                    ch = Character.toCodePoint(c, chLow);
                } else {
                    throw new ParsingException();
                }
            } else if (Character.isLowSurrogate(c)) {
                throw new ParsingException();
            }
            // faster than String.getBytes("UTF-8") with exception handled
            // faster than toUtf8() then toHexText()
            Blob blob = new Blob();
            FormatCodec.PrivateContract.encode(ch, blob);
            w.append(blob.a);
        }
        w.append('\"');
    }

    private Factory() {
        // dummy
    }
}
