public class HashSetTest extends PerformanceTestBase {
    public static final int ITERATIONS = 1000;
    public static HashSet<Integer> sSet;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        sSet = new HashSet<Integer>();
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            sSet.add(i);
        }
    }
    @Override
    public int startPerformance(PerformanceTestCase.Intermediates intermediates) {
        intermediates.setInternalIterations(ITERATIONS);
        return 0;
    }
    @SuppressWarnings("unchecked")
    public void testHashSetAdd() {
        HashSet set = new HashSet();
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            set.add(i);
            set.add(i);
            set.add(i);
            set.add(i);
            set.add(i);
            set.add(i);
            set.add(i);
            set.add(i);
            set.add(i);
            set.add(i);
        }
    }
    public void testHashSetContains() {
        Integer index = new Integer(500);
        boolean flag;
        HashSet set = sSet;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            flag = set.contains(index);
            flag = set.contains(index);
            flag = set.contains(index);
            flag = set.contains(index);
            flag = set.contains(index);
            flag = set.contains(index);
            flag = set.contains(index);
            flag = set.contains(index);
            flag = set.contains(index);
        }
    }
    public void testHashSetSize() {
        int num;
        HashSet set = sSet;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            num = set.size();
            num = set.size();
            num = set.size();
            num = set.size();
            num = set.size();
            num = set.size();
            num = set.size();
            num = set.size();
            num = set.size();
        }
    }
    public void testHashSetIterator() {
        Iterator iterator;
        HashSet set = sSet;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            iterator = set.iterator();
            iterator = set.iterator();
            iterator = set.iterator();
            iterator = set.iterator();
            iterator = set.iterator();
            iterator = set.iterator();
            iterator = set.iterator();
            iterator = set.iterator();
            iterator = set.iterator();
        }
    }
    @SuppressWarnings("unchecked")
    public void testHashSetRemove() {
        HashSet set = new HashSet(sSet);
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            set.remove(i);
            set.remove(i);
            set.remove(i);
            set.remove(i);
            set.remove(i);
            set.remove(i);
            set.remove(i);
            set.remove(i);
            set.remove(i);
            set.remove(i);
        }
    }
    public void testHashSetIsEmpty() {
        HashSet set = sSet;
        boolean flag;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            flag = set.isEmpty();
            flag = set.isEmpty();
            flag = set.isEmpty();
            flag = set.isEmpty();
            flag = set.isEmpty();
            flag = set.isEmpty();
            flag = set.isEmpty();
            flag = set.isEmpty();
            flag = set.isEmpty();
            flag = set.isEmpty();
        }
    }
    public void testHashSetClone() {
        HashSet hSet = sSet;
        Object set;
        for (int i = ITERATIONS - 1; i > 0; i--) {
            set = hSet.clone();
            set = hSet.clone();
            set = hSet.clone();
            set = hSet.clone();
            set = hSet.clone();
            set = hSet.clone();
            set = hSet.clone();
            set = hSet.clone();
            set = hSet.clone();
            set = hSet.clone();
        }
    }
}
