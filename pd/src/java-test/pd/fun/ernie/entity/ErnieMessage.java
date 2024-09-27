package pd.fun.ernie.entity;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class ErnieMessage {

    public static final String ROLE_USER = "user";

    public static final String ROLE_ASSISTANT = "assistant";

    public static ErnieMessage userMessage(String content) {
        return new ErnieMessage(ROLE_USER, content);
    }

    public static ErnieMessage assistantMessage(String content) {
        return new ErnieMessage(ROLE_ASSISTANT, content);
    }

    public String role;

    public String content;

    public ErnieMessage() {
    }

    public ErnieMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }
}
