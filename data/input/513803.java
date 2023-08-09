@TestTargetClass(KeyRep.Type.class)
public class KeyRepTypeTest extends TestCase {
    protected void setUp() throws Exception {
        super.setUp();
    }
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "valueOf",
        args = {java.lang.String.class}
    )
    public void testValueOf() {
        try {
            KeyRep.Type.valueOf("type");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            KeyRep.Type.valueOf(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
        assertEquals(KeyRep.Type.PRIVATE, KeyRep.Type
                .valueOf(KeyRep.Type.PRIVATE.toString()));
        assertEquals(KeyRep.Type.PUBLIC, KeyRep.Type.valueOf(KeyRep.Type.PUBLIC
                .toString()));
        assertEquals(KeyRep.Type.SECRET, KeyRep.Type.valueOf(KeyRep.Type.SECRET
                .toString()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "values",
        args = {}
    )
    public void testValues() {
        KeyRep.Type[] types = new KeyRep.Type[] { KeyRep.Type.SECRET,
                KeyRep.Type.PUBLIC, KeyRep.Type.PRIVATE };
        try {
            assertTrue(Arrays.equals(types, KeyRep.Type.values()));
        } catch (Exception e) {
            fail("Unexpected exception " + e.getMessage());
        }
    }
}
