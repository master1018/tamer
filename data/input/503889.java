@TestTargetClass(AbstractSet.class)
public class AbstractSetTest extends TestCase {
    class Mock_AbstractSet extends AbstractSet{
        @Override
        public Iterator iterator() {
            return new Iterator() {
                public boolean hasNext() {
                    return false;
                }
                public Object next() {
                    return null;
                }
                public void remove() {
                }
            };
        }
        @Override
        public int size() {
            return 0;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void testHashCode() {
        AbstractSet as = new Mock_AbstractSet();
        assertNotNull(as.hashCode());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "equals",
            args = {java.lang.Object.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "AbstractSet",
            args = {}
        )
    })
    public void testEquals() {
        AbstractSet as1 = new Mock_AbstractSet();
        AbstractSet as2 = new Mock_AbstractSet();
        assertTrue(as1.equals(as2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "removeAll",
        args = {java.util.Collection.class}
    )
    public void testRemoveAll() {
        AbstractSet as = new AbstractSet(){
            @Override
            public Iterator iterator() {
                return new Iterator() {
                    public boolean hasNext() {
                        return true;
                    }
                    public Object next() {
                        return null;
                    }
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
            @Override
            public int size() {
                return 10;
            }
        };
        try {
            as.removeAll(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
        Collection c = new Vector();
        c.add(null);
        try {
            as.removeAll(c);
            fail("UnsupportedOperationException expected");
        } catch (UnsupportedOperationException e) {
        }
        as = new Mock_AbstractSet();
        as.removeAll(c);
    }
}
