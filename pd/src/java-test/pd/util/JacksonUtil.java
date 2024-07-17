package pd.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;

public class JacksonUtil {

    static final ObjectMapper CONFIGURED_OBJECT_MAPPER = new ObjectMapper()
            .configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    @SneakyThrows
    public static String jacksonSerialize(Object o) {
        return CONFIGURED_OBJECT_MAPPER.writeValueAsString(o);
    }

    @SneakyThrows
    public static <T> T jacksonDeserialize(String s, Class<T> targetClass) {
        return CONFIGURED_OBJECT_MAPPER.readValue(s, targetClass);
    }
}
