package pd.fun.ernie.entity;

public enum MessageRole {
    USER,
    ASSISTANT;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
