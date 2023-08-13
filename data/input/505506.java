public class TreeSetTest extends PerformanceTestBase {
    public static final int ITERATIONS = 1000;
    public static TreeSet<Integer> sSet;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        sSet = new TreeSet<Integer>();
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
    public void testTreeSetAdd() {
        TreeSet<Integer> set = new TreeSet();
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
    public void testTreeSetFirst() {
        int value;
        TreeSet<Integer> set = sSet;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            value = set.first();
            value = set.first();
            value = set.first();
            value = set.first();
            value = set.first();
            value = set.first();
            value = set.first();
            value = set.first();
            value = set.first();
        }
    }
    public void testTreeSetLast() {
        int value;
        TreeSet<Integer> set = sSet;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            value = set.last();
            value = set.last();
            value = set.last();
            value = set.last();
            value = set.last();
            value = set.last();
            value = set.last();
            value = set.last();
            value = set.last();
        }
    }
    public void testTreeSetContains() {
        Integer index = new Integer(500);
        boolean flag;
        TreeSet<Integer> set = sSet;
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
    public void testTreeSetSize() {
        int value;
        TreeSet<Integer> set = sSet;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            value = set.size();
            value = set.size();
            value = set.size();
            value = set.size();
            value = set.size();
            value = set.size();
            value = set.size();
            value = set.size();
            value = set.size();
        }
    }
    public void testTreeSetIterator() {
        Iterator iterator;
        TreeSet<Integer> set = sSet;
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
    public void testTreeSetComparator() {
        Comparator comparator;
        TreeSet<Integer> set = sSet;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            comparator = set.comparator();
            comparator = set.comparator();
            comparator = set.comparator();
            comparator = set.comparator();
            comparator = set.comparator();
            comparator = set.comparator();
            comparator = set.comparator();
            comparator = set.comparator();
            comparator = set.comparator();
        }
    }
    public void testTreeSetClone() {
        Object obj;
        TreeSet<Integer> set = sSet;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            obj = set.clone();
            obj = set.clone();
            obj = set.clone();
            obj = set.clone();
            obj = set.clone();
            obj = set.clone();
            obj = set.clone();
            obj = set.clone();
            obj = set.clone();
            obj = set.clone();
        }
    }
    @SuppressWarnings("unchecked")
    public void testTreeSetRemove() {
        TreeSet<Integer> set = new TreeSet(sSet);
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
    public void testTreeSetHeadSet() {
        Integer value = new Integer(100);
        SortedSet set;
        TreeSet<Integer> tSet = sSet;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            set = tSet.headSet(value);
            set = tSet.headSet(value);
            set = tSet.headSet(value);
            set = tSet.headSet(value);
            set = tSet.headSet(value);
            set = tSet.headSet(value);
            set = tSet.headSet(value);
            set = tSet.headSet(value);
            set = tSet.headSet(value);
            set = tSet.headSet(value);
        }
    }
    public void testTreeSetSubSet() {
        Integer value = new Integer(400);
        Integer nInt = new Integer(500);
        SortedSet set;
        TreeSet<Integer> tSet = sSet;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            set = tSet.subSet(value, nInt);
            set = tSet.subSet(value, nInt);
            set = tSet.subSet(value, nInt);
            set = tSet.subSet(value, nInt);
            set = tSet.subSet(value, nInt);
            set = tSet.subSet(value, nInt);
            set = tSet.subSet(value, nInt);
            set = tSet.subSet(value, nInt);
            set = tSet.subSet(value, nInt);
            set = tSet.subSet(value, nInt);
        }
    }
    public void testTreeSetTailSet() {
        Integer value = new Integer(900);
        SortedSet set;
        TreeSet<Integer> tSet = sSet;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            set = tSet.tailSet(value);
            set = tSet.tailSet(value);
            set = tSet.tailSet(value);
            set = tSet.tailSet(value);
            set = tSet.tailSet(value);
            set = tSet.tailSet(value);
            set = tSet.tailSet(value);
            set = tSet.tailSet(value);
            set = tSet.tailSet(value);
            set = tSet.tailSet(value);
        }
    }
    public void testTreeSetIsEmpty() {
        boolean flag;
        TreeSet<Integer> tSet = sSet;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            flag = tSet.isEmpty();
            flag = tSet.isEmpty();
            flag = tSet.isEmpty();
            flag = tSet.isEmpty();
            flag = tSet.isEmpty();
            flag = tSet.isEmpty();
            flag = tSet.isEmpty();
            flag = tSet.isEmpty();
            flag = tSet.isEmpty();
            flag = tSet.isEmpty();
        }
    }
}
