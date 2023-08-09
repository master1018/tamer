public class General {
    public static boolean debug = false;
    private static boolean win32 = (File.separatorChar == '\\');
    private static int gensymCounter = 0;
    private static String gensym() {
        return "x." + ++gensymCounter;
    }
    private static String findSomeFile(String dir, String subdir, String[] dl) {
        for (int i = 0; i < dl.length; i++) {
            File f = new File(subdir, dl[i]);
            File df = new File(dir, f.getPath());
            if (df.exists() && df.isFile()) {
                return f.getPath();
            }
        }
        for (int i = 0; i < dl.length; i++) {
            File f = (subdir.length() == 0) ? new File(dl[i])
                                            : new File(subdir, dl[i]);
            File df = new File(dir, f.getPath());
            if (df.exists() && df.isDirectory()) {
                String[] dl2 = df.list();
                if (dl2 != null) {
                    String ff = findSomeFile(dir, f.getPath(), dl2);
                    if (ff != null) return ff;
                }
            }
        }
        return null;
    }
    private static String findSomeFile(String dir, boolean create) {
        File d = new File(dir);
        String[] dl = d.list();
        if (dl == null) {
            throw new RuntimeException("Can't list " + dir);
        }
        for (int i = 0; i < dl.length; i++) {
            File f = new File(dir, dl[i]);
            if (f.isFile()) {
                return dl[i];
            }
        }
        String f = findSomeFile(dir, "", dl);
        if (f != null) {
            return f;
        }
        if (create) {
            File nf = new File(d, gensym());
            OutputStream os;
            try {
                os = new FileOutputStream(nf);
                os.close();
            } catch (IOException x) {
                throw new RuntimeException("Can't create a file in " + dir);
            }
            return nf.getName();
        }
        return null;
    }
    private static String findSomeDir(String dir, boolean create) {
        File d = new File(dir);
        String[] dl = d.list();
        if (dl == null) {
            throw new RuntimeException("Can't list " + dir);
        }
        for (int i = 0; i < dl.length; i++) {
            File f = new File(d, dl[i]);
            if (f.isDirectory() && f.canRead()) {
                String[] dl2 = f.list();
                if (dl2.length >= 250) {
                    continue;
                }
                return dl[i];
            }
        }
        if (create) {
            File sd = new File(d, gensym());
            if (sd.mkdir()) return sd.getName();
        }
        return null;
    }
    private static String findNon(String dir) {
        File d = new File(dir);
        String[] x = new String[] { "foo", "bar", "baz" };
        for (int i = 0; i < x.length; i++) {
            File f = new File(d, x[i]);
            if (!f.exists()) {
                return x[i];
            }
        }
        for (int i = 0; i < 1024; i++) {
            String n = "xx" + Integer.toString(i);
            File f = new File(d, n);
            if (!f.exists()) {
                return n;
            }
        }
        throw new RuntimeException("Can't find a non-existent file in " + dir);
    }
    public static void ensureNon(String fn) {
        if ((new File(fn)).exists()) {
            throw new RuntimeException("Test path " + fn + " exists");
        }
    }
    private static boolean isSlash(char x) {
        if (x == File.separatorChar) return true;
        if (win32 && (x == '/')) return true;
        return false;
    }
    private static String trimTrailingSlashes(String s) {
        int n = s.length();
        if (n == 0) return s;
        n--;
        while ((n > 0) && isSlash(s.charAt(n))) {
            if ((n >= 1) && s.charAt(n - 1) == ':') break;
            n--;
        }
        return s.substring(0, n + 1);
    }
    private static String pathConcat(String a, String b) {
        if (a.length() == 0) return b;
        if (b.length() == 0) return a;
        if (isSlash(a.charAt(a.length() - 1))
            || isSlash(b.charAt(0))
            || (win32 && (a.charAt(a.length() - 1) == ':'))) {
            return a + b;
        } else {
            return a + File.separatorChar + b;
        }
    }
    private static Hashtable checked = new Hashtable();
    public static void check(String answer, String path) throws IOException {
        String ans = trimTrailingSlashes(answer);
        if (path.length() == 0) return;
        if (checked.get(path) != null) {
            System.err.println("DUP " + path);
            return;
        }
        checked.put(path, path);
        String cpath;
        try {
            File f = new File(path);
            cpath = f.getCanonicalPath();
            if (f.exists() && f.isFile() && f.canRead()) {
                InputStream in = new FileInputStream(path);
                in.close();
                RandomAccessFile raf = new RandomAccessFile(path, "r");
                raf.close();
            }
        } catch (IOException x) {
            System.err.println(ans + " <-- " + path + " ==> " + x);
            if (debug) return;
            else throw x;
        }
        if (cpath.equals(ans)) {
            System.err.println(ans + " <== " + path);
        } else {
            System.err.println(ans + " <-- " + path + " ==> " + cpath + " MISMATCH");
            if (!debug) {
                throw new RuntimeException("Mismatch: " + path + " ==> " + cpath +
                                           ", should be " + ans);
            }
        }
    }
    public static void checkSlash(int depth, boolean create,
                                  String ans, String ask, String slash)
        throws Exception
    {
        check(ans, ask + slash);
        checkNames(depth, create,
                   ans.endsWith(File.separator) ? ans : ans + File.separator,
                   ask + slash);
    }
    public static void checkSlashes(int depth, boolean create,
                                    String ans, String ask)
        throws Exception
    {
        check(ans, ask);
        if (depth == 0) return;
        checkSlash(depth, create, ans, ask, "/");
        checkSlash(depth, create, ans, ask, "
        checkSlash(depth, create, ans, ask, "
        if (win32) {
            checkSlash(depth, create, ans, ask, "\\");
            checkSlash(depth, create, ans, ask, "\\\\");
            checkSlash(depth, create, ans, ask, "\\/");
            checkSlash(depth, create, ans, ask, "/\\");
            checkSlash(depth, create, ans, ask, "\\\\\\");
        }
    }
    public static void checkNames(int depth, boolean create,
                                  String ans, String ask)
        throws Exception
    {
        int d = depth - 1;
        File f = new File(ans);
        String n;
        if (f.exists()) {
            if (f.isDirectory() && f.canRead()) {
                if ((n = findSomeFile(ans, create)) != null)
                    checkSlashes(d, create, ans + n, ask + n);
                if ((n = findSomeDir(ans, create)) != null)
                    checkSlashes(d, create, ans + n, ask + n);
            }
            n = findNon(ans);
            checkSlashes(d, create, ans + n, ask + n);
        } else {
            n = "foo" + depth;
            checkSlashes(d, create, ans + n, ask + n);
        }
        checkSlashes(d, create, trimTrailingSlashes(ans), ask + ".");
        if ((n = f.getParent()) != null) {
            String n2;
            if (win32
                && ((n2 = f.getParentFile().getParent()) != null)
                && n2.equals("\\\\")) {
                checkSlashes(d, create, ans, ask + "..");
            } else {
                checkSlashes(d, create, n, ask + "..");
            }
        }
        else {
            if (win32)
                checkSlashes(d, create, ans, ask + "..");
            else {
                File thisPath = new File(ask);
                File nextPath = new File(ask + "..");
                if (!thisPath.getCanonicalPath().equals(nextPath.getCanonicalPath()))
                    checkSlashes(d, create, ans + "..", ask + "..");
            }
        }
    }
}
