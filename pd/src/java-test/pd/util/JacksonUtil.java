package pd.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;

public class JacksonUtil {

    static final ObjectMapper jackson = new ObjectMapper()
            .configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    @SneakyThrows
    public static String jacksonSerialize(Object o) {
        return jackson.writeValueAsString(o);
    }

    @SneakyThrows
    public static <T> T jacksonDeserialize(String s, Class<T> targetClass) {
        return jacksonDeserialize(s, targetClass, jackson);
    }

    @SneakyThrows
    public static <T> T jacksonDeserialize(String s, Class<T> targetClass, ObjectMapper objectMapper) {
        return objectMapper.readValue(s, targetClass);
    }

    @SneakyThrows
    public static <T> T jacksonDeserialize(String s, TypeReference<T> typeReference) {
        return jacksonDeserialize(s, typeReference, jackson);
    }

    @SneakyThrows
    public static <T> T jacksonDeserialize(String s, TypeReference<T> typeReference, ObjectMapper objectMapper) {
        return objectMapper.readValue(s, typeReference);
    }
}
