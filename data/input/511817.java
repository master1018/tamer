@TestTargetClass(FilePermission.class) 
public class FilePermissionTest extends junit.framework.TestCase {
    FilePermission readAllFiles;
    FilePermission alsoReadAllFiles;
    FilePermission allInCurrent;
    FilePermission readInCurrent;
    FilePermission readInFile;
    @Override protected void setUp() throws Exception {
        super.setUp();
        TestEnvironment.reset();
        readAllFiles = new FilePermission("<<ALL FILES>>", "read");
        alsoReadAllFiles = new FilePermission("<<ALL FILES>>", "read");
        allInCurrent = new FilePermission("*", "read, write, execute,delete");
        readInCurrent = new FilePermission("*", "read");
        readInFile = new FilePermission("aFile.file", "read");
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies FilePermission(java.lang.String, java.lang.String) constructor.",
        method = "FilePermission",
        args = {java.lang.String.class, java.lang.String.class}
    )     
    public void test_ConstructorLjava_lang_StringLjava_lang_String() {
        assertTrue("Used to test", true);
        FilePermission constructFile = new FilePermission("test constructor",
                "write");
        assertEquals("action given to the constructor did not correspond - constructor failed",
                "write", constructFile.getActions());
        assertEquals(
                "name given to the constructor did not correspond - constructor failed",
                "test constructor", constructFile.getName());
        try {
            new FilePermission(null, "drink");
            fail("Expected IAE");
        } catch (IllegalArgumentException e) {
        }
        try {
            new FilePermission(null, "read");
            fail("Expected NPE");
        } catch (NullPointerException e) {
        }
        try {
            new FilePermission(null, null);
            fail("Expected IAE");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies getActions() method.",
        method = "getActions",
        args = {}
    )     
    public void test_getActions() {
        assertEquals("getActions should have returned only read", "read", readAllFiles
                .getActions());
        assertEquals("getActions should have returned all actions", "read,write,execute,delete", allInCurrent
                .getActions());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies equals(java.lang.Object) method.",
        method = "equals",
        args = {java.lang.Object.class}
    )     
    public void test_equalsLjava_lang_Object() {
        assertTrue(
                "returned false when two instance of FilePermission is equal",
                readAllFiles.equals(alsoReadAllFiles));
        assertTrue(
                "returned true when two instance    of FilePermission is not equal",
                !(readInCurrent.equals(readInFile)));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies implies(java.security.Permission) method.",
        method = "implies",
        args = {java.security.Permission.class}
    )    
    public void test_impliesLjava_security_Permission() {
        assertTrue("Returned true for non-subset of actions", !readAllFiles
                .implies(allInCurrent));
        assertTrue("Returned true for non-subset of files", !allInCurrent
                .implies(readAllFiles));
        assertTrue("Returned false for subset of actions", allInCurrent
                .implies(readInCurrent));
        assertTrue("Returned false for subset of files", readAllFiles
                .implies(readInCurrent));
        assertTrue("Returned false for subset of files and actions",
                allInCurrent.implies(readInFile));
        assertTrue("Returned false for equal FilePermissions", readAllFiles
                .implies(alsoReadAllFiles));
        FilePermission fp3 = new FilePermission("/bob
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies newPermissionCollection() method.",
        method = "newPermissionCollection",
        args = {}
    )      
    public void test_newPermissionCollection() {
        char s = File.separatorChar;
        FilePermission perm[] = new FilePermission[4];
        perm[0] = readAllFiles;
        perm[1] = allInCurrent;
        perm[2] = new FilePermission(s + "tmp" + s + "test" + s + "*",
                "read,write");
        perm[3] = new FilePermission(s + "tmp" + s + "test" + s
                + "collection.file", "read");
        PermissionCollection collect = perm[0].newPermissionCollection();
        for (int i = 0; i < perm.length; i++) {
            collect.add(perm[i]);
        }
        assertTrue("returned false for subset of files", collect
                .implies(new FilePermission("*", "write")));
        assertTrue("returned false for subset of name and action", collect
                .implies(new FilePermission(s + "tmp", "read")));
        assertTrue("returned true for non subset of file and action", collect
                .implies(readInFile));
        FilePermission fp1 = new FilePermission("/tmp/-".replace('/',
                File.separatorChar), "read");
        PermissionCollection fpc = fp1.newPermissionCollection();
        fpc.add(fp1);
        fpc.add(new FilePermission("/tmp/scratch/foo
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies hashCode() method.",
        method = "hashCode",
        args = {}
    )         
    public void test_hashCode() {
        assertTrue(
                "two equal filePermission instances returned different hashCode",
                readAllFiles.hashCode() == alsoReadAllFiles.hashCode());
        assertTrue(
                "two filePermission instances with same permission name returned same hashCode",
                readInCurrent.hashCode() != allInCurrent.hashCode());
    }
}
