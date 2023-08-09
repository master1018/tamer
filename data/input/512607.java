@TestTargetClass(JarOutputStream.class)
public class JarExecTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.ADDITIONAL,
        notes = "Regression functional test. Exception checking missed.",
        method = "putNextEntry",
        args = {java.util.zip.ZipEntry.class}
    )
    @KnownFailure("Maybe not a failure, but dalvikvm -jar is not supported (, as yet).")
    public void test_1562() throws Exception {
        Manifest man = new Manifest();
        Attributes att = man.getMainAttributes();
        att.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        att.put(Attributes.Name.MAIN_CLASS, "foo.bar.execjartest.Foo");
        File outputJar = File.createTempFile("hyts_", ".jar");
        outputJar.deleteOnExit();
        JarOutputStream jout = new JarOutputStream(new FileOutputStream(
                outputJar), man);
        File resources = Support_Resources.createTempFolder();
        for (String jarClass : new String[] {"Foo", "Bar"}) {
            jout.putNextEntry(new JarEntry("foo/bar/execjartest/" + jarClass
                    + ".class"));
            jout.write(getResource(resources, "hyts_" + jarClass + ".ser"));
        }
        jout.close();
        ProcessBuilder builder = javaProcessBuilder();
        builder.command().add("-jar");
        builder.command().add(outputJar.getAbsolutePath());
        assertTrue("Error executing JAR",
                execAndGetOutput(builder).startsWith("FOOBAR"));
    }
    @TestTargetNew(
        level = TestLevel.ADDITIONAL,
        notes = "Functional test.",
        method = "JarOutputStream",
        args = {java.io.OutputStream.class, java.util.jar.Manifest.class}
    )
    @KnownFailure("Maybe not a failure, but dalvikvm -jar is not supported (, as yet).")
    public void test_jar_class_path() throws Exception {
        File fooJar = File.createTempFile("hyts_", ".jar");
        File barJar = File.createTempFile("hyts_", ".jar");
        fooJar.deleteOnExit();
        barJar.deleteOnExit();
        Manifest man = new Manifest();
        Attributes att = man.getMainAttributes();
        att.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        att.put(Attributes.Name.MAIN_CLASS, "foo.bar.execjartest.Foo");
        att.put(Attributes.Name.CLASS_PATH, barJar.getName());
        File resources = Support_Resources.createTempFolder();
        JarOutputStream joutFoo = new JarOutputStream(new FileOutputStream(
                fooJar), man);
        joutFoo.putNextEntry(new JarEntry("foo/bar/execjartest/Foo.class"));
        joutFoo.write(getResource(resources, "hyts_Foo.ser"));
        joutFoo.close();
        JarOutputStream joutBar = new JarOutputStream(new FileOutputStream(
                barJar));
        joutBar.putNextEntry(new JarEntry("foo/bar/execjartest/Bar.class"));
        joutBar.write(getResource(resources, "hyts_Bar.ser"));
        joutBar.close();
        ProcessBuilder builder = javaProcessBuilder();
        builder.command().add("-jar");
        builder.command().add(fooJar.getAbsolutePath());
        assertTrue("Error executing JAR",
                execAndGetOutput(builder).startsWith("FOOBAR"));
        att.put(Attributes.Name.CLASS_PATH, "xx yy zz " + barJar.getName());
        joutFoo = new JarOutputStream(new FileOutputStream(fooJar), man);
        joutFoo.putNextEntry(new JarEntry("foo/bar/execjartest/Foo.class"));
        joutFoo.write(getResource(resources, "hyts_Foo.ser"));
        joutFoo.close();
        assertTrue("Error executing JAR",
                execAndGetOutput(builder).startsWith( "FOOBAR"));
        att.put(Attributes.Name.CLASS_PATH, ".." + File.separator
                + barJar.getParentFile().getName() + File.separator
                + barJar.getName());
        joutFoo = new JarOutputStream(new FileOutputStream(fooJar), man);
        joutFoo.putNextEntry(new JarEntry("foo/bar/execjartest/Foo.class"));
        joutFoo.write(getResource(resources, "hyts_Foo.ser"));
        joutFoo.close();
        assertTrue("Error executing JAR",
                execAndGetOutput(builder).startsWith( "FOOBAR"));
    }
    @TestTargetNew(
        level = TestLevel.ADDITIONAL,
        notes = "Functional test.",
        method = "JarOutputStream",
        args = {java.io.OutputStream.class, java.util.jar.Manifest.class}
    )
    @KnownFailure("Maybe not a failure, but dalvikvm -jar is not supported (, as yet).")
    public void test_main_class_in_another_jar() throws Exception {
        File fooJar = File.createTempFile("hyts_", ".jar");
        File barJar = File.createTempFile("hyts_", ".jar");
        fooJar.deleteOnExit();
        barJar.deleteOnExit();
        Manifest man = new Manifest();
        Attributes att = man.getMainAttributes();
        att.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        att.put(Attributes.Name.MAIN_CLASS, "foo.bar.execjartest.Foo");
        att.put(Attributes.Name.CLASS_PATH, fooJar.getName());
        File resources = Support_Resources.createTempFolder();
        JarOutputStream joutFoo = new JarOutputStream(new FileOutputStream(
                fooJar));
        joutFoo.putNextEntry(new JarEntry("foo/bar/execjartest/Foo.class"));
        joutFoo.write(getResource(resources, "hyts_Foo.ser"));
        joutFoo.close();
        JarOutputStream joutBar = new JarOutputStream(new FileOutputStream(
                barJar), man);
        joutBar.putNextEntry(new JarEntry("foo/bar/execjartest/Bar.class"));
        joutBar.write(getResource(resources, "hyts_Bar.ser"));
        joutBar.close();
        ProcessBuilder builder = javaProcessBuilder();
        builder.command().add("-jar");
        builder.command().add(barJar.getAbsolutePath());
        assertTrue("Error executing JAR",
                execAndGetOutput(builder).startsWith("FOOBAR"));
    }
    @TestTargetNew(
        level = TestLevel.ADDITIONAL,
        notes = "Functional test.",
        method = "JarOutputStream",
        args = {java.io.OutputStream.class, java.util.jar.Manifest.class}
    )
    @KnownFailure("Maybe not a failure, but dalvikvm -jar is not supported (, as yet).")
    public void test_classpath() throws Exception {
        File resources = Support_Resources.createTempFolder();
        File fooJar = File.createTempFile("hyts_", ".jar");
        fooJar.deleteOnExit();
        JarOutputStream joutFoo = new JarOutputStream(new FileOutputStream(
                fooJar));
        joutFoo.putNextEntry(new JarEntry("foo/bar/execjartest/Foo.class"));
        joutFoo.write(getResource(resources, "hyts_Foo.ser"));
        joutFoo.putNextEntry(new JarEntry("foo/bar/execjartest/Bar.class"));
        joutFoo.write(getResource(resources, "hyts_Bar.ser"));
        joutFoo.close();
        ProcessBuilder builder = javaProcessBuilder();
        builder.environment().put("CLASSPATH", fooJar.getAbsolutePath());
        builder.command().add("foo.bar.execjartest.Foo");
        assertTrue("Error executing class from ClassPath",
                execAndGetOutput(builder).startsWith("FOOBAR"));
        File booJar = File.createTempFile("hyts_", ".jar");
        booJar.deleteOnExit();
        JarOutputStream joutBoo = new JarOutputStream(new FileOutputStream(
                booJar));
        joutBoo.putNextEntry(new JarEntry("foo/bar/execjartest/Foo.class"));
        String booBody = new String(getResource(resources, "hyts_Foo.ser"),
                "iso-8859-1");
        booBody = booBody.replaceFirst("FOO", "BOO");
        joutBoo.write(booBody.getBytes("iso-8859-1"));
        joutBoo.putNextEntry(new JarEntry("foo/bar/execjartest/Bar.class"));
        String farBody = new String(getResource(resources, "hyts_Bar.ser"),
                "iso-8859-1");
        farBody = farBody.replaceFirst("BAR", "FAR");
        joutBoo.write(farBody.getBytes("iso-8859-1"));
        joutBoo.close();
        builder = javaProcessBuilder();
        builder.environment().put("CLASSPATH", fooJar.getAbsolutePath());
        builder.command().add("-cp");
        builder.command().add(booJar.getAbsolutePath());
        builder.command().add("foo.bar.execjartest.Foo");
        assertTrue("Error executing class specified by -cp",
                execAndGetOutput(builder).startsWith("BOOFAR"));
        Manifest man = new Manifest();
        Attributes att = man.getMainAttributes();
        att.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        att.put(Attributes.Name.MAIN_CLASS, "foo.bar.execjartest.Foo");
        File zooJar = File.createTempFile("hyts_", ".jar");
        zooJar.deleteOnExit();
        JarOutputStream joutZoo = new JarOutputStream(new FileOutputStream(
                zooJar), man);
        joutZoo.putNextEntry(new JarEntry("foo/bar/execjartest/Foo.class"));
        String zooBody = new String(getResource(resources, "hyts_Foo.ser"),
                "iso-8859-1");
        zooBody = zooBody.replaceFirst("FOO", "ZOO");
        joutZoo.write(zooBody.getBytes("iso-8859-1"));
        joutZoo.putNextEntry(new JarEntry("foo/bar/execjartest/Bar.class"));
        String zarBody = new String(getResource(resources, "hyts_Bar.ser"),
                "iso-8859-1");
        zarBody = zarBody.replaceFirst("BAR", "ZAR");
        joutZoo.write(zarBody.getBytes("iso-8859-1"));
        joutZoo.close();
        builder = javaProcessBuilder();
        builder.environment().put("CLASSPATH", fooJar.getAbsolutePath());
        builder.command().add("-cp");
        builder.command().add(booJar.getAbsolutePath());
        builder.command().add("-jar");
        builder.command().add(zooJar.getAbsolutePath());
        assertTrue("Error executing class specified by -jar",
                execAndGetOutput(builder).startsWith("ZOOZAR"));
    }
    private static byte[] getResource(File tempDir, String resourceName)
            throws IOException {
        Support_Resources.copyFile(tempDir, null, resourceName);
        File resourceFile = new File(tempDir, resourceName);
        resourceFile.deleteOnExit();
        byte[] resourceBody = new byte[(int) resourceFile.length()];
        FileInputStream fis = new FileInputStream(resourceFile);
        fis.read(resourceBody);
        fis.close();
        return resourceBody;
    }
}
