package pd.jaco;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import pd.fun.Cat;
import pd.fun.Dog;
import pd.fun.ernie.entity.ErnieMessage;
import pd.fun.ernie.entity.ErnieRequest;
import pd.jaco.bridge.JacoToEntityConverter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestConversion {

    @Test
    public void testEntityTypeMapping() {
        JacoToEntityConverter.Config config = new JacoToEntityConverter.Config();

        assertEquals(ArrayList.class, config.queryEntityTypeMapping("ignored", List.class, "ignored"));
        assertEquals(LinkedHashMap.class, config.queryEntityTypeMapping("ignored", Map.class, "ignored"));

        config.registerEntityTypeMapping("/animals/1", Cat.class);
        config.registerEntityTypeMapping("/animals/*", Dog.class);

        assertEquals(Dog.class, config.queryEntityTypeMapping(null, Object.class, "/animals/0"));
        assertEquals(Cat.class, config.queryEntityTypeMapping(null, Object.class, "/animals/1"));
        assertEquals(Dog.class, config.queryEntityTypeMapping(null, Object.class, "/animals/2"));
    }

    @Test
    public void testEntity() {
        Object entity;
        {
            Cat cat = new Cat();
            cat.Phylum = "Chordata";
            cat.Class = "Mammalia";
            cat.Order = "Carnivora";
            cat.Family = "Felidae";
            cat.Genus = "Felis";
            cat.Species = "Felis catus";
            cat.name = "Mimi";
            cat.birthTime = Instant.EPOCH;
            entity = cat;
        }
        Object jaco;
        {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("birthTime", Instant.EPOCH.toString());
            m.put("Phylum", "Chordata");
            m.put("Class", "Mammalia");
            m.put("Order", "Carnivora");
            m.put("Family", "Felidae");
            m.put("Genus", "Felis");
            m.put("Species", "Felis catus");
            m.put("name", "Mimi");
            jaco = m;
        }
        JsonMan jsonMan = new JsonMan();
        assertEquals(entity, jsonMan.jacoToEntity(jaco, Cat.class, "Cat"));
        assertEquals(jaco, jsonMan.entityToJaco(entity));
    }

    @Test
    public void testNullEntity() {
        Cat cat = new Cat();
        cat.name = "Mimi";

        JsonMan jsonMan = new JsonMan();

        @SuppressWarnings("unchecked")
        Map<String, Object> jaco = (Map<String, Object>) jsonMan.entityToJaco(cat);
        assertFalse(jaco.containsKey("Phylum"));
        assertTrue(jaco.containsKey("name"));
    }

    @Test
    public void testArrayEntity() {
        Object entity;
        {
            Cat cat = new Cat();
            cat.name = "Mimi";
            List<Cat> cats = new ArrayList<>();
            cats.add(cat);
            entity = cats;
        }
        Object jaco;
        {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", "Mimi");
            List<Map<String, Object>> a = new LinkedList<>();
            a.add(m);
            jaco = a;
        }

        JsonMan jsonMan = new JsonMan();
        jsonMan.jacoToEntityConverter.config.registerEntityTypeMapping("aaa/*", Cat.class);

        assertEquals(entity, jsonMan.jacoToEntity(jaco, List.class, "aaa"));
        assertEquals(jaco, jsonMan.entityToJaco(entity));
    }

    @Test
    public void testMapEntity() {
        Object entity;
        {
            Cat cat = new Cat();
            cat.name = "Mimi";
            Map<String, Cat> cats = new LinkedHashMap<>();
            cats.put(cat.name, cat);
            entity = cats;
        }
        Object jaco;
        {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", "Mimi");
            Map<String, Map<String, Object>> a = new HashMap<>();
            a.put(m.get("name").toString(), m);
            jaco = a;
        }

        JsonMan jsonMan = new JsonMan();
        jsonMan.jacoToEntityConverter.config.registerEntityTypeMapping("aaa/*", Cat.class);

        assertEquals(entity, jsonMan.jacoToEntity(jaco, Map.class, "aaa"));
        assertEquals(jaco, jsonMan.entityToJaco(entity));
    }

    @Test
    public void test() {
        String json = "\"9\\n3d$fs冬你我他\""; // 冬: 0x2F81A, 194586
        Object o = "9\n3d$fs冬你我他";

        JsonMan jsonMan = new JsonMan();
        assertEquals(json, jsonMan.jacoToJson(o));
        assertEquals(o, jsonMan.jsonToJaco(json));
    }

    @Test
    public void test2() {
        final Object jaco;
        {
            Map<String, Object> o = new LinkedHashMap<>();
            {
                List<Object> a = new LinkedList<>();
                a.add(4L);
                a.add(2L);
                o.put("a", a);
            }
            {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("1", "1");
                m.put("k", "v");
                o.put("m", m);
            }
            o.put("i", 32);
            o.put("i64", 64L);
            o.put("f", 32.32f);
            o.put("pi", 3.141592653589793); // double has 16 significant digits
            jaco = o;
        }
        final String json = "{\"a\":[4,2],\"m\":{\"1\":\"1\",\"k\":\"v\"},\"i\":32,\"i64\":64,\"f\":32.32,\"pi\":3.141592653589793}";

        JacoMan jacoMan = new JacoMan();
        JsonMan jsonMan = new JsonMan();

        assertEquals(json, jsonMan.jacoToJson(jaco));

        Object producedJaco = jsonMan.jsonToJaco(json);
        assertEquals(4L, jacoMan.getWithPath(producedJaco, "a/0"));
        assertEquals("v", jacoMan.getWithPath(producedJaco, "m/k"));
        assertEquals(Double.class, jacoMan.getWithPath(producedJaco, "f").getClass());
        assertEquals(32.32d, jacoMan.getWithPath(producedJaco, "f"));
        assertEquals(3.141592653589793, jacoMan.getWithPath(producedJaco, "pi"));
    }

    @Test
    public void test3() {
        Object jaco;
        {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("a", 1L);
            m.put("b", 2L);
            jaco = m;
        }
        String json = "{\"a\":1,\"b\":2}";

        JsonMan jsonMan = new JsonMan();
        assertEquals(json, jsonMan.jacoToJson(jaco));
        assertEquals(jaco, jsonMan.jsonToJaco(json));
    }

    @Test
    public void test4() {
        Object jaco;
        {
            List<Object> a = new LinkedList<>();
            a.add("a");
            a.add("b");
            jaco = a;
        }
        String json = "[\"a\",\"b\"]";

        JsonMan jsonMan = new JsonMan();
        assertEquals(json, jsonMan.jacoToJson(jaco));
        assertEquals(jaco, jsonMan.jsonToJaco(json));
    }

    @Test
    public void test5() {
        ErnieRequest request = new ErnieRequest();
        request.messages = new LinkedList<>();
        request.messages.add(ErnieMessage.userMessage("how are you"));
        request.messages.add(ErnieMessage.assistantMessage("fine, thank you, and you"));
        request.messages.add(ErnieMessage.userMessage("how old are you"));
        request.stream = true;

        Map<String, Object> requestJaco = new LinkedHashMap<>();
        {
            List<Object> messages = new LinkedList<>();
            {
                Map<String, Object> m1 = new LinkedHashMap<>();
                m1.put("role", ErnieMessage.ROLE_USER.toString());
                m1.put("content", "how are you");
                messages.add(m1);
            }
            {
                Map<String, Object> m1 = new LinkedHashMap<>();
                m1.put("role", ErnieMessage.ROLE_ASSISTANT.toString());
                m1.put("content", "fine, thank you, and you");
                messages.add(m1);
            }
            {
                Map<String, Object> m1 = new LinkedHashMap<>();
                m1.put("role", ErnieMessage.ROLE_USER.toString());
                m1.put("content", "how old are you");
                messages.add(m1);
            }
            requestJaco.put("messages", messages);
            requestJaco.put("stream", true);
        }

        String requestJson = "{\"messages\":[{\"role\":\"user\",\"content\":\"how are you\"},{\"role\":\"assistant\",\"content\":\"fine, thank you, and you\"},{\"role\":\"user\",\"content\":\"how old are you\"}],\"stream\":true}";

        JsonMan jsonMan = new JsonMan();
        jsonMan.jacoToEntityConverter.config.registerEntityTypeMapping("ErnieRequest/messages/*", ErnieMessage.class);

        assertEquals(request, jsonMan.jacoToEntity(requestJaco, ErnieRequest.class, "ErnieRequest"));
        assertEquals(requestJaco, jsonMan.entityToJaco(request));

        assertEquals(requestJson, jsonMan.serialize(request));
        assertEquals(request, jsonMan.deserialize(requestJson, ErnieRequest.class, "ErnieRequest"));
    }
}
