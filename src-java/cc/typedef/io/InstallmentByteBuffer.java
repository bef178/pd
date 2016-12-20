package cc.typedef.io;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

import cc.typedef.basic.Blob;

/**
 * some thing of smart array & queue & installment savings
 */
public class InstallmentByteBuffer {

    /**
     * not a java.io.Reader<br/>
     */
    public class Reader implements FormatCodec.Nextable {

        private int next = 0;

        /**
         * get from underlying byte sequence assuming encoded with utf8<br/>
         * throws {@link ParsingException} if fail
         */
        public int getNextCodePoint() {
            int n = FormatCodec.Unicode.utf8LengthByUtf8((byte) peek());
            if (n < 0) {
                throw new ParsingException();
            }

            // have to copy coz cannot pass the function next() into FormatCodec.Unicode
            byte[] a = new byte[n];
            for (int i = 0; i < n; ++i) {
                int ch = next();
                if (ch < 0) {
                    throw new ParsingException();
                }
                a[i] = (byte) (ch & 0xFF);
            }
            return FormatCodec.Unicode.fromUtf8(new Blob(a, 0));
        }

        public boolean hasNext() {
            return next >= 0 && next < used;
        }

        @Override
        public int next() {
            if (hasNext()) {
                return get(next++) & 0xFF;
            } else {
                return -1;
            }
        }

        /**
         * For reading, get the cursor's offset.
         */
        public int offset() {
            return next;
        }

        public int peek() {
            if (hasNext()) {
                return get(next) & 0xFF;
            } else {
                return -1;
            }
        }

        public void putBack() {
            if (next > 0) {
                --next;
                return;
            }
            throw new IndexOutOfBoundsException();
        }

        /**
         * Rewind for reading.
         */
        public void rewind() {
            seek(0);
        }

        public void seek(int pos) {
            if (pos >= 0 && pos < used) {
                next = pos;
            } else {
                throw new IndexOutOfBoundsException();
            }
        }

        public void seek(int offset, int whence) {
            switch (whence) {
                case SEEK_SET:
                    whence = 0;
                    break;
                case SEEK_CUR:
                    whence = next;
                    break;
                case SEEK_END:
                    whence = used;
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            seek(whence + offset);
        }

        public int size() {
            return InstallmentByteBuffer.this.size();
        }
    }

    public static final int SEEK_SET = 0;
    public static final int SEEK_CUR = 1;
    public static final int SEEK_END = 2;

    private static final int INSTALLMENT_BITS = 10;
    private static final int INSTALLMENT_BYTES = 1 << INSTALLMENT_BITS;
    private static final int INSTALLMENT_MASK = INSTALLMENT_BYTES - 1;

    private ArrayList<byte[]> savings = new ArrayList<>();
    private int used = 0;

    private boolean readonly = false;

    public InstallmentByteBuffer() {
        this(INSTALLMENT_BYTES);
    }

    public InstallmentByteBuffer(int capacity) {
        setupCapacity(capacity);
    }

    public InstallmentByteBuffer append(Blob blob) {
        return append(blob.a, blob.i, blob.a.length);
    }

    public InstallmentByteBuffer append(byte[] a) {
        return append(a, 0, a.length);
    }

    public InstallmentByteBuffer append(byte[] a, int i, int j) {
        if (readonly()) {
            return this;
        }

        int n = j - i;
        setupCapacity(used + n);

        if ((used & INSTALLMENT_MASK) != 0) {
            int left = INSTALLMENT_BYTES - (used & INSTALLMENT_MASK);
            if (left > n) {
                left = n;
            }
            System.arraycopy(a, i, savings.get(used >> INSTALLMENT_BITS),
                    used & INSTALLMENT_MASK, left);
            i += left;
            used += left;
        }

        while (i + INSTALLMENT_BYTES < j) {
            System.arraycopy(a, i, savings.get(used >> INSTALLMENT_BITS),
                    0, INSTALLMENT_BYTES);
            i += INSTALLMENT_BYTES;
            used += INSTALLMENT_BYTES;
        }

        if (i < j) {
            System.arraycopy(a, i, savings.get(used >> INSTALLMENT_BITS),
                    0, j - i);
            used += j - i;
        }

        return this;
    }

    public InstallmentByteBuffer append(int b) {
        if (!readonly()) {
            setupCapacity(used + 1);
            put(used++, (byte) (b & 0xFF));
        }
        return this;
    }

    public InstallmentByteBuffer append(String s) {
        return append(s.getBytes());
    }

    /**
     * @return a copy of valid in bounds byte array
     */
    private byte[] array() {
        byte[] array = new byte[used];
        int i = 0;
        while (i + INSTALLMENT_BYTES <= used) {
            System.arraycopy(savings.get(i >> INSTALLMENT_BITS),
                    0, array, i, INSTALLMENT_BYTES);
            i += INSTALLMENT_BYTES;
        }
        System.arraycopy(savings.get(i >> INSTALLMENT_BITS),
                0, array, i, used - i);
        return array;
    }

    public int capacity() {
        return savings.size() << INSTALLMENT_BITS;
    }

    private int get(int pos) {
        return savings.get(pos >> INSTALLMENT_BITS)[pos & INSTALLMENT_MASK];
    }

    public boolean isEmpty() {
        return used == 0;
    }

    private void put(int pos, byte b) {
        savings.get(pos >> INSTALLMENT_BITS)[pos & INSTALLMENT_MASK] = b;
    }

    public Reader reader() {
        return new Reader();
    }

    public boolean readonly() {
        return readonly;
    }

    /**
     * Writable to read only is a one-way street.
     */
    public void readonly(boolean ro) {
        if (readonly) {
            return;
        }
        readonly = ro;
    }

    public void rewind() {
        seek(0);
    }

    public void seek(int pos) {
        if (readonly) {
            return;
        }
        if (pos >= 0 && pos < used) {
            used = pos;
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    private void setupCapacity(int newLength) {
        if (savings == null) {
            savings = new ArrayList<>();
        }
        if (newLength > capacity()) {
            int n = newLength >> INSTALLMENT_BITS;
            if ((newLength & INSTALLMENT_MASK) != 0) {
                ++n;
            }
            for (int i = savings.size() + 1; i <= n; ++i) {
                savings.add(new byte[INSTALLMENT_BYTES]);
            }
        }
    }

    public int size() {
        return used;
    }

    public byte[] toByteArray() {
        return array();
    }

    @Override
    public String toString() {
        try {
            return new String(array(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new Error("Not support utf8");
        }
    }

    /**
     * For writing, erase every slot's content and reset size.<br/>
     */
    public void wipe() {
        if (readonly) {
            return;
        }
        for (byte[] a : savings) {
            Arrays.fill(a, (byte) 0);
        }
        used = 0;
    }
}
