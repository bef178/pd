package pd.fenc;

import java.util.ArrayList;
import java.util.Arrays;

public class InstallmentByteBuffer {

    public class Provider implements Int8Provider {

        private int pos = 0;

        @Override
        public boolean hasNext() {
            return pos >= 0 && pos < size();
        }

        /**
         * values in [-0x80,0x7F]
         */
        @Override
        public int next() {
            return InstallmentByteBuffer.this.get(pos++);
        }

        @Override
        public int position() {
            return pos;
        }
    }

    private static final int INSTALLMENT_BITS = 10;

    private static final int INSTALLMENT_BYTES = 1 << INSTALLMENT_BITS;
    private static final int INSTALLMENT_MASK = INSTALLMENT_BYTES - 1;

    private ArrayList<byte[]> savings = new ArrayList<>();

    private int size = 0;

    public InstallmentByteBuffer() {
        this(INSTALLMENT_BYTES);
    }

    public InstallmentByteBuffer(int initialCapacity) {
        setupCapacity(initialCapacity);
    }

    public int capacity() {
        return savings.size() << INSTALLMENT_BITS;
    }

    /**
     * @return a copy of valid in bounds byte array
     */
    public byte[] copyBytes() {
        byte[] bytes = new byte[size];
        int i = 0;
        while (i + INSTALLMENT_BYTES <= size) {
            System.arraycopy(savings.get(i >> INSTALLMENT_BITS),
                    0, bytes, i, INSTALLMENT_BYTES);
            i += INSTALLMENT_BYTES;
        }
        System.arraycopy(savings.get(i >> INSTALLMENT_BITS),
                0, bytes, i, size - i);
        return bytes;
    }

    private byte get(int pos) {
        return savings.get(pos >> INSTALLMENT_BITS)[pos & INSTALLMENT_MASK];
    }

    public InstallmentByteBuffer push(byte[] a) {
        return push(a, 0, a.length);
    }

    public InstallmentByteBuffer push(byte[] a, int i, int j) {
        int n = j - i;
        setupCapacity(size + n);

        if ((size & INSTALLMENT_MASK) != 0) {
            int left = INSTALLMENT_BYTES - (size & INSTALLMENT_MASK);
            if (left > n) {
                left = n;
            }
            System.arraycopy(a, i, savings.get(size >> INSTALLMENT_BITS),
                    size & INSTALLMENT_MASK, left);
            i += left;
            size += left;
        }

        while (i + INSTALLMENT_BYTES < j) {
            System.arraycopy(a, i, savings.get(size >> INSTALLMENT_BITS),
                    0, INSTALLMENT_BYTES);
            i += INSTALLMENT_BYTES;
            size += INSTALLMENT_BYTES;
        }

        if (i < j) {
            System.arraycopy(a, i, savings.get(size >> INSTALLMENT_BITS),
                    0, j - i);
            size += j - i;
        }

        return this;
    }

    public InstallmentByteBuffer push(byte byteValue) {
        setupCapacity(size + 1);
        put(size++, byteValue);
        return this;
    }

    private void put(int pos, byte b) {
        savings.get(pos >> INSTALLMENT_BITS)[pos & INSTALLMENT_MASK] = b;
    }

    public Provider provider() {
        return new Provider();
    }

    private void setupCapacity(int newCapacity) {
        if (savings == null) {
            savings = new ArrayList<>();
        }
        if (newCapacity > capacity()) {
            int n = newCapacity >> INSTALLMENT_BITS;
            if ((newCapacity & INSTALLMENT_MASK) != 0) {
                ++n;
            }
            for (int i = savings.size() + 1; i <= n; ++i) {
                savings.add(new byte[INSTALLMENT_BYTES]);
            }
        }
    }

    public int size() {
        return size;
    }

    @Override
    public String toString() {
        return new String(copyBytes());
    }

    /**
     * For writing, erase every slot's content and reset size.<br/>
     */
    public void wipe() {
        for (byte[] a : savings) {
            Arrays.fill(a, (byte) 0);
        }
        size = 0;
    }
}
