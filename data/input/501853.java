@TestTargetClass(BasicPermission.class)
public class BasicPermissionTest extends TestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(BasicPermissionTest.class);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "BasicPermission",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "BasicPermission",
            args = {java.lang.String.class, java.lang.String.class}
        )
    })
    public void testCtor()
    {
        String name = "basic123*$%#";
        BasicPermission test = new BasicPermission(name){};
        assertEquals(name, test.getName());
        assertEquals("", test.getActions());
        test = new BasicPermission(name, "#$!#12435"){};
        assertEquals(name, test.getName());
        assertEquals("", test.getActions());
        try{
            new BasicPermission(null){};
            fail("NPE is not thrown");
        }
        catch (NullPointerException ok){}
        try{
            new BasicPermission(null, "ds235"){};
            fail("NPE is not thrown");
        }
        catch (NullPointerException ok){}
        try{
            new BasicPermission(""){};
            fail("IAE is not thrown");
        }
        catch (IllegalArgumentException ok){}
        try{
            new BasicPermission("", "ertre 3454"){};
            fail("IAE is not thrown");
        }
        catch (IllegalArgumentException ok){}
    }
    private final class BasicPermissionImpl extends BasicPermission
    {
        public BasicPermissionImpl(String name)
        {
            super(name);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void testEquals()
    {
        BasicPermission b1 = new BasicPermissionImpl("abc");
        BasicPermission b2 = null;
        assertTrue(b1.equals(b1)); 
        assertFalse(b1.equals(null));
        assertFalse(b1.equals(new Object()));
        assertFalse(b1.equals("abc"));
        assertTrue(b1.equals(b2 = new BasicPermissionImpl("abc")));
        assertTrue(b1.hashCode() == b2.hashCode());
        assertFalse(b1.equals(new BasicPermission("abc"){}));
        assertFalse(b1.equals(new BasicPermissionImpl("abc.*")));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "implies",
        args = {java.security.Permission.class}
    )
    public void testImplies()
    {
        BasicPermission b1 = new BasicPermissionImpl("a.b.c");
        assertTrue(b1.implies(b1));
        assertTrue(b1.implies(new BasicPermissionImpl("a.b.c")));
        assertFalse(b1.implies(new BasicPermissionImpl("a.b.c.*")));
        assertFalse(b1.implies(new BasicPermission("a.b.c"){}));
        assertTrue(new BasicPermissionImpl("a.b.*").implies(b1));
        assertTrue(new BasicPermissionImpl("a.*").implies(b1));
        assertTrue(new BasicPermissionImpl("*").implies(b1));
        assertFalse(new BasicPermissionImpl("a.b*").implies(b1));
        assertFalse(new BasicPermissionImpl("a.b.c.*").implies(b1));
        assertTrue(new BasicPermissionImpl("1.*").implies(new BasicPermissionImpl("1.234.*")));
        assertTrue(new BasicPermissionImpl("*").implies(new BasicPermissionImpl("*")));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "newPermissionCollection",
        args = {}
    )
    public void testCollection()
    {
        BasicPermission b1 = new BasicPermissionImpl("a.b.c");
        PermissionCollection pc1 = b1.newPermissionCollection();
        PermissionCollection pc2 = b1.newPermissionCollection();
        assertNotSame(pc1, pc2);
    }
}
