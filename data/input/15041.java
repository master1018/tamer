public class MetaInf {
    static String jarName = "a.jar";
    static String INDEX = "META-INF/INDEX.LIST";
    static String SERVICES = "META-INF/services";
    static String contents =
        System.getProperty("test.src") + File.separatorChar + "jarcontents";
    static void run(String ... args) {
        if (! new Main(System.out, System.err, "jar").run(args))
            throw new Error("jar failed: args=" + Arrays.toString(args));
    }
    static void copy(File from, File to) throws IOException {
        FileInputStream in = new FileInputStream(from);
        FileOutputStream out = new FileOutputStream(to);
        try {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1)
                out.write(buf, 0, n);
        } finally {
            in.close();
            out.close();
        }
    }
    static boolean contains(File jarFile, String entryName)
        throws IOException {
        ZipFile zf = new ZipFile(jarFile);
        if ( zf != null ) {
            boolean result = zf.getEntry(entryName) != null;
            zf.close();
            return result;
        }
        return false;
    }
    static void checkContains(File jarFile, String entryName)
        throws IOException {
        if (! contains(jarFile, entryName))
            throw new Error(String.format("expected jar %s to contain %s",
                                          jarFile, entryName));
    }
    static void testIndex(String jarName) throws IOException {
        System.err.printf("jarName=%s%n", jarName);
        File jar = new File(jarName);
        run("cf", jarName, "-C", contents, SERVICES);
        for (int i = 0; i < 2; i++) {
            run("i", jarName);
            checkContains(jar, INDEX);
            checkContains(jar, SERVICES);
        }
        JarFile f = new JarFile(jarName);
        BufferedReader index =
            new BufferedReader(
                    new InputStreamReader(
                            f.getInputStream(f.getJarEntry(INDEX))));
        String line;
        while ((line = index.readLine()) != null) {
            if (line.equals(SERVICES)) {
                index.close();
                f.close();
                return;
            }
        }
        index.close();
        f.close();
        throw new Error(SERVICES + " not indexed.");
    }
    public static void main(String[] args) throws IOException {
        testIndex("a.jar");             
        testIndex("./a.zip");           
        File tmpFile = File.createTempFile("MetaInf", null, null);
        try {
            testIndex(tmpFile.getPath());
        } finally {
            tmpFile.delete();
        }
    }
}
