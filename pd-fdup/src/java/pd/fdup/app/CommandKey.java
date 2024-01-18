package pd.fdup.app;

public enum CommandKey {

    list,
    list_duplicated,
    remove_duplicated;

    public static CommandKey fromLiteral(String literal) {
        if (literal == null) {
            return null;
        }
        try {
            return valueOf(literal.toLowerCase());
        } catch (Exception e) {
            return null;
        }
    }
}
