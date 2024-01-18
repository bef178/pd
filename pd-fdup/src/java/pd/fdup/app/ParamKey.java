package pd.fdup.app;

public enum ParamKey {

    command;

    public String toOptString() {
        return "--" + name() + ":";
    }
}
