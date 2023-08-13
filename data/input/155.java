public class UnsharedNameTable extends Name.Table {
    static public Name.Table create(Names names) {
        return new UnsharedNameTable(names);
    }
    static class HashEntry extends WeakReference<NameImpl> {
        HashEntry next;
        HashEntry(NameImpl referent) {
            super(referent);
        }
    }
    private HashEntry[] hashes = null;
    private int hashMask;
    public int index;
    public UnsharedNameTable(Names names, int hashSize) {
        super(names);
        hashMask = hashSize - 1;
        hashes = new HashEntry[hashSize];
    }
    public UnsharedNameTable(Names names) {
        this(names, 0x8000);
    }
    @Override
    public Name fromChars(char[] cs, int start, int len) {
        byte[] name = new byte[len * 3];
        int nbytes = Convert.chars2utf(cs, start, name, 0, len);
        return fromUtf(name, 0, nbytes);
    }
    @Override
    public Name fromUtf(byte[] cs, int start, int len) {
        int h = hashValue(cs, start, len) & hashMask;
        HashEntry element = hashes[h];
        NameImpl n = null;
        HashEntry previousNonNullTableEntry = null;
        HashEntry firstTableEntry = element;
        while (element != null) {
            if (element == null) {
                break;
            }
            n = element.get();
            if (n == null) {
                if (firstTableEntry == element) {
                    hashes[h] = firstTableEntry = element.next;
                }
                else {
                    Assert.checkNonNull(previousNonNullTableEntry, "previousNonNullTableEntry cannot be null here.");
                    previousNonNullTableEntry.next = element.next;
                }
            }
            else {
                if (n.getByteLength() == len && equals(n.bytes, 0, cs, start, len)) {
                    return n;
                }
                previousNonNullTableEntry = element;
            }
            element = element.next;
        }
        byte[] bytes = new byte[len];
        System.arraycopy(cs, start, bytes, 0, len);
        n = new NameImpl(this, bytes, index++);
        System.arraycopy(cs, start, n.bytes, 0, len);
        HashEntry newEntry = new HashEntry(n);
        if (previousNonNullTableEntry == null) { 
            hashes[h] = newEntry;
        }
        else {
            Assert.checkNull(previousNonNullTableEntry.next, "previousNonNullTableEntry.next must be null.");
            previousNonNullTableEntry.next = newEntry;
        }
        return n;
    }
    @Override
    public void dispose() {
        hashes = null;
    }
    static class NameImpl extends Name {
        NameImpl(UnsharedNameTable table, byte[] bytes, int index) {
            super(table);
            this.bytes = bytes;
            this.index = index;
        }
        final byte[] bytes;
        final int index;
        @Override
        public int getIndex() {
            return index;
        }
        @Override
        public int getByteLength() {
            return bytes.length;
        }
        @Override
        public byte getByteAt(int i) {
            return bytes[i];
        }
        @Override
        public byte[] getByteArray() {
            return bytes;
        }
        @Override
        public int getByteOffset() {
            return 0;
        }
    }
}
