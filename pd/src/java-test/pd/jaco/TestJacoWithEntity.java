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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestJacoWithEntity {

    JacoMan jacoMan = new JacoMan();

    @Test
    public void testJacoToEntityConfig() {
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
    public void testFromToEntity() {
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
        assertEquals(entity, jacoMan.toEntity(jaco, Cat.class, "Cat"));
        assertEquals(jaco, jacoMan.fromEntity(entity));

        {
            ErnieRequest request = new ErnieRequest();
            request.messages = new LinkedList<>();
            request.messages.add(ErnieMessage.userMessage("how are you"));
            request.messages.add(ErnieMessage.assistantMessage("fine, thank you, and you"));
            request.messages.add(ErnieMessage.userMessage("how old are you"));
            request.stream = true;
            entity = request;
        }
        {
            Map<String, Object> m = new LinkedHashMap<>();
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
                m.put("messages", messages);
            }
            m.put("stream", true);
            jaco = m;
        }
        jacoMan.toEntityConfig.registerEntityTypeMapping("ErnieRequest/messages/*", ErnieMessage.class);
        assertEquals(entity, jacoMan.toEntity(jaco, ErnieRequest.class, "ErnieRequest"));
        assertEquals(jaco, jacoMan.fromEntity(entity));
    }

    @Test
    public void testFromToEntityNullField() {
        Cat cat = new Cat();
        cat.name = "Mimi";
        @SuppressWarnings("unchecked")
        Map<String, Object> jaco = (Map<String, Object>) jacoMan.fromEntity(cat);
        assertFalse(jaco.containsKey("Phylum"));
        assertTrue(jaco.containsKey("name"));
    }

    @Test
    public void testArray() {
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

        JacoMan jacoMan = new JacoMan();
        jacoMan.toEntityConfig.registerEntityTypeMapping("aaa/*", Cat.class);

        assertEquals(entity, jacoMan.toEntity(jaco, List.class, "aaa"));
        assertEquals(jaco, jacoMan.fromEntity(entity));
    }

    @Test
    public void testMap() {
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

        JacoMan jacoMan = new JacoMan();
        jacoMan.toEntityConfig.registerEntityTypeMapping("aaa/*", Cat.class);

        assertEquals(entity, jacoMan.toEntity(jaco, Map.class, "aaa"));
        assertEquals(jaco, jacoMan.fromEntity(entity));
    }
}
