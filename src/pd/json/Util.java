package pd.json;

import pd.fenc.ParsingException;

class Util {

    static <T extends IJsonToken> T cast(IJsonToken jsonToken, Class<T> expectedClass) {
        if (jsonToken == null || !expectedClass.isInstance(jsonToken)) {
            throw new ParsingException(String.format("invalid json type: expected [%s], actual [%s]",
                    expectedClass.getName(), jsonToken.getClass().getName()));
        }
        return expectedClass.cast(jsonToken);
    }

    static String serializeToQuotedString(String s) {
        return pd.fenc.Util.serializeToQuotedString(s);
    }
}
