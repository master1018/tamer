public class T6572945
{
    static File testSrc = new File(System.getProperty("test.src", "."));
    static File testClasses = new File(System.getProperty("test.classes", "."));
    static boolean isWindows = System.getProperty("os.name").startsWith("Windows");
    public static void main(String... args)
        throws IOException, InterruptedException
    {
        boolean ok = new T6572945().run(args);
        if (!ok)
            throw new Error("Test Failed");
    }
    public boolean run(String[] args)
        throws IOException, InterruptedException
    {
        if (args.length == 1)
            jdk = new File(args[0]);
        test("-o", "jni.file.1",  "-jni", "TestClass1");
        test("-o", "jni.file.2",  "-jni", "TestClass1", "TestClass2");
        test("-d", "jni.dir.1",   "-jni", "TestClass1", "TestClass2");
        test("-o", "jni.file.3",  "-jni", "TestClass3");
        return (errors == 0);
    }
    void test(String... args)
        throws IOException, InterruptedException
    {
        String[] cp_args = new String[args.length + 2];
        cp_args[0] = "-classpath";
        cp_args[1] = testClasses.getPath();
        System.arraycopy(args, 0, cp_args, 2, args.length);
        if (jdk != null)
            init(cp_args);
        File out = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-o")) {
                out = new File(args[++i]);
                break;
            } else if (args[i].equals("-d")) {
                out = new File(args[++i]);
                out.mkdirs();
                break;
            }
        }
        try {
            System.out.println("test: " + Arrays.asList(cp_args));
            PrintWriter err = new PrintWriter(System.err, true);
            int rc = Main.run(cp_args, err);
            if (rc != 0) {
                error("javah failed: rc=" + rc);
                return;
            }
            compare(new File(new File(testSrc, "gold"), out.getName()), out);
        } catch (Throwable t) {
            t.printStackTrace();
            error("javah threw exception");
        }
    }
    void init(String[] args) throws IOException, InterruptedException {
        String[] cmdArgs = new String[args.length + 1];
        cmdArgs[0] = new File(new File(jdk, "bin"), "javah").getPath();
        System.arraycopy(args, 0, cmdArgs, 1, args.length);
        System.out.println("init: " + Arrays.asList(cmdArgs));
        ProcessBuilder pb = new ProcessBuilder(cmdArgs);
        pb.directory(new File(testSrc, "gold"));
        pb.redirectErrorStream(true);
        Process p = pb.start();
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = in.readLine()) != null)
            System.out.println("javah: " + line);
        int rc = p.waitFor();
        if (rc != 0)
            error("javah: exit code " + rc);
    }
    void compare(File f1, File f2) {
        compare(f1, f2, null);
    }
    void compare(File f1, File f2, String p) {
        File f1p = (p == null ? f1 : new File(f1, p));
        File f2p = (p == null ? f2 : new File(f2, p));
        System.out.println("compare " + f1p + " " + f2p);
        if (f1p.isDirectory() && f2p.isDirectory()) {
            Set<String> children = new HashSet<String>();
            children.addAll(Arrays.asList(f1p.list()));
            children.addAll(Arrays.asList(f2p.list()));
            for (String c: children) {
                compare(f1, f2, new File(p, c).getPath()); 
            }
        }
        else if (f1p.isFile() && f2p.isFile()) {
            String s1 = read(f1p);
            if (isWindows) {
                s1 = s1.replaceAll("( [0-9]+)LL\n", "$1i64\n");
            }
            String s2 = read(f2p);
            if (!s1.equals(s2)) {
                System.out.println("File: " + f1p + "\n" + s1);
                System.out.println("File: " + f2p + "\n" + s2);
                error("Files differ: " + f1p + " " + f2p);
            }
        }
        else if (f1p.exists() && !f2p.exists())
            error("Only in " + f1 + ": " + p);
        else if (f2p.exists() && !f1p.exists())
            error("Only in " + f2 + ": " + p);
        else
            error("Files differ: " + f1p + " " + f2p);
    }
    private String read(File f) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(f));
            try {
                StringBuilder sb = new StringBuilder((int) f.length());
                String line;
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n");
                }
                return sb.toString();
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        } catch (IOException e) {
            error("error reading " + f + ": " + e);
            return "";
        }
    }
    private void error(String msg) {
        System.out.println(msg);
        errors++;
    }
    private int errors;
    private File jdk;
}
