package pd.fenc;

import pd.util.CurlyBracketPatternExtension;

public interface Int32Pusher {

    Int32Pusher push(int value);

    int position();

    Int32Pusher valueRange(int minValue, int maxValue);

    static Int32Pusher wrap(StringBuilder sb) {
        return new Int32Pusher() {

            private int minValue = 0;

            private int maxValue = 0x10FFFF;

            @Override
            public Int32Pusher push(int value) {
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

            @Override
            public Int32Pusher valueRange(int minValue, int maxValue) {
                this.minValue = minValue;
                this.maxValue = maxValue;
                return this;
            }
        };
    }
}
