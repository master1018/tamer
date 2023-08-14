public class TreeMapTest extends TestCase {
    private Random mRandom = new Random(1);
    private static final boolean SPEW = false;
    @LargeTest
    public void testTreeMap() {
        for (int i = 0; i < 10; i++) {
            if (SPEW) System.out.println("Running doTest cycle #" + (i + 1));
            doTest();
        }
    }
    private void doTest() {
        TreeMap<Integer, String> tm = new TreeMap<Integer, String>();
        HashMap<Integer, String> hm = new HashMap<Integer, String>();
        int minVal = Integer.MAX_VALUE;
        int maxVal = Integer.MIN_VALUE;
        for (int i = 0; i < 100; i++) {
            int val = mRandom.nextInt(1000);
            if (SPEW) System.out.println("Adding val = " + val);
            if (val < minVal) {
                minVal = val;
            }
            if (val > maxVal) {
                maxVal = val;
            }
            tm.put(new Integer(val), "V:" + val);
            hm.put(new Integer(val), "V:" + val);
            if (SPEW) System.out.println("tm = " + tm);
            if (SPEW) System.out.println("tm.size() = " + tm.size());
            if (SPEW) System.out.println("hm.size() = " + hm.size());
            assertEquals(tm.size(), hm.size());
            if (SPEW) System.out.println("tm.firstKey() = " + tm.firstKey());
            if (SPEW) System.out.println("minVal = " + minVal);
            if (SPEW) System.out.println("tm.lastKey() = " + tm.lastKey());
            if (SPEW) System.out.println("maxVal = " + maxVal);
            assertEquals(minVal, tm.firstKey().intValue());
            assertEquals(maxVal, tm.lastKey().intValue());
        }
        for (int val = 0; val < 1000; val++) {
            Integer vv = new Integer(val);
            String tms = tm.get(vv);
            String hms = hm.get(vv);
            assertEquals(tms, hms);
        }
        for (int i = 0; i < 1000; i++) {
            int val = mRandom.nextInt(1000);
            if (SPEW) System.out.println("Removing val = " + val);
            String tms = tm.remove(new Integer(val));
            String hms = hm.remove(new Integer(val));
            if (SPEW) System.out.println("tm = " + tm);
            assertEquals(tm.size(), hm.size());
            assertEquals(tms, hms);
        }
        for (int val = 0; val < 1000; val++) {
            Integer vv = new Integer(val);
            String tms = tm.get(vv);
            String hms = hm.get(vv);
            assertEquals(tms, hms);
        }
    }
}
