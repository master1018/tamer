public class toArray {
    public static void main(String[] args) throws Throwable {
        final Throwable throwable[] = new Throwable[1];
        final int maxSize = 1000;
        final ConcurrentHashMap<Integer, Integer> m
            = new ConcurrentHashMap<Integer, Integer>();
        final Thread t1 = new Thread() { public void run() {
            for (int i = 0; i < maxSize; i++)
                m.put(i,i);}};
        final Thread t2 = new Thread() {
            public Throwable exception = null;
            private int prevSize = 0;
            private boolean checkProgress(Object[] a) {
                int size = a.length;
                if (size < prevSize) throw new RuntimeException("WRONG WAY");
                if (size > maxSize)  throw new RuntimeException("OVERSHOOT");
                if (size == maxSize) return true;
                prevSize = size;
                return false;
            }
            public void run() {
                try {
                    Integer[] empty = new Integer[0];
                    while (true) {
                        if (checkProgress(m.values().toArray())) return;
                        if (checkProgress(m.keySet().toArray())) return;
                        if (checkProgress(m.values().toArray(empty))) return;
                        if (checkProgress(m.keySet().toArray(empty))) return;
                    }
                } catch (Throwable t) {
                   throwable[0] = t;
                }}};
        t2.start();
        t1.start();
        t1.join();
        t2.join();
        if (throwable[0] != null)
            throw throwable[0];
    }
}
