package pd.jaco;

import java.util.LinkedList;

import org.junit.jupiter.api.Test;
import pd.fun.ernie.entity.ErnieMessage;
import pd.fun.ernie.entity.ErnieRequest;
import pd.util.PathPattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestJsonMan {

    @Test
    public void test() {
        ErnieRequest request = new ErnieRequest();
        request.messages = new LinkedList<>();
        request.messages.add(ErnieMessage.userMessage("how are you"));
        request.messages.add(ErnieMessage.assistantMessage("fine, thank you, and you"));
        request.messages.add(ErnieMessage.userMessage("how old are you"));
        request.stream = true;

        String json = "{\"messages\":[{\"role\":\"user\",\"content\":\"how are you\"},{\"role\":\"assistant\",\"content\":\"fine, thank you, and you\"},{\"role\":\"user\",\"content\":\"how old are you\"}],\"stream\":true}";

        JsonMan jsonMan = new JsonMan();
        jsonMan.getToEntityConfig().register(Object.class, (j, p, c) -> {
            if (PathPattern.singleton().matches("/messages/[*]", p)) {
                return ErnieMessage.class;
            }
            return Object.class;
        });

        assertEquals(json, jsonMan.serialize(request));
        assertEquals(request, jsonMan.deserialize(json, ErnieRequest.class));
    }
}
