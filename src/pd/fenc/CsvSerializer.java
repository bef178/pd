package pd.fenc;

import static pd.ctype.Ctype.COMMA;
import static pd.ctype.Ctype.CR;
import static pd.ctype.Ctype.DOUBLE_QUOTE;
import static pd.ctype.Ctype.LF;
import static pd.fenc.IReader.EOF;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PrimitiveIterator.OfInt;

/**
 * ["a","b","c"] => "a,b,c\r\n"
 */
public class CsvSerializer {

    /**
     * @return true if record ends
     */
    private static boolean deserializeField(ICharReader src, int fieldSeparator, ICharWriter dst) {
        int state = 0;
        while (true) {
            switch (state) {
                case 0: {
                    int ch = src.hasNext() ? src.next() : EOF;
                    if (ch == EOF) {
                        return true;
                    } else if (ch == CR) {
                        state = 4;
                    } else if (ch == fieldSeparator) {
                        return false;
                    } else if (ch == DOUBLE_QUOTE) {
                        state = 2;
                    } else {
                        dst.append(ch);
                        state = 1;
                    }
                    break;
                }
                case 1: {
                    // unquoted
                    int ch = src.hasNext() ? src.next() : EOF;
                    if (ch == EOF) {
                        return true;
                    } else if (ch == CR) {
                        state = 4;
                    } else if (ch == fieldSeparator) {
                        return false;
                    } else if (ch == DOUBLE_QUOTE) {
                        throw new IllegalArgumentException();
                    } else {
                        dst.append(ch);
                    }
                    break;
                }
                case 2: {
                    // quoted
                    int ch = src.hasNext() ? src.next() : EOF;
                    if (ch == EOF) {
                        throw new IllegalArgumentException();
                    } else if (ch == DOUBLE_QUOTE) {
                        state = 3;
                    } else {
                        dst.append(ch);
                    }
                    break;
                }
                case 3: {
                    // seen quote
                    int ch = src.hasNext() ? src.next() : EOF;
                    if (ch == EOF) {
                        return true;
                    } else if (ch == CR) {
                        state = 5;
                    } else if (ch == fieldSeparator) {
                        return false;
                    } else if (ch == DOUBLE_QUOTE) {
                        dst.append(DOUBLE_QUOTE);
                        state = 2;
                    } else {
                        throw new IllegalArgumentException();
                    }
                    break;
                }
                case 4: {
                    // seen CR
                    int ch = src.hasNext() ? src.next() : EOF;
                    if (ch == EOF) {
                        dst.append(CR);
                        return true;
                    } else if (ch == LF) {
                        return true;
                    } else if (ch == fieldSeparator) {
                        dst.append(CR);
                        return false;
                    } else {
                        dst.append(CR);
                        dst.append(ch);
                        state = 1;
                    }
                    break;
                }
                case 5: {
                    // CR followed an appeared enclosing quote
                    int ch = src.hasNext() ? src.next() : EOF;
                    if (ch == LF) {
                        return true;
                    } else {
                        throw new IllegalArgumentException();
                    }
                }
            }
        }
    }

    public static List<String> deserializeRecord(OfInt it, int fieldSeparator) {
        ICharReader src = ICharReader.wrap(it);
        List<String> fields = new LinkedList<String>();
        while (src.hasNext()) {
            StringBuilder sb = new StringBuilder();
            boolean endsRecord = deserializeField(src, fieldSeparator, ICharWriter.wrap(sb));
            fields.add(sb.toString());
            if (endsRecord) {
                break;
            }
        }
        return fields;
    }

    public static List<String> deserializeRecord(String csv) {
        return deserializeRecord(csv, COMMA);
    }

    public static List<String> deserializeRecord(String csv, int fieldSeparator) {
        return deserializeRecord(csv.codePoints().iterator(), fieldSeparator);
    }

    private static int serializeField(String src, int fieldSeparator, ICharWriter dst) {
        boolean isQuoted = false;
        {
            ICharReader it = ICharReader.wrap(src);
            while (it.hasNext()) {
                int ch = it.next();
                if (ch == DOUBLE_QUOTE || ch == fieldSeparator) {
                    // quote
                    isQuoted = true;
                }
            }
        }

        int numProduced = 0;
        ICharReader it = ICharReader.wrap(src);
        if (isQuoted) {
            dst.append(DOUBLE_QUOTE);
            numProduced++;
        }
        while (it.hasNext()) {
            int ch = it.next();
            if (ch == DOUBLE_QUOTE) {
                // escape
                dst.append(DOUBLE_QUOTE);
                numProduced++;
            }
            dst.append(ch);
            numProduced++;
        }
        if (isQuoted) {
            dst.append(DOUBLE_QUOTE);
            numProduced++;
        }
        return numProduced;
    }

    public static String serializeRecord(List<String> fields) {
        return serializeRecord(fields, COMMA);
    }

    public static String serializeRecord(List<String> fields, int fieldSeparator) {
        StringBuilder sb = new StringBuilder();
        ICharWriter dst = ICharWriter.wrap(sb);
        Iterator<String> it = fields.iterator();
        while (it.hasNext()) {
            String field = it.next();
            serializeField(field, fieldSeparator, dst);
            if (it.hasNext()) {
                dst.append(fieldSeparator);
            }
        }
        dst.append(CR);
        dst.append(LF);
        return sb.toString();
    }

    public static String serializeRecord(String... fields) {
        return serializeRecord(Arrays.asList(fields));
    }
}
