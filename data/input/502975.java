public class HashMapPerfTest extends TestCase {
    private static final Random sRandom = new Random(1);
    class StringThing {
        String mId;
        public StringThing() {
            int len = sRandom.nextInt(20) + 1;
            char[] chars = new char[len];
            chars[0] = 't';
            for (int i = 1; i < len; i++) {
                chars[i] = (char) ('q' + sRandom.nextInt(4));
            }
            mId = new String(chars, 0, len);
        }
        public String getId() {
            return mId;
        }
    }
    private static final int NUM_ELTS = 1000;
    private static final int ITERS = 100;
    String[] keyCopies = new String[NUM_ELTS];
    private static final boolean lookupByOriginals = false;
    @LargeTest
    public void testHashMapPerformance() throws Exception {
        StringThing[] st = new StringThing[NUM_ELTS];
        for (int i = 0; i < NUM_ELTS; i++) {
            st[i] = new StringThing();
            keyCopies[i] = st[i].getId();
        }
        long start = SystemClock.uptimeMillis();
        for (int i = 0; i < ITERS; i++) {
            HashMap<String, StringThing> map = new HashMap<String, StringThing>();
            for (int j = 0; j < NUM_ELTS; j++) {
                StringThing s = st[i];
                map.put(s.getId(), s);
            }
            for (int j = 0; j < NUM_ELTS; j++) {
                if (lookupByOriginals) {
                    StringThing s = st[i];
                    map.get(s.getId());
                } else {
                    map.get(keyCopies[j]);
                }
            }
        }
        long finish = SystemClock.uptimeMillis();
    }
}
