@TestTargetClass(Permissions.class)
public class Permissions2Test extends junit.framework.TestCase {
    FilePermission readAllFiles = new FilePermission("<<ALL FILES>>", "read");
    FilePermission alsoReadAllFiles = new FilePermission("<<ALL FILES>>",
            "read");
    FilePermission allInCurrent = new FilePermission("*",
            "read, write, execute,delete");
    FilePermission readInCurrent = new FilePermission("*", "read");
    FilePermission readInFile = new FilePermission("aFile.file", "read");
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Permissions",
        args = {}
    )
    public void test_Constructor() {
        new Permissions();
	}
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "add",
        args = {java.security.Permission.class}
    )
    public void test_addLjava_security_Permission() {
        char s = File.separatorChar;
        FilePermission perm[] = new FilePermission[7];
        perm[0] = readAllFiles;
        perm[1] = allInCurrent;
        perm[2] = new FilePermission(s + "tmp" + s + "test" + s + "*",
                "read,write");
        perm[3] = new FilePermission(s + "tmp" + s + "test" + s
                + "collection.file", "read");
        perm[4] = alsoReadAllFiles;
        perm[5] = readInFile;
        perm[6] = new FilePermission("hello.file", "write");
        Permissions perms = new Permissions();
        for (int i = 0; i < perm.length; i++) {
            perms.add(perm[i]);
        }
        Enumeration e = perms.elements();
        FilePermission perm2[] = new FilePermission[10];
        int i = 0;
        while (e.hasMoreElements()) {
            perm2[i] = (FilePermission) e.nextElement();
            i++;
        }
        assertEquals("Permissions.elements did not return the correct number "
                + "of permission - called in add() test ", i, perm.length);
        perms.setReadOnly();
        try {
            perms.add(new AllPermission());
            fail("expected SecurityException");
        } catch (SecurityException ex) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "elements",
        args = {}
    )
    public void test_elements() {
        char s = File.separatorChar;
        FilePermission perm[] = new FilePermission[7];
        perm[0] = readAllFiles;
        perm[1] = allInCurrent;
        perm[2] = new FilePermission(s + "tmp" + s + "test" + s + "*",
                "read,write");
        perm[3] = new FilePermission(s + "tmp" + s + "test" + s
                + "collection.file", "read");
        perm[4] = alsoReadAllFiles;
        perm[5] = readInFile;
        perm[6] = new FilePermission("hello.file", "write");
        Permissions perms = new Permissions();
        for (int i = 0; i < perm.length; i++) {
            perms.add(perm[i]);
        }
        Enumeration e = perms.elements();
        FilePermission perm2[] = new FilePermission[10];
        int i = 0;
        while (e.hasMoreElements()) {
            perm2[i] = (FilePermission) e.nextElement();
            i++;
        }
        assertEquals("Permissions.elements did not return the correct "
                + "number of permission - called in element() test", i,
                perm.length);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "implies",
        args = {java.security.Permission.class}
    )
    public void test_impliesLjava_security_Permission() {
        char s = File.separatorChar;
        FilePermission perm[] = new FilePermission[7];
        perm[0] = new FilePermission("test1.file", "write");
        perm[1] = allInCurrent;
        perm[2] = new FilePermission(s + "tmp" + s + "test" + s + "*",
                "read,write");
        perm[3] = new FilePermission(s + "tmp" + s + "test" + s
                + "collection.file", "read");
        perm[4] = new FilePermission(s + "windows" + "*", "delete");
        perm[5] = readInFile;
        perm[6] = new FilePermission("hello.file", "write");
        Permissions perms = new Permissions();
        for (int i = 0; i < perm.length; i++) {
            perms.add(perm[i]);
        }
        assertTrue("Returned true for non-subset of files", !perms
                .implies(new FilePermission("<<ALL FILES>>", "execute")));
        assertTrue("Returned true for non-subset of action", !perms
                .implies(new FilePermission(s + "tmp" + s + "test" + s + "*",
                        "execute")));
        assertTrue("Returned false for subset of actions", perms
                .implies(new FilePermission("*", "write")));
        assertTrue("Returned false for subset of files", perms
                .implies(new FilePermission(s + "tmp" + s + "test" + s
                        + "test.file", "read")));
        assertTrue("Returned false for subset of files and actions", perms
                .implies(new FilePermission(s + "tmp" + s + "test" + s
                        + "test2.file", "write")));
    }
    class TestSecurityManager extends SecurityManager {
        @Override
        public void checkPermission(Permission permission) {
        }
    }
}
