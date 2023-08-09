@TestTargetClass(Subject.class) 
public class SubjectTest extends TestCase {
    SecurityManager old;
    @Override
    protected void setUp() throws Exception {
        old = System.getSecurityManager();
        super.setUp();
    }
    @Override
    protected void tearDown() throws Exception {
        System.setSecurityManager(old);
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Subject",
        args = {}
    )
    public void test_Constructor_01() {
        try {
            Subject s = new Subject();
            assertNotNull("Null object returned", s);
            assertTrue("Set of principal is not empty", s.getPrincipals().isEmpty());
            assertTrue("Set of private credentials is not empty", s.getPrivateCredentials().isEmpty());
            assertTrue("Set of public credentials is not empty", s.getPublicCredentials().isEmpty());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Subject",
        args = {boolean.class, Set.class, Set.class, Set.class}
    )
    public void test_Constructor_02() {
        Set <Principal> principal = new HashSet<Principal>();
        Set <Object> pubCredentials = new HashSet<Object>();
        Set <Object> privCredentials = new HashSet<Object>();
        Principal pr1 = new PrincipalImpl("TestPrincipal1");
        Principal pr2 = new PrincipalImpl("TestPrincipal2");
        principal.add(pr1);
        principal.add(pr2);
        Object pubCredential1 = new Object();
        Object pubCredential2 = new Object();
        pubCredentials.add(pubCredential1);
        pubCredentials.add(pubCredential2);
        Object privCredential1 = new Object();
        Object privCredential2 = new Object();
        privCredentials.add(privCredential1);
        privCredentials.add(privCredential2);
        try {
            Subject s = new Subject(true, principal, pubCredentials, privCredentials);
            assertNotNull("Null object returned", s);
            assertTrue("Not read-only object", s.isReadOnly());
            assertFalse("Set of principal is empty", s.getPrincipals().isEmpty());
            assertFalse("Set of private credentials is empty", s.getPrivateCredentials().isEmpty());
            assertFalse("Set of public credentials is empty", s.getPublicCredentials().isEmpty());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        try {
            Subject s = new Subject(false, principal, pubCredentials, privCredentials);
            assertNotNull("Null object returned", s);
            assertFalse("Read-only object", s.isReadOnly());
            assertFalse("Set of principal is empty", s.getPrincipals().isEmpty());
            assertFalse("Set of private credentials is empty", s.getPrivateCredentials().isEmpty());
            assertFalse("Set of public credentials is empty", s.getPublicCredentials().isEmpty());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        try {
            Subject s = new Subject(true, null, pubCredentials, privCredentials);
            fail("NullPointerException wasn't thrown");
        } catch (NullPointerException npe) {
        }
        try {
            Subject s = new Subject(true, principal, null, privCredentials);
            fail("NullPointerException wasn't thrown");
        } catch (NullPointerException npe) {
        }
        try {
            Subject s = new Subject(true, principal, pubCredentials, null);
            fail("NullPointerException wasn't thrown");
        } catch (NullPointerException npe) {
        }
        try {
            Subject s = new Subject(true, null, null, null);
            fail("NullPointerException wasn't thrown");
        } catch (NullPointerException npe) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "doAs",
        args = {Subject.class, PrivilegedAction.class}
    )
    public void test_doAs_01() {
        Subject subj = new Subject();
        PrivilegedAction<Object> pa = new myPrivilegedAction();
        PrivilegedAction<Object> paNull = null;
        try {
            Object obj = Subject.doAs(null, pa);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        try {
            Object obj = Subject.doAs(subj, pa);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        try {
            Object obj = Subject.doAs(subj, paNull);
            fail("NullPointerException wasn't thrown");
        } catch (NullPointerException npe) {
        }
        class TestSecurityManager extends SecurityManager {
            @Override
            public void checkPermission(Permission permission) {
                if (permission instanceof AuthPermission
                        && "doAs".equals(permission.getName())) {
                    throw new SecurityException();
                }
                super.checkPermission(permission);
            }
        }
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        try {
            Object obj = Subject.doAs(subj, pa);
            fail("SecurityException wasn't thrown");
        } catch (SecurityException se) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "doAs",
        args = {Subject.class, PrivilegedExceptionAction.class}
    )
    public void test_doAs_02() {
        Subject subj = new Subject();
        PrivilegedExceptionAction<Object> pea = new myPrivilegedExceptionAction();
        PrivilegedExceptionAction<Object> peaNull = null;
        try {
            Object obj = Subject.doAs(null, pea);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        try {
            Object obj = Subject.doAs(subj, pea);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        try {
            Object obj = Subject.doAs(subj, peaNull);
            fail("NullPointerException wasn't thrown");
        } catch (NullPointerException npe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of NullPointerException");
        }
        try {
            Subject.doAs(subj, new PrivilegedExceptionAction<Object>(){
                public Object run() throws PrivilegedActionException {
                    throw new PrivilegedActionException(null);
                }
            });
            fail("PrivilegedActionException wasn't thrown");
        } catch (PrivilegedActionException e) {
        }
        class TestSecurityManager extends SecurityManager {
            @Override
            public void checkPermission(Permission permission) {
                if (permission instanceof AuthPermission
                        && "doAs".equals(permission.getName())) {
                    throw new SecurityException();
                }
                super.checkPermission(permission);
            }
        }
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        try {
            Object obj = Subject.doAs(subj, pea);
            fail("SecurityException wasn't thrown");
        } catch (SecurityException se) {
        } catch (Exception e) {
            fail(e + " was thrown instead of SecurityException");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "doAsPrivileged",
        args = {Subject.class, PrivilegedAction.class, AccessControlContext.class}
    )
    public void test_doAsPrivileged_01() {
        Subject subj = new Subject();
        PrivilegedAction<Object> pa = new myPrivilegedAction();
        PrivilegedAction<Object> paNull = null;
        AccessControlContext acc = AccessController.getContext();
        try {
            Object obj = Subject.doAsPrivileged(null, pa, acc);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        try {
            Object obj = Subject.doAsPrivileged(subj, pa, acc);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        try {
            Object obj = Subject.doAsPrivileged(subj, paNull, acc);
            fail("NullPointerException wasn't thrown");
        } catch (NullPointerException npe) {
        }
        class TestSecurityManager extends SecurityManager {
            @Override
            public void checkPermission(Permission permission) {
                if (permission instanceof AuthPermission
                        && "doAsPrivileged".equals(permission.getName())) {
                    throw new SecurityException();
                }
                super.checkPermission(permission);
            }
        }
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        try {
            Object obj = Subject.doAsPrivileged(subj, pa, acc);
            fail("SecurityException wasn't thrown");
        } catch (SecurityException se) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "doAsPrivileged",
        args = {Subject.class, PrivilegedExceptionAction.class, AccessControlContext.class}
    )
    public void test_doAsPrivileged_02() {
        Subject subj = new Subject();
        PrivilegedExceptionAction<Object> pea = new myPrivilegedExceptionAction();
        PrivilegedExceptionAction<Object> peaNull = null;
        AccessControlContext acc = AccessController.getContext();
        try {
            Object obj = Subject.doAsPrivileged(null, pea, acc);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        try {
            Object obj = Subject.doAsPrivileged(subj, pea, acc);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        try {
            Object obj = Subject.doAsPrivileged(subj, peaNull, acc);
            fail("NullPointerException wasn't thrown");
        } catch (NullPointerException npe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of NullPointerException");
        }
        try {
            Subject.doAsPrivileged(subj, new PrivilegedExceptionAction<Object>(){
                public Object run() throws PrivilegedActionException {
                    throw new PrivilegedActionException(null);
                }
            }, acc);
            fail("PrivilegedActionException wasn't thrown");
        } catch (PrivilegedActionException e) {
        }
        class TestSecurityManager extends SecurityManager {
            @Override
            public void checkPermission(Permission permission) {
                if (permission instanceof AuthPermission
                        && "doAsPrivileged".equals(permission.getName())) {
                    throw new SecurityException();
                }
                super.checkPermission(permission);
            }
        }
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        try {
            Object obj = Subject.doAsPrivileged(subj, pea, acc);
            fail("SecurityException wasn't thrown");
        } catch (SecurityException se) {
        } catch (Exception e) {
            fail(e + " was thrown instead of SecurityException");
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "SecurityException wasn't tested",
        method = "equals",
        args = {Object.class}
    )
    public void test_equals() {
        Set <Principal> principal = new HashSet<Principal>();
        Set <Principal> principal1 = new HashSet<Principal>();
        Set <Object> pubCredentials = new HashSet<Object>();
        Set <Object> privCredentials = new HashSet<Object>();
        Principal pr1 = new PrincipalImpl("TestPrincipal1");
        Principal pr2 = new PrincipalImpl("TestPrincipal2");
        principal.add(pr1);
        principal.add(pr2);
        principal1.add(pr1);
        Object pubCredential1 = new Object();
        Object pubCredential2 = new Object();
        pubCredentials.add(pubCredential1);
        pubCredentials.add(pubCredential2);
        Object privCredential1 = new Object();
        Object privCredential2 = new Object();
        privCredentials.add(privCredential1);
        privCredentials.add(privCredential2);
        Subject s1 = new Subject(true, principal, pubCredentials, privCredentials);
        Subject s2 = new Subject(true, principal1, pubCredentials, privCredentials);
        Subject s3 = new Subject(true, principal, pubCredentials, privCredentials);
        try {
            assertTrue(s1.equals(s1));
            assertFalse(s1.equals(s2));
            assertTrue(s1.equals(s3));
            assertFalse(s1.equals(new Object()));
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        class TestSecurityManager extends SecurityManager {
            @Override
            public void checkPermission(Permission permission) {
                if (permission instanceof PrivateCredentialPermission
                        && "equals".equals(permission.getName())) {
                    throw new SecurityException();
                }
                super.checkPermission(permission);
            }
        }
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        try {
            s1.equals(s1);
        } catch (SecurityException se) {
        } 
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getPrincipals",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getPrivateCredentials",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getPublicCredentials",
            args = {}
        )
    })
    public void test_getPrincipals() {
        Set <Principal> principal = new HashSet<Principal>();
        Set <Object> pubCredentials = new HashSet<Object>();
        Set <Object> privCredentials = new HashSet<Object>();
        Principal pr1 = new PrincipalImpl("TestPrincipal1");
        Principal pr2 = new PrincipalImpl("TestPrincipal2");
        principal.add(pr1);
        principal.add(pr2);
        Object pubCredential1 = new Object();
        pubCredentials.add(pubCredential1);
        Object privCredential1 = new Object();
        Object privCredential2 = new Object();
        privCredentials.add(privCredential1);
        privCredentials.add(privCredential2);
        Subject s = new Subject(false, principal, pubCredentials, privCredentials);
        try {
            Set<Principal> pr = s.getPrincipals();
            assertNotNull(pr);
            assertEquals(principal.size(), pr.size());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        try {
            Set<Object> privC = s.getPrivateCredentials();
            assertNotNull(privC);
            assertEquals(privCredentials.size(), privC.size());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        try {
            Set<Object> pubC = s.getPublicCredentials();
            assertNotNull(pubC);
            assertEquals(pubCredentials.size(), pubC.size());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "isReadOnly",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setReadOnly",
            args = {}
        )
    })
    public void test_ReadOnly() {
        Set <Principal> principal = new HashSet<Principal>();
        Set <Object> pubCredentials = new HashSet<Object>();
        Set <Object> privCredentials = new HashSet<Object>();
        Principal pr1 = new PrincipalImpl("TestPrincipal1");
        Principal pr2 = new PrincipalImpl("TestPrincipal2");
        principal.add(pr1);
        principal.add(pr2);
        Object pubCredential1 = new Object();
        pubCredentials.add(pubCredential1);
        Object privCredential1 = new Object();
        Object privCredential2 = new Object();
        privCredentials.add(privCredential1);
        privCredentials.add(privCredential2);
        Subject s = new Subject(false, principal, pubCredentials, privCredentials);
        try {
            assertFalse(s.isReadOnly());
            s.setReadOnly();
            assertTrue(s.isReadOnly());
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
        class TestSecurityManager extends SecurityManager {
            @Override
            public void checkPermission(Permission permission) {
                if (permission instanceof AuthPermission
                        && "setReadOnly".equals(permission.getName())) {
                    throw new SecurityException();
                }
                super.checkPermission(permission);
            }
        }
        TestSecurityManager ss = new TestSecurityManager();
        System.setSecurityManager(ss);
        try {
            s.setReadOnly();
            fail("SecurityException wasn't thrown");
        } catch (SecurityException se) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSubject",
        args = {AccessControlContext.class}
    )
    public void test_getSubject() {
        Subject subj = new Subject();
        AccessControlContext acc = new AccessControlContext(new ProtectionDomain[0]);
        try {
            assertNull(Subject.getSubject(acc));
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
        class TestSecurityManager extends SecurityManager {
            @Override
            public void checkPermission(Permission permission) {
                if (permission instanceof AuthPermission
                        && "getSubject".equals(permission.getName())) {
                    throw new SecurityException();
                }
                super.checkPermission(permission);
            }
        }
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        try {
            Subject.getSubject(acc);
            fail("SecurityException wasn't thrown");
        } catch (SecurityException se) {
        } 
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() {
        Subject subj = new Subject();
        try {
            assertNotNull("Null returned", subj.toString());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "SecurityException wasn't tested",
        method = "hashCode",
        args = {}
    )
    public void test_hashCode() {
        Subject subj = new Subject();
        try {
            assertNotNull("Null returned", subj.hashCode());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        class TestSecurityManager extends SecurityManager {
            @Override
            public void checkPermission(Permission permission) {
                if (permission instanceof AuthPermission
                        && "hashCode".equals(permission.getName())) {
                    throw new SecurityException();
                }
                super.checkPermission(permission);
            }
        }
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        try {
            subj.hashCode();
        } catch (SecurityException se) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getPrincipals",
            args = {Class.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "",
            method = "getPrivateCredentials",
            args = {Class.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "",
            method = "getPublicCredentials",
            args = {Class.class}
        )
    })
    public void test_getPrincipals_Class() {
        Set <Principal> principal = new HashSet<Principal>();
        Set <Object> pubCredentials = new HashSet<Object>();
        Set <Object> privCredentials = new HashSet<Object>();
        Principal pr1 = new PrincipalImpl("TestPrincipal1");
        Principal pr2 = new PrincipalImpl("TestPrincipal2");
        principal.add(pr1);
        principal.add(pr2);
        Object pubCredential1 = new Object();
        pubCredentials.add(pubCredential1);
        Object privCredential1 = new Object();
        Object privCredential2 = new Object();
        privCredentials.add(privCredential1);
        privCredentials.add(privCredential2);
        Subject s = new Subject(true, principal, pubCredentials, privCredentials);
        try {
            Set<Principal> pr = s.getPrincipals(null);
            fail("NullPointerException wasn't thrown");
        } catch (NullPointerException npe) {
        }
        try {
            Set<Object> privC = s.getPrivateCredentials(null);
            fail("NullPointerException wasn't thrown");
        } catch (NullPointerException npe) {
        }
        try {
            Set<Object> pubC = s.getPublicCredentials(null);
            fail("NullPointerException wasn't thrown");
        } catch (NullPointerException npe) {
        }
        try {
            Set<Principal> pr = s.getPrincipals(Principal.class);
            assertNotNull(pr);
            assertEquals(principal.size(), pr.size());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        try {
            Set<Object> privC = s.getPrivateCredentials(Object.class);
            assertNotNull(privC);
            assertEquals(privCredentials.size(), privC.size());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        try {
            Set<Object> pubC = s.getPublicCredentials(Object.class);
            assertNotNull(pubC);
            assertEquals(pubCredentials.size(), pubC.size());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
}
class myPrivilegedAction implements PrivilegedAction <Object> {
    myPrivilegedAction(){}
    public Object run() {
        return new Object();
    }
}
class myPrivilegedExceptionAction implements PrivilegedExceptionAction <Object> {
    myPrivilegedExceptionAction(){}
    public Object run() {
        return new Object();
    }
}
