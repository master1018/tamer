@TestTargetClass(Modifier.class) 
public class ModifierTest extends junit.framework.TestCase {
    private static final int ALL_FLAGS = 0x7FF;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "The only thing I can do",
        method = "Modifier",
        args = {}
    )
    public void test_Constructor() {
        assertNotNull(new Modifier());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isAbstract",
        args = {int.class}
    )
    public void test_isAbstractI() {
        assertTrue("ABSTRACT returned false", Modifier.isAbstract(ALL_FLAGS));
        assertTrue("ABSTRACT returned false", Modifier
                .isAbstract(Modifier.ABSTRACT));
        assertTrue("Non-ABSTRACT returned true", !Modifier
                .isAbstract(Modifier.TRANSIENT));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isFinal",
        args = {int.class}
    )
    public void test_isFinalI() {
        assertTrue("FINAL returned false", Modifier.isFinal(ALL_FLAGS));
        assertTrue("FINAL returned false", Modifier.isFinal(Modifier.FINAL));
        assertTrue("Non-FINAL returned true", !Modifier
                .isFinal(Modifier.TRANSIENT));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isInterface",
        args = {int.class}
    )
    public void test_isInterfaceI() {
        assertTrue("INTERFACE returned false", Modifier.isInterface(ALL_FLAGS));
        assertTrue("INTERFACE returned false", Modifier
                .isInterface(Modifier.INTERFACE));
        assertTrue("Non-INTERFACE returned true", !Modifier
                .isInterface(Modifier.TRANSIENT));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isNative",
        args = {int.class}
    )
    public void test_isNativeI() {
        assertTrue("NATIVE returned false", Modifier.isNative(ALL_FLAGS));
        assertTrue("NATIVE returned false", Modifier.isNative(Modifier.NATIVE));
        assertTrue("Non-NATIVE returned true", !Modifier
                .isNative(Modifier.TRANSIENT));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isPrivate",
        args = {int.class}
    )
    public void test_isPrivateI() {
        assertTrue("PRIVATE returned false", Modifier.isPrivate(ALL_FLAGS));
        assertTrue("PRIVATE returned false", Modifier
                .isPrivate(Modifier.PRIVATE));
        assertTrue("Non-PRIVATE returned true", !Modifier
                .isPrivate(Modifier.TRANSIENT));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isProtected",
        args = {int.class}
    )
    public void test_isProtectedI() {
        assertTrue("PROTECTED returned false", Modifier.isProtected(ALL_FLAGS));
        assertTrue("PROTECTED returned false", Modifier
                .isProtected(Modifier.PROTECTED));
        assertTrue("Non-PROTECTED returned true", !Modifier
                .isProtected(Modifier.TRANSIENT));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isPublic",
        args = {int.class}
    )
    public void test_isPublicI() {
        assertTrue("PUBLIC returned false", Modifier.isPublic(ALL_FLAGS));
        assertTrue("PUBLIC returned false", Modifier.isPublic(Modifier.PUBLIC));
        assertTrue("Non-PUBLIC returned true", !Modifier
                .isPublic(Modifier.TRANSIENT));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isStatic",
        args = {int.class}
    )
    public void test_isStaticI() {
        assertTrue("STATIC returned false", Modifier.isStatic(ALL_FLAGS));
        assertTrue("STATIC returned false", Modifier.isStatic(Modifier.STATIC));
        assertTrue("Non-STATIC returned true", !Modifier
                .isStatic(Modifier.TRANSIENT));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isStrict",
        args = {int.class}
    )
    public void test_isStrictI() {
        assertTrue("STRICT returned false", Modifier.isStrict(Modifier.STRICT));
        assertTrue("Non-STRICT returned true", !Modifier
                .isStrict(Modifier.TRANSIENT));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isSynchronized",
        args = {int.class}
    )
    public void test_isSynchronizedI() {
        assertTrue("Synchronized returned false", Modifier
                .isSynchronized(ALL_FLAGS));
        assertTrue("Non-Synchronized returned true", !Modifier
                .isSynchronized(Modifier.VOLATILE));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isTransient",
        args = {int.class}
    )
    public void test_isTransientI() {
        assertTrue("Transient returned false", Modifier.isTransient(ALL_FLAGS));
        assertTrue("Transient returned false", Modifier
                .isTransient(Modifier.TRANSIENT));
        assertTrue("Non-Transient returned true", !Modifier
                .isTransient(Modifier.VOLATILE));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isVolatile",
        args = {int.class}
    )
    public void test_isVolatileI() {
        assertTrue("Volatile returned false", Modifier.isVolatile(ALL_FLAGS));
        assertTrue("Volatile returned false", Modifier
                .isVolatile(Modifier.VOLATILE));
        assertTrue("Non-Volatile returned true", !Modifier
                .isVolatile(Modifier.TRANSIENT));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {int.class}
    )
    public void test_toStringI() {
        assertTrue("Returned incorrect string value: "
                + Modifier.toString(java.lang.reflect.Modifier.PUBLIC
                        + java.lang.reflect.Modifier.ABSTRACT), Modifier
                .toString(
                        java.lang.reflect.Modifier.PUBLIC
                                + java.lang.reflect.Modifier.ABSTRACT).equals(
                        "public abstract"));
    }
    protected void setUp() {
    }
    protected void tearDown() {
    }
}
