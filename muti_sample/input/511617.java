@TestTargetClass(Group.class)
public class IGroupTest extends TestCase {
    class MyGroup extends GroupImpl {
        public MyGroup(String str) {
            super(str);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "addMember",
            args = {java.security.Principal.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "isMember",
            args = {java.security.Principal.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "removeMember",
            args = {java.security.Principal.class}
        )
    })
    public void test_addMember() {
        MyGroup gr = new MyGroup("TestOwners");
        Principal pr = new PrincipalImpl("TestPrincipal");
        try {
            assertTrue(gr.addMember(pr));
            assertFalse(gr.addMember(pr));
            assertTrue(gr.isMember(pr));
            assertTrue(gr.removeMember(pr));
            assertFalse(gr.isMember(pr));
            assertFalse(gr.removeMember(pr));
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "members",
        args = {}
    )
    public void test_members() {
        MyGroup gr = new MyGroup("TestOwners");
        Principal pr = new PrincipalImpl("TestPrincipal");
        try {
            Enumeration en = gr.members();
            assertFalse("Not empty enumeration", en.hasMoreElements());
            assertTrue(gr.addMember(pr));
            assertTrue("Empty enumeration", en.hasMoreElements());
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
}