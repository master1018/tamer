@TestTargetClass(PermissionCollection.class)
public class PermissionCollectionTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "",
        method = "implies",
        args = {java.security.Permission.class}
    )
    @KnownFailure("Android doesn't support protection domains.")
    public void test_impliesLjava_security_Permission() throws Exception{
        ProtectionDomain protectionDomain = getClass().getProtectionDomain();
        assertNotNull("ProtectionDomain is null", protectionDomain);
        CodeSource codeSource = protectionDomain.getCodeSource();
        assertNotNull("CodeSource is null", codeSource);
        URL classURL = codeSource.getLocation();
        assertNotNull("Could not get this class' location", classURL);
        File policyFile = Support_GetLocal.createTempFile(".policy");
        policyFile.deleteOnExit();
        URL signedBKS = getResourceURL("PermissionCollection/signedBKS.jar");
        URL keystoreBKS = getResourceURL("PermissionCollection/keystore.bks");
        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(policyFile);
            String linebreak = System.getProperty("line.separator");
            StringBuilder towrite = new StringBuilder();
            towrite.append("grant {");
            towrite.append(linebreak);
            towrite.append("permission java.io.FilePermission \"");
            towrite.append(signedBKS.getFile());
            towrite.append("\", \"read\";");
            towrite.append(linebreak);
            towrite.append("permission java.lang.RuntimePermission \"getProtectionDomain\";");
            towrite.append(linebreak);
            towrite.append("permission java.security.SecurityPermission \"getPolicy\";");
            towrite.append(linebreak);
            towrite.append("};");
            towrite.append(linebreak);
            towrite.append("grant codeBase \"");
            towrite.append(signedBKS.toExternalForm());
            towrite.append("\" signedBy \"eleanor\" {");
            towrite.append(linebreak);
            towrite.append("permission java.io.FilePermission \"test1.txt\", \"write\";");
            towrite.append(linebreak);
            towrite.append("permission mypackage.MyPermission \"essai\", signedBy \"eleanor,dylan\";");
            towrite.append(linebreak);
            towrite.append("};");
            towrite.append(linebreak);
            towrite.append("grant codeBase \"");
            towrite.append(signedBKS.toExternalForm());
            towrite.append("\" signedBy \"eleanor\" {");
            towrite.append(linebreak);
            towrite.append("permission java.io.FilePermission \"test2.txt\", \"write\";");
            towrite.append(linebreak);
            towrite.append("};");
            towrite.append(linebreak);
            towrite.append("grant codeBase \"");
            towrite.append(classURL.toExternalForm());
            towrite.append("\" {");
            towrite.append(linebreak);
            towrite.append("permission java.security.AllPermission;");
            towrite.append(linebreak);
            towrite.append("};");
            towrite.append(linebreak);
            towrite.append("keystore \"");
            towrite.append(keystoreBKS.toExternalForm());
            towrite.append("\",\"BKS\";");            
            fileOut.write(towrite.toString().getBytes());
            fileOut.flush();
        } finally {
            if (fileOut != null) {
                fileOut.close();
            }
        }
        File jarFile = null;
        FileOutputStream fout = null;
        InputStream jis = null;
        try {
            jis = Support_Resources
                    .getResourceStream("PermissionCollection/mypermissionBKS.jar");
            jarFile = Support_GetLocal.createTempFile(".jar");
            jarFile.deleteOnExit();
            fout = new FileOutputStream(jarFile);
            int c = jis.read();
            while (c != -1) {
                fout.write(c);
                c = jis.read();
            }
            fout.flush();
        } finally {
            if (fout != null) {
                fout.close();
            }
            if (jis != null) {
                jis.close();
            }
        }
        ProcessBuilder builder = javaProcessBuilder();
        builder.command().add("-cp");
        builder.command().add(Support_Exec.createPath(
                new File(classURL.getFile()).getPath(), jarFile.getPath()));
        builder.command().add("-Djava.security.policy=" + policyFile.toURL());
        builder.command().add("tests.support.Support_PermissionCollection");
        builder.command().add(signedBKS.toExternalForm());
        String result = execAndGetOutput(builder);
        StringTokenizer resultTokenizer = new StringTokenizer(result, ",");
        assertEquals("Permission should be granted", "false", resultTokenizer
                .nextToken());
        assertEquals("signed Permission should be granted", "false",
                resultTokenizer.nextToken());
        assertEquals("Permission should not be granted", "false",
                resultTokenizer.nextToken());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "PermissionCollection",
        args = {}
    )
    public void test_Constructor() {
        SecurityPermission permi = new SecurityPermission(
                "testing permissionCollection-isReadOnly");
        PermissionCollection permCollect = permi.newPermissionCollection();
        assertNotNull("creat permissionCollection constructor returned a null",
                permCollect);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isReadOnly",
        args = {}
    )
    public void test_isReadOnly() {
        SecurityPermission permi = new SecurityPermission(
                "testing permissionCollection-isREadOnly");
        PermissionCollection permCollect = permi.newPermissionCollection();
        assertTrue("readOnly has not been set, but isReadOnly returned true",
                !permCollect.isReadOnly());
        permCollect.setReadOnly();
        assertTrue("readOnly is set, but isReadonly returned false",
                permCollect.isReadOnly());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setReadOnly",
        args = {}
    )
    public void test_setReadOnly() {
        SecurityPermission permi = new SecurityPermission(
                "testing permissionCollection-setReadOnly");
        PermissionCollection permCollect = permi.newPermissionCollection();
        assertTrue("readOnly has not been set, but isReadOnly returned true",
                !permCollect.isReadOnly());
        permCollect.setReadOnly();
        assertTrue("readOnly is set, but isReadonly returned false",
                permCollect.isReadOnly());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() {
        SecurityPermission permi = new SecurityPermission(
                "testing permissionCollection-isREadOnly");
        assertNotNull("toString should have returned a string of elements",
                permi.newPermissionCollection().toString());
        assertTrue(permi.newPermissionCollection().toString().endsWith("\n"));
    }
    public static URL getResourceURL(String name) {
        URL url = ClassLoader.getSystemClassLoader().getResource(name);
        if (url == null) {
            throw new RuntimeException("Failed to get resource url: " + name);
        }
        return url;
    }
}