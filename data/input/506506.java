@TestTargetClass(Map.Entry.class)
public class MapEntryTest extends TestCase {
    Map.Entry me = null;
    HashMap   hm = null;
    Iterator  i  = null;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getKey",
        args = {}
    )
    public void testGetKey() {
        assertTrue(hm.containsKey(me.getKey()));
        hm.clear();
        try {
            me.getKey();
        } catch (IllegalStateException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getValue",
        args = {}
    )
    public void testGetValue() {
        assertTrue(hm.containsValue(me.getValue()));
        hm.clear();
        try {
            me.getValue();
        } catch (IllegalStateException e) {
        }
    }
    class Mock_HashMap extends HashMap {
        @Override
        public Object put(Object key, Object val) {
            if (val == null) throw new NullPointerException();
            if (val.getClass() == Double.class) throw new ClassCastException();
            if (((String)val).equals("Wrong element")) throw new IllegalArgumentException();
            throw new UnsupportedOperationException();
        }
        public Object fakePut(Object key, Object val) {
            return super.put(key, val);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "RI does not throw following exceptions: UnsupportedOperationException,ClassCastException, IllegalArgumentException and NullPointerException",
        method = "setValue",
        args = {java.lang.Object.class}
    )
    public void testSetValue() {
        Mock_HashMap mhm = new Mock_HashMap();
        mhm.fakePut(new Integer(1), "One");
        mhm.fakePut(new Integer(2), "Two");
        i = mhm.entrySet().iterator();
        me = (Map.Entry)i.next();
        me.setValue("Wrong element");
        hm.clear();
        try {
            me.setValue("");
        } catch (IllegalStateException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void testEquals() {
        Map.Entry me1 = (Map.Entry)i.next();
        assertFalse(me.equals(me1));
        assertFalse(me.equals(this));
        me1 = me;
        assertTrue(me.equals(me1));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void testHashCode() {
        Map.Entry me1 = (Map.Entry)i.next();
        assertTrue(me.hashCode() != me1.hashCode());
    }
    protected void setUp() throws Exception {
        hm = new HashMap();
        hm.put(new Integer(1), "one");
        hm.put(new Integer(2), "two");
        i = hm.entrySet().iterator();
        me = (Map.Entry)i.next();
        super.setUp();
    }
}
