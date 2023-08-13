@TestTargetClass(Dictionary.class)
public class DictionaryTest extends TestCase {
    class Mock_Dictionary extends Dictionary {
        @Override
        public Enumeration elements() {
            return null;
        }
        @Override
        public Object get(Object arg0) {
            return null;
        }
        @Override
        public boolean isEmpty() {
            return false;
        }
        @Override
        public Enumeration keys() {
            return null;
        }
        @Override
        public Object put(Object arg0, Object arg1) {
            return null;
        }
        @Override
        public Object remove(Object arg0) {
            return null;
        }
        @Override
        public int size() {
            return 0;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Dictionary",
        args = {}
    )
    public void testDictionary() {
        Dictionary md = new Mock_Dictionary();
        assertNotNull(md);
    }
}
