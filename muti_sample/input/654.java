public class Bash {
    static class TestReference extends SoftReference {
        public static TestReference head;
        public TestReference next;
        public TestReference(Object referent) {
            super(referent);
            next = head;
            head = this;
        }
    }
    final static int NUM_BLOCKS = 2048;
    final static int BLOCK_SIZE = 32768;
    public static void main(String[] args) throws Exception {
        for (int i = 0; i < NUM_BLOCKS; ++ i) {
            TestReference ref = new TestReference(new byte[BLOCK_SIZE]);
        }
        int emptyCount = 0;
        int fullCount = 0;
        for (TestReference r = TestReference.head; r != null; r = r.next) {
            if (r.get() == null) emptyCount++;
            else fullCount++;
        }
        System.err.println(fullCount + " full, " + emptyCount + " empty ");
    }
}
