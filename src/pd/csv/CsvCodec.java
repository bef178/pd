package pd.csv;

import java.util.List;

public class CsvCodec {

    private final int comma;

    private final String crlf;

    public CsvCodec(int comma, String crlf) {
        this.comma = comma;
        this.crlf = crlf;
    }

    public List<String> deserialize(String raw) {
        return CsvDeserializer.deserialize(raw, comma, crlf);
    }

    public String serialize(List<String> record) {
        return CsvSerializer.serialize(record, comma, crlf);
    }
}
