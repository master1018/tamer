import com.sun.tools.classfile.AccessFlags;
import com.sun.tools.classfile.ClassFile;
import com.sun.tools.classfile.ConstantPoolException;
import com.sun.tools.classfile.Method;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashSet;
public class CompareTest {
    String[][] testCases = {
        { },
        { "-jni" },
    };
    public static void main(String... args) throws Exception {
        new CompareTest().run(args);
    }
    public void run(String... args) throws Exception {
        old_javah_cmd = new File(args[0]);
        rt_jar = new File(args[1]);
        Set<String> testClasses;
        if (args.length > 2) {
            testClasses = new LinkedHashSet<String>(Arrays.asList(Arrays.copyOfRange(args, 2, args.length)));
        } else
            testClasses = getNativeClasses(new JarFile(rt_jar));
        for (String[] options: testCases) {
            for (String name: testClasses) {
                test(Arrays.asList(options), rt_jar, name);
            }
        }
        if (errors == 0)
            System.out.println(count + " tests passed");
        else
            throw new Exception(errors + "/" + count + " tests failed");
    }
    public void test(List<String> options, File bootclasspath, String className)
            throws IOException, InterruptedException {
        System.err.println("test: " + options + " " + className);
        count++;
        testOptions = options;
        testClassName = className;
        File oldOutDir = initDir(file(new File("old"), className));
        int old_rc = old_javah(options, oldOutDir, bootclasspath, className);
        File newOutDir = initDir(file(new File("new"), className));
        int new_rc = new_javah(options, newOutDir, bootclasspath, className);
        if (old_rc != new_rc)
            error("return codes differ; old: " + old_rc + ", new: " + new_rc);
        compare(oldOutDir, newOutDir);
    }
    int old_javah(List<String> options, File outDir, File bootclasspath, String className)
            throws IOException, InterruptedException {
        List<String> cmd = new ArrayList<String>();
        cmd.add(old_javah_cmd.getPath());
        cmd.addAll(options);
        cmd.add("-d");
        cmd.add(outDir.getPath());
        cmd.add("-bootclasspath");
        cmd.add(bootclasspath.getPath());
        cmd.add(className);
        System.err.println("old_javah: " + cmd);
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process p = pb.start();
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = in.readLine()) != null) {
            sb.append(line);
            sb.append("\n");
        }
        System.err.println("old javah out: " + sb.toString());
        return p.waitFor();
    }
    int new_javah(List<String> options, File outDir, File bootclasspath, String className) {
        List<String> args = new ArrayList<String>();
        args.addAll(options);
        args.add("-d");
        args.add(outDir.getPath());
        args.add("-bootclasspath");
        args.add(bootclasspath.getPath());
        args.add(className);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        int rc = com.sun.tools.javah.Main.run(args.toArray(new String[args.size()]), pw);
        pw.close();
        System.err.println("new javah out: " + sw.toString());
        return rc;
    }
    Set<String> getNativeClasses(JarFile jar) throws IOException, ConstantPoolException {
        System.err.println("getNativeClasses: " + jar.getName());
        Set<String> results = new TreeSet<String>();
        Enumeration<JarEntry> e = jar.entries();
        while (e.hasMoreElements()) {
            JarEntry je = e.nextElement();
            if (isNativeClass(jar, je)) {
                String name = je.getName();
                results.add(name.substring(0, name.length() - 6).replace("/", "."));
            }
        }
        return results;
    }
    boolean isNativeClass(JarFile jar, JarEntry entry) throws IOException, ConstantPoolException {
        String name = entry.getName();
        if (name.startsWith("META-INF") || !name.endsWith(".class"))
            return false;
        InputStream in = jar.getInputStream(entry);
        ClassFile cf = ClassFile.read(in);
        for (int i = 0; i < cf.methods.length; i++) {
            Method m = cf.methods[i];
            if (m.access_flags.is(AccessFlags.ACC_NATIVE)) {
                return true;
            }
        }
        return false;
    }
    void compare(File f1, File f2) throws IOException {
        if (f1.isFile() && f2.isFile())
            compareFiles(f1, f2);
        else if (f1.isDirectory() && f2.isDirectory())
            compareDirectories(f1, f2);
        else
            error("files differ: "
                + f1 + " (" + getFileType(f1) + "), "
                + f2 + " (" + getFileType(f2) + ")");
    }
    void compareDirectories(File d1, File d2) throws IOException {
        Set<String> list = new TreeSet<String>();
        list.addAll(Arrays.asList(d1.list()));
        list.addAll(Arrays.asList(d2.list()));
        for (String c: list)
            compare(new File(d1, c), new File(d2, c));
    }
    void compareFiles(File f1, File f2) throws IOException {
        byte[] c1 = readFile(f1);
        byte[] c2 = readFile(f2);
        if (!Arrays.equals(c1, c2))
            error("files differ: " + f1 + ", " + f2);
    }
    byte[] readFile(File file) throws IOException {
        int size = (int) file.length();
        byte[] data = new byte[size];
        DataInputStream in = new DataInputStream(new FileInputStream(file));
        try {
            in.readFully(data);
        } finally {
            in.close();
        }
        return data;
    }
    String getFileType(File f) {
        return f.isDirectory() ? "directory"
                : f.isFile() ? "file"
                : f.exists() ? "other"
                : "not found";
    }
    public File initDir(File dir) {
        if (dir.exists())
            deleteAll(dir);
        dir.mkdirs();
        return dir;
    }
    boolean deleteAll(File file) {
        if (file.isDirectory()) {
            for (File f: file.listFiles())
                deleteAll(f);
        }
        return file.delete();
    }
    File file(File dir, String... path) {
        File f = dir;
        for (String p: path)
            f = new File(f, p);
        return f;
    }
    void error(String msg, String... more) {
        System.err.println("test: " + testOptions + " " + testClassName);
        System.err.println("error: " + msg);
        for (String s: more)
            System.err.println(s);
        errors++;
        System.exit(1);
    }
    File old_javah_cmd;
    File rt_jar;
    List<String> testOptions;
    String testClassName;
    int count;
    int errors;
}
