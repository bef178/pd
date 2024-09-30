package pd.csv;

import java.util.Iterator;
import java.util.List;
import java.util.PrimitiveIterator;

import lombok.NonNull;
import pd.fenc.UnicodeConsumer;
import pd.util.AsciiExtension;

class ToCsvSerializer {

    public String toCsvRow(@NonNull List<String> values) {
        StringBuilder sb = new StringBuilder();
        toCsvRow(values, UnicodeConsumer.wrap(sb));
        return sb.toString();
    }

    private void toCsvRow(Iterable<String> values, UnicodeConsumer unicodeConsumer) {
        Iterator<String> it = values.iterator();
        while (it.hasNext()) {
            String value = it.next();
            stringToCsvValue(value, unicodeConsumer);
            if (it.hasNext()) {
                unicodeConsumer.next(AsciiExtension.COMMA);
            }
        }
        unicodeConsumer.next(AsciiExtension.CR);
        unicodeConsumer.next(AsciiExtension.LF);
    }

    private void stringToCsvValue(String value, UnicodeConsumer unicodeConsumer) {
        StringBuilder sb = new StringBuilder();
        boolean shouldQuote = false;

        PrimitiveIterator.OfInt it = value.codePoints().iterator();
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
            unicodeConsumer.next(AsciiExtension.DOUBLE_QUOTE).next(sb).next(AsciiExtension.DOUBLE_QUOTE);
        } else {
            unicodeConsumer.next(sb);
        }
    }
}
