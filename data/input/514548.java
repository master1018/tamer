@TestTargetClass(Owner.class)
public class IOwnerTest extends TestCase {
    class MyOwner extends OwnerImpl {
        public MyOwner(Principal pr) {
            super(pr);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isOwner",
        args = {java.security.Principal.class}
    )
    public void test_isOwner() {
        MyOwner mo = new MyOwner(new PrincipalImpl("NewOwner"));
        try {
            assertFalse("Method returns TRUE", mo.isOwner(new PrincipalImpl("TestOwner")));
            assertTrue("Method returns FALSE", mo.isOwner(new PrincipalImpl("NewOwner")));
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "addOwner",
        args = {java.security.Principal.class, java.security.Principal.class}
    )
    public void test_addOwner() {
        Principal p1 = new PrincipalImpl("Owner");
        Principal p2 = new PrincipalImpl("AclOwner");
        Principal pt = new PrincipalImpl("NewOwner");
        MyOwner mo = new MyOwner(p1);
        try {
            assertTrue("Method returns FALSE", mo.addOwner(p1, pt));
            assertFalse("Method returns TRUE", mo.addOwner(p1, pt));
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
        try {
            mo.addOwner(p2, pt);
            fail("NotOwnerException was not thrown");
        } catch (NotOwnerException noe) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "deleteOwner",
        args = {java.security.Principal.class, java.security.Principal.class}
    )
    public void test_deleteOwner() {
        Principal caller = new PrincipalImpl("Owner");
        Principal owner1 = new PrincipalImpl("NewOwner1");
        Principal owner2 = new PrincipalImpl("NewOwner2");
        Principal notCaller = new PrincipalImpl("AclOwner");
        MyOwner mo = new MyOwner(caller);
        try {
            if (!mo.isOwner(owner1))  mo.addOwner(caller, owner1);
            if (!mo.isOwner(owner2))  mo.addOwner(caller, owner2);
        } catch (Exception e) {
            fail("Unexpected exception " + e + " was thrown for addOwner");
        }
        try {
            assertTrue("Method returns FALSE", mo.deleteOwner(caller, owner1));
            assertFalse("Object presents in the owner list", mo.isOwner(owner1));
            assertFalse("Method returns TRUE", mo.deleteOwner(caller, owner1));
            assertTrue("Method returns FALSE", mo.deleteOwner(caller, owner2));
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
        try {
            mo.deleteOwner(notCaller, owner1);
            fail("NotOwnerException was not thrown");
        } catch (NotOwnerException noe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of NotOwnerException");
        }
        try {
            mo.deleteOwner(caller, owner2);
            fail("LastOwnerException was not thrown");
        } catch (LastOwnerException loe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of LastOwnerException");
        }
    }
}