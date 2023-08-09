@TestTargetClass(JarOutputStream.class)
@AndroidOnly("dalvik vm specific")
public class DalvikExecTest extends TestCase {
    String execDalvik1(String classpath, String mainClass, String arg1)
            throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder();
        String base = System.getenv("OUT");
        builder.command().add(base + "/system/bin/dalvikvm");
        builder.command().add("-Djava.io.tmpdir=/tmp/mc");
        builder.command().add("-Duser.language=en");
        builder.command().add("-Duser.region=US");
        if ("true".equals(System.getenv("TARGET_SIMULATOR"))) {
            builder.command().add("-Xbootclasspath:" + System.getProperty("java.boot.class.path"));
        } else {
        }
        builder.command().add("-classpath");
        builder.command().add(classpath);
        builder.command().add(mainClass);
        if (arg1 != null) {
            builder.command().add(arg1);
        }
        return execAndGetOutput(builder);
    }
    String execDalvik (String classpath, String mainClass)
            throws IOException, InterruptedException {
        return execDalvik1(classpath, mainClass, null);
    }
    @TestTargets ({
        @TestTargetNew(
            level = TestLevel.ADDITIONAL,
            notes = "Execute an existing JAR on dalvikvm using -classpath option.",
            clazz = Runtime.class,
            method = "exec",
            args = {java.lang.String[].class}
        )
    })
    public void test_execExistingJar () throws IOException, InterruptedException {
        String res;
        File jarFile;
        if (System.getProperty("java.vendor").contains("Android")) {
            File tempDir = Support_Resources.createTempFolder();
            jarFile = Support_Resources.copyFile(
                    tempDir, null, "cts_dalvikExecTest.jar" );
            res = execDalvik(jarFile.getAbsolutePath(), "dalvikExecTest.HelloWorld");
            assertEquals("Hello Android World!", "Hello Android World!\n", res);
            res = execDalvik(jarFile.getAbsolutePath(), "dalvikExecTest.ResourceDumper");
            assertTrue("Android Resource Dumper started",
                    res.contains("Android Resource Dumper started"));
            assertTrue("This Resource contains some text.",
                    res.contains("This Resource contains some text."));
        } else {
        }
    }
    @TestTargets ({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "putNextEntry",
            args = {java.util.zip.ZipEntry.class}
        ),
        @TestTargetNew(
            level = TestLevel.ADDITIONAL,
            method = "JarOutputStream",
            args = {java.io.OutputStream.class}
        ),
        @TestTargetNew(
            level = TestLevel.ADDITIONAL,
            notes = "Create a temp file, fill it with contents according to Dalvik JAR format, and execute it on dalvikvm using -classpath option.",
            clazz = Runtime.class,
            method = "exec",
            args = {java.lang.String[].class}
        )
    })
    public void test_execCreatedJar () throws IOException, InterruptedException {
        File jarFile = File.createTempFile("cts_dalvikExecTest_", ".jar");
        jarFile.deleteOnExit();
        JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(jarFile));
        jarOut.putNextEntry(new JarEntry("classes.dex"));
        Support_Resources.writeResourceToStream("cts_dalvikExecTest_classes.dex", jarOut);
        jarOut.putNextEntry(new JarEntry("dalvikExecTest/myResource"));
        jarOut.write("This Resource contains some text.".getBytes());
        jarOut.close();
        String res;
        res = execDalvik(jarFile.getAbsolutePath(), "dalvikExecTest.HelloWorld");
        assertEquals("Hello Android World!", "Hello Android World!\n", res);
        res = execDalvik(jarFile.getAbsolutePath(), "dalvikExecTest.ResourceDumper");
        assertTrue("Android Resource Dumper started",
                res.contains("Android Resource Dumper started"));
        assertTrue("This Resource contains some text.",
                res.contains("This Resource contains some text."));
    }
    @TestTargets ({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "putNextEntry",
            args = {java.util.zip.ZipEntry.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "JarOutputStream",
            args = {java.io.OutputStream.class, java.util.jar.Manifest.class}
        ),
        @TestTargetNew(
            level = TestLevel.ADDITIONAL,
            clazz = Runtime.class,
            method = "exec",
            args = {java.lang.String[].class}
        )
    })
    public void test_execCreatedJarWithManifest () throws IOException, InterruptedException {
        File jarFile = File.createTempFile("cts_dalvikExecTest_", ".jar");
        jarFile.deleteOnExit();
        Manifest manifest = new Manifest();
        Attributes attrs = manifest.getMainAttributes();
        attrs.put(Attributes.Name.MANIFEST_VERSION, "3.1415962");
        attrs.put(Attributes.Name.MAIN_CLASS, "dalvikExecTest.HelloWorld");
        attrs.put(Attributes.Name.CLASS_PATH, jarFile.getName());
        JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(jarFile), manifest);
        jarOut.putNextEntry(new JarEntry("classes.dex"));
        Support_Resources.writeResourceToStream("cts_dalvikExecTest_classes.dex", jarOut);
        jarOut.putNextEntry(new JarEntry("dalvikExecTest/myResource"));
        jarOut.write("This Resource contains some text.".getBytes());
        jarOut.close();
        String res;
        res = execDalvik(jarFile.getAbsolutePath(), "dalvikExecTest.HelloWorld");
        assertEquals("Hello Android World!", "Hello Android World!\n", res);
        res = execDalvik(jarFile.getAbsolutePath(), "dalvikExecTest.ResourceDumper");
        assertTrue("Android Resource Dumper started",
                res.contains("Android Resource Dumper started"));
        assertTrue("This Resource contains some text.",
                res.contains("This Resource contains some text."));
        JarFile jarIn = new JarFile(jarFile);
        manifest = jarIn.getManifest();
        attrs = manifest.getMainAttributes();
        assertEquals("MANIFEST_VERSION must match!", "3.1415962",
                attrs.get(Attributes.Name.MANIFEST_VERSION));
        assertEquals("MAIN_CLASS must match!", "dalvikExecTest.HelloWorld",
                attrs.get(Attributes.Name.MAIN_CLASS));
        assertEquals("CLASS_PATH must match!", jarFile.getName(),
                attrs.get(Attributes.Name.CLASS_PATH));
    }
    public static class HelloWorld {
        public static void main(String[] args) {
            System.out.println("Hello Android World!");
        }
    }
    public static class ResourceDumper {
        static ByteArrayOutputStream outputFrom (InputStream input) throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[512];
            int total = 0;
            int count;
            count = input.read(buffer);
            while (count != -1) {
                out.write(buffer, 0, count);
                total = total + count;
                count = input.read(buffer);
            }
            return out;
        }
        public static void main(String[] args) throws IOException {
            System.out.print("Android Resource Dumper started ");
            String fileName;
            if (args.length >= 1) {
                fileName = args[0];
                System.out.format("for argument '%s'.\n", fileName);
            } else {
                System.out.print("standard ");
                fileName = "myResource";
                System.out.println("for standard 'myResource'.");
            }
            InputStream is = ResourceDumper.class.getResourceAsStream(fileName);
            if (is != null) {
                System.out.println("Resource obtained and being dumped:");
                System.out.println(outputFrom(is).toString());
            }
        }
    }
}
