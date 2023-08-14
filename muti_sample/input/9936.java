public class SharedNameTable extends Name.Table {
    private static List<SoftReference<SharedNameTable>> freelist = List.nil();
    static public synchronized SharedNameTable create(Names names) {
        while (freelist.nonEmpty()) {
            SharedNameTable t = freelist.head.get();
            freelist = freelist.tail;
            if (t != null) {
                return t;
            }
        }
        return new SharedNameTable(names);
    }
    static private synchronized void dispose(SharedNameTable t) {
        freelist = freelist.prepend(new SoftReference<SharedNameTable>(t));
    }
    private NameImpl[] hashes;
    public byte[] bytes;
    private int hashMask;
    private int nc = 0;
    public SharedNameTable(Names names, int hashSize, int nameSize) {
        super(names);
        hashMask = hashSize - 1;
        hashes = new NameImpl[hashSize];
        bytes = new byte[nameSize];
    }
    public SharedNameTable(Names names) {
        this(names, 0x8000, 0x20000);
    }
    @Override
    public Name fromChars(char[] cs, int start, int len) {
        int nc = this.nc;
        byte[] bytes = this.bytes;
        while (nc + len * 3 >= bytes.length) {
            byte[] newnames = new byte[bytes.length * 2];
            System.arraycopy(bytes, 0, newnames, 0, bytes.length);
            bytes = this.bytes = newnames;
        }
        int nbytes = Convert.chars2utf(cs, start, bytes, nc, len) - nc;
        int h = hashValue(bytes, nc, nbytes) & hashMask;
        NameImpl n = hashes[h];
        while (n != null &&
                (n.getByteLength() != nbytes ||
                !equals(bytes, n.index, bytes, nc, nbytes))) {
            n = n.next;
        }
        if (n == null) {
            n = new NameImpl(this);
            n.index = nc;
            n.length = nbytes;
            n.next = hashes[h];
            hashes[h] = n;
            this.nc = nc + nbytes;
            if (nbytes == 0) {
                this.nc++;
            }
        }
        return n;
    }
    @Override
    public Name fromUtf(byte[] cs, int start, int len) {
        int h = hashValue(cs, start, len) & hashMask;
        NameImpl n = hashes[h];
        byte[] names = this.bytes;
        while (n != null &&
                (n.getByteLength() != len || !equals(names, n.index, cs, start, len))) {
            n = n.next;
        }
        if (n == null) {
            int nc = this.nc;
            while (nc + len > names.length) {
                byte[] newnames = new byte[names.length * 2];
                System.arraycopy(names, 0, newnames, 0, names.length);
                names = this.bytes = newnames;
            }
            System.arraycopy(cs, start, names, nc, len);
            n = new NameImpl(this);
            n.index = nc;
            n.length = len;
            n.next = hashes[h];
            hashes[h] = n;
            this.nc = nc + len;
            if (len == 0) {
                this.nc++;
            }
        }
        return n;
    }
    @Override
    public void dispose() {
        dispose(this);
    }
    static class NameImpl extends Name {
        NameImpl next;
        int index;
        int length;
        NameImpl(SharedNameTable table) {
            super(table);
        }
        @Override
        public int getIndex() {
            return index;
        }
        @Override
        public int getByteLength() {
            return length;
        }
        @Override
        public byte getByteAt(int i) {
            return getByteArray()[index + i];
        }
        @Override
        public byte[] getByteArray() {
            return ((SharedNameTable) table).bytes;
        }
        @Override
        public int getByteOffset() {
            return index;
        }
        public int hashCode() {
            return index;
        }
        public boolean equals(Object other) {
            if (other instanceof Name)
                return
                    table == ((Name)other).table && index == ((Name) other).getIndex();
            else return false;
        }
    }
}
