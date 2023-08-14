public class _IntList
    extends TestCase {
    public void test_contains() {
        for (int sz = 0; sz < 100; sz++) {
            IntList list = new IntList(sz);
            for (int i = 0; i < sz; i++) {
                list.add(i * 2);
            }
            for (int i = (sz * 2) - 1; i >= 0; i--) {
                boolean contains = list.contains(i);
                if ((i & 1) == 0) {
                    assertTrue(label(sz, i), contains);
                } else {
                    assertFalse(label(sz, i), contains);
                }
            }
            assertFalse(label(sz, -1), list.contains(-1));
            assertFalse(label(sz, sz * 2), list.contains(sz * 2));
        }
    }
    public void test_addSorted() {
        IntList list = new IntList(2);
        list.add(9);
        list.add(12);
        assertTrue(list.contains(9));
        assertTrue(list.contains(12));
    }
    public void test_addUnsorted() {
        IntList list = new IntList(2);
        list.add(12);
        list.add(9);
        assertTrue(list.contains(12));
        assertTrue(list.contains(9));
    }
    private static String label(int n, int m) {
        return "(" + n + "/" + m + ")";
    }
}
