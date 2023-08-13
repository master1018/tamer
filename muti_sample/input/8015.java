public class ViewSynch {
    static final Integer ZERO = new Integer(0);
    static final Int INT_ZERO = new Int(0);
    static final Int INT_ONE = new Int(1);
    static SortedMap m = Collections.synchronizedSortedMap(new TreeMap());
    static Map m2 = m.tailMap(ZERO);
    static Collection c = m2.values();
    public static void main(String[] args) {
        for (int i=0; i<10000; i++)
            m.put(new Integer(i), INT_ZERO);
        new Thread() {
            public void run() {
                for (int i=0; i<100; i++) {
                    Thread.yield();
                    m.remove(ZERO);
                    m.put(ZERO, INT_ZERO);
                }
            }
        }.start();
        c.contains(INT_ONE);
    }
}
class Int {
    Integer x;
    Int(int i) {x = new Integer(i);}
    public boolean equals(Object o) {
        Thread.yield();
        Int i = (Int)o;
        return x.equals(i.x);
    }
    public int hashCode() {return x.hashCode();}
}
