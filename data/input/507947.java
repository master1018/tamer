@TestTargetClass(Permissions.class)
public class PermissionsTest extends TestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(PermissionsTest.class);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "add",
        args = {java.security.Permission.class}
    )
    public void testAdd() {
        Permissions ps = new Permissions();
        Permission ap = new AllPermission();
        Permission bp = new BasicPermission("jhb23jhg5") {
        };
        Permission sp0 = new SecurityPermission("abc");
        Permission sp1 = new SecurityPermission("a.b.c");
        Permission sp2 = new SecurityPermission("a.b.*");
        Permission sp3 = new SecurityPermission("a.*");
        Permission up1 = new UnresolvedPermission("131234", null, null, null);
        Permission up2 = new UnresolvedPermission("KUJKHVKJgyuygjhb", "xcv456",
            "26r ytf", new java.security.cert.Certificate[0]);
        Permission[] arr = new Permission[] {
            up1, up2, ap, bp, sp0, sp1, sp2, sp3,  };
        for (int i = 0; i < arr.length; i++) {
            ps.add(arr[i]);
        }
        ps.add(up1);
        ps.add(sp0);
        ps.setReadOnly();
        try {
            ps.add(up1);
            fail("read-only flag is ignored");
        } catch (SecurityException ok) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "elements",
        args = {}
    )
    public void testElements() {
        Permissions ps = new Permissions();
        Permission ap = new AllPermission();
        Permission bp = new BasicPermission("jhb23jhg5") {
            public PermissionCollection newPermissionCollection() {
                return null;
            }
        };
        Permission sp = new SecurityPermission("abc");
        Permission up1 = new UnresolvedPermission("131234", null, null, null);
        Permission up2 = new UnresolvedPermission("KUJKHVKJgyuygjhb", "xcv456",
            "26r ytf", new java.security.cert.Certificate[0]);
        Enumeration<Permission> en = ps.elements();
        assertNotNull(en);
        assertFalse(en.hasMoreElements());
        ps.add(up1);
        en = ps.elements();
        assertTrue(en.hasMoreElements());
        assertTrue(up1.equals(en.nextElement()));
        assertFalse(en.hasMoreElements());
        ps.add(up1);
        en = ps.elements();
        assertTrue(en.hasMoreElements());
        assertTrue(up1.equals(en.nextElement()));
        Permission[] arr = new Permission[] {
            ap, bp, sp, up1, up2 };
        for (int i = 0; i < arr.length; i++) {
            ps.add(arr[i]);
        }
        en = ps.elements();
        Collection<Permission> els = new ArrayList<Permission>();
        while (en.hasMoreElements()) {
            els.add(en.nextElement());
        }
        assertTrue(els.containsAll(Arrays.asList(arr)));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "implies",
            args = {java.security.Permission.class}
        )
    })
    public void testNull(){
        Permissions ps = new Permissions();
        try {
            ps.elements().nextElement();
            fail("should throw NoSuchElementException");
        } catch (NoSuchElementException e) {}
        try {
            ps.implies(null);
            fail("should throw NPE");
        } catch (NullPointerException e){
        }
        try {    
            ps.add(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e){}
    }
 }
