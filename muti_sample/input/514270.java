@TestTargetClass(SecurityPermission.class)
public class SecurityPermissionTest extends TestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(SecurityPermissionTest.class);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "SecurityPermission",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "SecurityPermission",
            args = {java.lang.String.class, java.lang.String.class}
        )
    })
    public void testCtor()
    {
        String name = "basic123*$%#";
        SecurityPermission test = new SecurityPermission(name);
        assertEquals(name, test.getName());
        assertEquals("", test.getActions());
        test = new SecurityPermission(name, "#$!#12435");
        assertEquals(name, test.getName());
        assertEquals("", test.getActions());
        try{
            new SecurityPermission(null);
            fail("NPE is not thrown");
        }
        catch (NullPointerException ok){}
        try{
            new SecurityPermission(null, "ds235");
            fail("NPE is not thrown");
        }
        catch (NullPointerException ok){}
        try{
            new SecurityPermission("");
            fail("IAE is not thrown");
        }
        catch (IllegalArgumentException ok){}
        try{
            new SecurityPermission("", "ertre 3454");
            fail("IAE is not thrown");
        }
        catch (IllegalArgumentException ok){} 
    }
}
