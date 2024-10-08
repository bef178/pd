package pd.fenc;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class Token {

    public int type;

    public String content;

    public Token() {
    }

    public Token(int type, String content) {
        this.type = type;
        this.content = content;
    }
}
