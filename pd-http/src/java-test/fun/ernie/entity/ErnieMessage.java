package fun.ernie.entity;

public class ErnieMessage {

    public static final String ROLE_USER = "user";

    public static final String ROLE_ASSISTANT = "assistant";

    public String role;

    public String content;

    public ErnieMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }
}
