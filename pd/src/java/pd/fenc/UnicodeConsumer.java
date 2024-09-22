package pd.fenc;

import java.util.PrimitiveIterator;

import pd.util.CurlyBracketPatternExtension;

public interface UnicodeConsumer {

    UnicodeConsumer push(int value);

    default UnicodeConsumer push(String s) {
        PrimitiveIterator.OfInt it = s.codePoints().iterator();
        while (it.hasNext()) {
            push(it.next());
        }
        return this;
    }

    int position();

    static UnicodeConsumer wrap(StringBuilder sb) {
        return new UnicodeConsumer() {

            @Override
            public UnicodeConsumer push(int value) {
                int minValue = 0;
                int maxValue = 0x10FFFF;
                if (value < minValue || value > maxValue) {
                    throw new ParsingException(CurlyBracketPatternExtension.format("E: value {} not in range [{}, {}]", value, minValue, maxValue));
                }
                sb.appendCodePoint(value);
                return this;
            }

            @Override
            public int position() {
                return sb.length();
            }
        };
    }
}
