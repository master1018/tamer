public class CacheMapTest {
    public static void main(String[] args) {
        try {
            boolean ok = test(5) && test(100);
            if (ok) {
                System.out.println("Test completed");
                return;
            } else {
                System.out.println("Test failed!");
                System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("Unexpected exception: " + e);
            e.printStackTrace();
            System.exit(1);
        }
    }
    private static boolean test(int cacheSize) throws Exception {
        System.out.println("CacheMap test with cache size " + cacheSize);
        CacheMap map = new CacheMap(cacheSize);
        int size = 0;
        int maxIterations = cacheSize * 10;
        while (map.size() == size && size < maxIterations) {
            Integer key = new Integer(size);
            Object x = map.put(key, "x");
            if (x != null) {
                System.out.println("Map already had entry " + key + "!");
                return false;
            }
            x = map.get(key);
            if (!"x".equals(x)) {
                System.out.println("Got back surprising value: " + x);
                return false;
            }
            size++;
        }
        System.out.println("Map size is " + map.size() + " after inserting " +
                           size + " elements");
        do {
            System.gc();
            Thread.sleep(1);
            System.out.println("Map size is " + map.size() + " after GC");
        } while (map.size() > cacheSize);
        if (map.size() < cacheSize) {
            System.out.println("Map shrank to less than cache size: " +
                               map.size() + " (surprising but not wrong)");
        } else
            System.out.println("Map shrank to cache size as expected");
        int lowest = size - cacheSize;
        for (Iterator it = map.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            Integer x = (Integer) entry.getKey();
            int xx = x.intValue();
            if (xx < lowest || xx >= size) {
                System.out.println("Old value remained (" + x + "), " +
                                   "expected none earlier than " + lowest);
                return false;
            }
            Object xxx = entry.getValue();
            if (!"x".equals(xxx)) {
                System.out.println("Got back surprising value: " + xxx);
                return false;
            }
        }
        if (map.size() > 0)
            System.out.println("Remaining elements are the most recent ones");
        System.out.println("Test passed");
        return true;
    }
}
