package pd.aws.s3.app;

public enum CommandKey {

    list,
    list_all,
    download,
    download_all,
    upload,
    upload_all,
    remove,
    remove_all;

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
