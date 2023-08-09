public class InformationObjectSet {
    private final int capacity;
    private final Entry[][] pool;
    public InformationObjectSet() {
        this(64, 10);
    }
    public InformationObjectSet(int capacity, int size) {
        this.capacity = capacity;
        pool = new Entry[capacity][size];
    }
    public void put(AttributeType at) {
        put(at.oid.getOid(), at);
    }
    public void put(int[] oid, Object object) {
        int index = hashIntArray(oid) % capacity;
        Entry[] list = pool[index];
        int i = 0;
        for (; list[i] != null; i++) {
            if (Arrays.equals(oid, list[i].oid)) {
                throw new Error(); 
            }
        }
        if (i == (capacity - 1)) {
            throw new Error(); 
        }
        list[i] = new Entry(oid, object);
    }
    public Object get(int[] oid) {
        int index = hashIntArray(oid) % capacity;
        Entry[] list = pool[index];
        for (int i = 0; list[i] != null; i++) {
            if (Arrays.equals(oid, list[i].oid)) {
                return list[i].object;
            }
        }
        return null;
    }
    private int hashIntArray(int[] array) {
        int intHash = 0;
        for (int i = 0; i < array.length && i < 4; i++) {
            intHash += array[i] << (8 * i); 
        }
        return intHash & 0x7FFFFFFF; 
    }
    private static class Entry {
        public int[] oid;
        public Object object;
        public Entry(int[] oid, Object object) {
            this.oid = oid;
            this.object = object;
        }
    }
}
