package pd.csv;

import java.util.List;

/**
 * COMMA as field separator<br/>
 * CR,LF as record separator<br/>
 * DOUBLE_QUOTE as start of escaping<br/>
 */
public class CsvMan {

    private final CsvSerializer serializer = new CsvSerializer();

    private final CsvDeserializer deserializer = new CsvDeserializer();

    public String serialize(List<String> values) {
        return serializer.serialize(values);
    }

    public List<String> deserialize(String recordString) {
        return deserializer.deserialize(recordString);
    }
}
