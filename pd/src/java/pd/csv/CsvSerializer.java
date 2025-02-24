package pd.csv;

import java.util.Iterator;
import java.util.List;
import java.util.PrimitiveIterator;

import lombok.NonNull;
import pd.fenc.UnicodeConsumer;
import pd.util.AsciiExtension;

class CsvSerializer {

    /**
     * serialize to a string, without trailing CR or LF
     */
    public String serialize(@NonNull List<String> fields) {
        StringBuilder sb = new StringBuilder();
        serialize(fields, UnicodeConsumer.wrap(sb));
        return sb.toString();
    }

    private void serialize(Iterable<String> fields, UnicodeConsumer dst) {
        Iterator<String> it = fields.iterator();
        while (it.hasNext()) {
            serializeField(it.next(), dst);
            if (it.hasNext()) {
                dst.next(AsciiExtension.COMMA);
            }
        }
    }

    private void serializeField(String field, UnicodeConsumer dst) {
        StringBuilder sb = new StringBuilder();
        boolean shouldQuote = false;

        PrimitiveIterator.OfInt it = field.codePoints().iterator();
        while (it.hasNext()) {
            int ch = it.nextInt();
            switch (ch) {
                case AsciiExtension.DOUBLE_QUOTE:
                    shouldQuote = true;
                    sb.appendCodePoint(AsciiExtension.DOUBLE_QUOTE).appendCodePoint(AsciiExtension.DOUBLE_QUOTE);
                    break;
                case AsciiExtension.COMMA:
                case AsciiExtension.CR:
                case AsciiExtension.LF:
                    shouldQuote = true;
                    sb.appendCodePoint(ch);
                    break;
                default:
                    sb.appendCodePoint(ch);
                    break;
            }
        }

        if (shouldQuote) {
            dst.next(AsciiExtension.DOUBLE_QUOTE).next(sb).next(AsciiExtension.DOUBLE_QUOTE);
        } else {
            dst.next(sb);
        }
    }
}
