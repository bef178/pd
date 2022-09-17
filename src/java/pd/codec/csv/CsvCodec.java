package pd.codec.csv;

import java.util.List;

/**
 * COMMA as field separator<br/>
 * CR,LF as record separator<br/>
 * DOUBLE_QUOTE as start of escaping<br/>
 */
public class CsvCodec {

    public static List<String> deserialize(String csvText) {
        return CsvDeserializer.deserialize(csvText);
    }

    public static String serialize(List<String> fields) {
        return CsvSerializer.serialize(fields);
    }
}
