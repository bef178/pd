package pd.csv;

import java.util.List;

/**
 * COMMA as field separator<br/>
 * CR,LF as record separator<br/>
 * DOUBLE_QUOTE as start of escaping<br/>
 */
public class CsvMan {

    public String serialize(List<String> values) {
        return new ToCsvSerializer().toCsvRow(values);
    }

    public List<String> deserialize(String recordString) {
        return new FromCsvDeserializer().fromCsvRow(recordString);
    }
}
