@TestTargetClass(SubjectDomainCombiner.class) 
public class SubjectDomainCombinerTest extends TestCase {
    private final static boolean DEBUG = true;
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
        method = "SubjectDomainCombiner",
        args = {Subject.class}
    )
    public void test_Constructor_01() {
        Subject s = new Subject();
        SubjectDomainCombiner c = new SubjectDomainCombiner(s);
        try {
            assertEquals(s, c.getSubject());
        } catch(SecurityException se) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies that Subject associated with this SubjectDomainCombiner is returned",
        method = "getSubject",
        args = {}
    )
    public void test_getSubject_01() {
        class TestSecurityManager extends SecurityManager {
            @Override
            public void checkPermission(Permission permission) {
                if (permission instanceof AuthPermission
                        && "getSubjectFromDomainCombiner".equals(permission.getName())) {
                    return;
                }
                super.checkPermission(permission);
            }
        }
        TestSecurityManager sm = new TestSecurityManager();
        System.setSecurityManager(sm);
        Subject s = new Subject();
        SubjectDomainCombiner c = new SubjectDomainCombiner(s);
        assertEquals(s, c.getSubject());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "SecurityException to be thrown if caller doesn't have permissions to get the Subject",
        method = "getSubject",
        args = {}
    )
    public void test_getSubject_02() {
        class TestSecurityManager extends SecurityManager {
            @Override
            public void checkPermission(Permission permission) {
                if (permission instanceof AuthPermission
                        && "getSubjectFromDomainCombiner".equals(permission.getName())) {
                    throw new SecurityException();
                }
                super.checkPermission(permission);
            }
        }
        TestSecurityManager sm = new TestSecurityManager();
        System.setSecurityManager(sm);
        Subject s = new Subject();
        SubjectDomainCombiner c = new SubjectDomainCombiner(s);
        try {
            c.getSubject();
            fail("SecurityException expected");
        } catch(SecurityException se) {
        }
    }
    protected final static String locationUrl = "http:
    protected final static String[] currentDomainX500names = { "CN=cd_name,OU=abc,O=corp,C=CH" };
    protected final static String[] currentDomainPerms = { "getStackTrace",
                                                           "setIO"
                                                         };
    protected final static String[] assignedDomainX500names = { "CN=ad_name,OU=def,O=corp,C=US" };
    protected final static String[] assignedDomainPerms = { "accessDeclaredMembers"
                                                          };
    protected final static String[] SubjectX500names = { "CN=s_user,OU=abc,O=corp,C=US",
                                                           "CN=s_user,OU=abc,O=corp,C=RU" };
    protected final static String subjectPubPerm1 = "readFileDescriptor";
    protected final static String subjectPvtPerm1 = "writeFileDescriptor";
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "both currentDomains and assignedDomains are not null",
        method = "combine",
        args = {ProtectionDomain[].class, ProtectionDomain[].class}
    )
    public void test_combine_01() {
        URL url;
        try {
            url = new URL(locationUrl);
        } catch (MalformedURLException mue) {
            throw new Error(mue);
        }
        CodeSource cs = new CodeSource(url, (java.security.cert.Certificate[])null);
        class MyClassLoader extends ClassLoader {
            public MyClassLoader() {
                super();
            }
        }
        ClassLoader current_pd_cl = new MyClassLoader() ;
        ClassLoader assigned_pd_cl = new MyClassLoader() ;
        ProtectionDomain[] current_pd = createProtectionDomains(cs, current_pd_cl, currentDomainX500names, currentDomainPerms);
        ProtectionDomain[] assigned_pd = createProtectionDomains(cs, assigned_pd_cl, assignedDomainX500names, assignedDomainPerms);
        Subject s = createSubject();        
        SubjectDomainCombiner c = new SubjectDomainCombiner(s);
        ProtectionDomain[] r_pd = c.combine(current_pd, assigned_pd);
        if(DEBUG) {
            System.out.println("=========== c_pd");
            dumpPD(current_pd);
            System.out.println("=========== a_pd");
            dumpPD(assigned_pd);
            System.out.println("=========== r_pd");
            dumpPD(r_pd);
            System.out.println("===========");
        }
        for(int i = 0; i < r_pd.length; i++) {
            ProtectionDomain pd = r_pd[i];
            assertTrue("code source mismatch", pd.getCodeSource().equals(cs));
            boolean cpd = false;
            if(pd.getClassLoader().equals(current_pd_cl)) {
                cpd = true;
            } else if(pd.getClassLoader().equals(assigned_pd_cl)) {
                cpd = false;
            } else {
                fail("class loader mismatch");
            }
            Principal[] principals = pd.getPrincipals();
            String[] names;
            if(cpd == true)    names = SubjectX500names; 
            else               names = assignedDomainX500names;
            for(int j = 0; j < principals.length; j++) {
                if(contains(names, principals[j].getName()) == false)
                    fail("principal mismatch ("  + j +") " + principals[j].getName());
            }
            PermissionCollection perms = pd.getPermissions();
            Enumeration<Permission> p = perms.elements();
            while(p.hasMoreElements()) {
                Permission pp = p.nextElement();
                String pn = pp.getName();
                if(cpd == true) {
                    if(contains(currentDomainPerms, pn) == false)
                        fail("current domains permissions mismatch " + pn);
                } else {
                    if(contains(assignedDomainPerms, pn) == false)
                        fail("assigned domains permissions mismatch " + pn);
                }
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "assignedDomains is null",
        method = "combine",
        args = {ProtectionDomain[].class, ProtectionDomain[].class}
    )
    public void test_combine_02() {
        URL url;
        try {
            url = new URL(locationUrl);
        } catch (MalformedURLException mue) {
            throw new Error(mue);
        }
        CodeSource cs = new CodeSource(url, (java.security.cert.Certificate[])null);
        class MyClassLoader extends ClassLoader {
            public MyClassLoader() {
                super();
            }
        }
        ClassLoader current_pd_cl = new MyClassLoader() ;
        ClassLoader assigned_pd_cl = new MyClassLoader() ;
        ProtectionDomain[] current_pd = createProtectionDomains(cs, current_pd_cl, currentDomainX500names, currentDomainPerms);
        ProtectionDomain[] assigned_pd = null;
        Subject s = createSubject();
        SubjectDomainCombiner c = new SubjectDomainCombiner(s);
        ProtectionDomain[] r_pd = c.combine(current_pd, assigned_pd);
        if(DEBUG) {
            System.out.println("=========== c_pd");
            dumpPD(current_pd);
            System.out.println("=========== a_pd");
            dumpPD(assigned_pd);
            System.out.println("=========== r_pd");
            dumpPD(r_pd);
            System.out.println("===========");
        }
        for(int i = 0; i < r_pd.length; i++) {
            ProtectionDomain pd = r_pd[i];
            assertTrue("code source mismatch", pd.getCodeSource().equals(cs));
            assertTrue("class loader mismatch", pd.getClassLoader().equals(current_pd_cl));
            Principal[] principals = pd.getPrincipals();
            for(int j = 0; j < principals.length; j++) {
                if(contains(SubjectX500names, principals[j].getName()) == false)
                    fail("principal mismatch ("  + j +") " + principals[j].getName());
            }
            PermissionCollection perms = pd.getPermissions();
            Enumeration<Permission> p = perms.elements();
            while(p.hasMoreElements()) {
                Permission pp = p.nextElement();
                String pn = pp.getName();
                   if(contains(currentDomainPerms, pn) == false)
                       fail("current domains permissions mismatch " + pn);
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "currentDomains is null",
        method = "combine",
        args = {ProtectionDomain[].class, ProtectionDomain[].class}
    )
    public void test_combine_03() {
        URL url;
        try {
            url = new URL(locationUrl);
        } catch (MalformedURLException mue) {
            throw new Error(mue);
        }
        CodeSource cs = new CodeSource(url, (java.security.cert.Certificate[])null);
        class MyClassLoader extends ClassLoader {
            public MyClassLoader() {
                super();
            }
        }
        ClassLoader current_pd_cl = new MyClassLoader() ;
        ClassLoader assigned_pd_cl = new MyClassLoader() ;
        ProtectionDomain[] current_pd = null;
        ProtectionDomain[] assigned_pd = createProtectionDomains(cs, assigned_pd_cl, assignedDomainX500names, assignedDomainPerms);
        Subject s = createSubject();        
        SubjectDomainCombiner c = new SubjectDomainCombiner(s);
        ProtectionDomain[] r_pd = c.combine(current_pd, assigned_pd);
        if(DEBUG) {
            System.out.println("=========== c_pd");
            dumpPD(current_pd);
            System.out.println("=========== a_pd");
            dumpPD(assigned_pd);
            System.out.println("=========== r_pd");
            dumpPD(r_pd);
            System.out.println("===========");
        }
        for(int i = 0; i < r_pd.length; i++) {
            ProtectionDomain pd = r_pd[i];
            assertTrue("code source mismatch", pd.getCodeSource().equals(cs));
            assertTrue("class loader mismatch", pd.getClassLoader().equals(assigned_pd_cl));
            Principal[] principals = pd.getPrincipals();
            for(int j = 0; j < principals.length; j++) {
                if(contains(assignedDomainX500names, principals[j].getName()) == false)
                    fail("principal mismatch ("  + j +") " + principals[j].getName());
            }
            PermissionCollection perms = pd.getPermissions();
            Enumeration<Permission> p = perms.elements();
            while(p.hasMoreElements()) {
                Permission pp = p.nextElement();
                String pn = pp.getName();
                  if(contains(assignedDomainPerms, pn) == false)
                       fail("assigned domains permissions mismatch " + pn);
            }
        }
    }
    protected ProtectionDomain[] createProtectionDomains(CodeSource cs, ClassLoader cl, String[] names, String[] perms) {
        ProtectionDomain[] pd = new ProtectionDomain[perms.length];
        Principal[] principals = new Principal[names.length];
        for(int i = 0; i < names.length; i++) {
            principals[i] = new X500Principal(names[i]);
        }        
        for(int i = 0; i < perms.length; i++) {
            RuntimePermission rp = new RuntimePermission(perms[i]);
            PermissionCollection pc = rp.newPermissionCollection();
            pc.add(rp);
            pd[i] = new ProtectionDomain(cs, pc, cl, principals);
        }
        return pd;
    }
    protected Subject createSubject() {
        HashSet<Principal> principal_set = new HashSet<Principal>();
        for(int i = 0; i < SubjectX500names.length; i++)
            principal_set.add(new X500Principal(SubjectX500names[i]));
        HashSet<Permission> pub_perms_set = new HashSet<Permission>();
        pub_perms_set.add(new RuntimePermission(subjectPubPerm1));
        HashSet<Permission> pvt_perms_set = new HashSet<Permission>();
        pvt_perms_set.add(new RuntimePermission(subjectPvtPerm1));
        Subject s = new Subject(false, principal_set, pub_perms_set, pvt_perms_set);
        return s;
    }
    boolean contains(String[] arr, String val) {
        for(int i = 0; i < arr.length; i++)
            if(arr[i].compareTo(val) == 0)
                return true;
        return false;
    }
    private void dumpPD(ProtectionDomain[] arr) {
        if(DEBUG) {
            if(arr == null) return;
            for(int i = 0; i < arr.length; i++) {
                System.out.println(arr[i].getCodeSource().getLocation().toString());
                dumpPerms(arr[i].getPermissions());
                dumpPrincipals(arr[i].getPrincipals());
            }
        }
    }
    private void dumpPerms(PermissionCollection perms) {
        if(DEBUG) {
            Enumeration<Permission> p = perms.elements();
            while(p.hasMoreElements()) {
                Permission pp = p.nextElement();
                System.out.println("   " + pp.getName() + "    " + pp.getActions());
            }
        }
    }
    private void dumpPrincipals(Principal[] p) {
        if(DEBUG) {
            if(p == null) return;
            for(int i = 0; i < p.length; i++) {
                System.out.println("   " + p[i].getName());
            }
        }
    }
}
