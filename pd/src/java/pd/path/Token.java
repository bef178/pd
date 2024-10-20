package pd.path;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
class Token extends pd.util.Token<TokenType> {

    public Token(TokenType type, String content) {
        super(type, content);
    }
}
