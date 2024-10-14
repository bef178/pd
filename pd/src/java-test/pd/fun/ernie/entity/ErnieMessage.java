package pd.fun.ernie.entity;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class ErnieMessage {

    public static final MessageRole ROLE_USER = MessageRole.USER;

    public static final MessageRole ROLE_ASSISTANT = MessageRole.ASSISTANT;

    public static ErnieMessage userMessage(String content) {
        return new ErnieMessage(ROLE_USER, content);
    }

    public static ErnieMessage assistantMessage(String content) {
        return new ErnieMessage(ROLE_ASSISTANT, content);
    }

    public MessageRole role;

    public String content;

    public ErnieMessage() {
    }

    public ErnieMessage(MessageRole role, String content) {
        this.role = role;
        this.content = content;
    }
}
