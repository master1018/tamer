@TestTargetClass(ConcurrentModificationException.class) 
public class ConcurrentModificationExceptionTest extends
        junit.framework.TestCase {
    static public class CollectionModifier implements Runnable {
        Collection col;
        boolean keepGoing = true;
        public CollectionModifier(Collection c) {
            col = c;
        }
        public void stopNow() {
            keepGoing = false;
        }
        public void run() {
            Object someItem = new Integer(-1);
            while (keepGoing) {
                col.add(someItem);
                col.remove(someItem);
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ConcurrentModificationException",
        args = {}
    )
    public void test_Constructor() {
        Collection myCollection = new LinkedList();
        Iterator myIterator = myCollection.iterator();
        for (int counter = 0; counter < 50; counter++)
            myCollection.add(new Integer(counter));
        CollectionModifier cm = new CollectionModifier(myCollection);
        Thread collectionSlapper = new Thread(cm);
        try {
            collectionSlapper.start();
            while (myIterator.hasNext())
                myIterator.next();
        } catch (ConcurrentModificationException e) {
            cm.stopNow();
            return;
        }
        cm.stopNow();
        fail("Failed to throw expected ConcurrentModificationException");
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ConcurrentModificationException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        String errorMessage = "This is an error message";
        try {
            if (true)
                throw new ConcurrentModificationException(errorMessage);
        } catch (ConcurrentModificationException e) {
            assertTrue("Exception thrown without error message", e.getMessage()
                    .equals(errorMessage));
            return;
        }
        fail("Failed to throw expected ConcurrentModificationException");
    }
    protected void setUp() {
    }
    protected void tearDown() {
    }
}
