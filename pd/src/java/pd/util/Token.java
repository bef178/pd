package pd.util;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class Token<T> {

    public T type;

    public String content;

    public Token(T type, String content) {
        this.type = type;
        this.content = content;
    }
}
