@TestTargetClass(java.util.Collection.class)
public class Support_CollectionTest extends junit.framework.TestCase { 
    Collection<Integer> col; 
    public Support_CollectionTest(String p1) {
        super(p1);
    }
    public Support_CollectionTest(String p1, Collection<Integer> c) {
        super(p1);
        col = c;
    }
    @Override
    public void runTest() {
        new Support_UnmodifiableCollectionTest("", col).runTest();
        Collection<Integer> myCollection = new TreeSet<Integer>();
        myCollection.add(new Integer(101));
        myCollection.add(new Integer(102));
        myCollection.add(new Integer(103));
        assertTrue("CollectionTest - a) add did not work", col.add(new Integer(
                101)));
        assertTrue("CollectionTest - b) add did not work", col
                .contains(new Integer(101)));
        assertTrue("CollectionTest - a) remove did not work", col
                .remove(new Integer(101)));
        assertTrue("CollectionTest - b) remove did not work", !col
                .contains(new Integer(101)));
        assertTrue("CollectionTest - a) addAll failed", col
                .addAll(myCollection));
        assertTrue("CollectionTest - b) addAll failed", col
                .containsAll(myCollection));
        assertTrue("CollectionTest - a) containsAll failed", col
                .containsAll(myCollection));
        col.remove(new Integer(101));
        assertTrue("CollectionTest - b) containsAll failed", !col
                .containsAll(myCollection));
        assertTrue("CollectionTest - a) removeAll failed", col
                .removeAll(myCollection));
        assertTrue("CollectionTest - b) removeAll failed", !col
                .removeAll(myCollection)); 
        assertTrue("CollectionTest - c) removeAll failed", !col
                .contains(new Integer(102)));
        assertTrue("CollectionTest - d) removeAll failed", !col
                .contains(new Integer(103)));
        col.addAll(myCollection);
        assertTrue("CollectionTest - a) retainAll failed", col
                .retainAll(myCollection));
        assertTrue("CollectionTest - b) retainAll failed", !col
                .retainAll(myCollection)); 
        assertTrue("CollectionTest - c) retainAll failed", col
                .containsAll(myCollection));
        assertTrue("CollectionTest - d) retainAll failed", !col
                .contains(new Integer(0)));
        assertTrue("CollectionTest - e) retainAll failed", !col
                .contains(new Integer(50)));
        col.clear();
        assertTrue("CollectionTest - a) clear failed", col.isEmpty());
        assertTrue("CollectionTest - b) clear failed", !col
                .contains(new Integer(101)));
    }
}
