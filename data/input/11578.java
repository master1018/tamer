public class GetXSpace {
    private static SecurityManager [] sma = { null, new Allow(), new DenyFSA(),
                                              new DenyRead() };
    private static final String name = System.getProperty("os.name");
    private static final String dfFormat;
    static {
        if (name.equals("SunOS") || name.equals("Linux")) {
            dfFormat = "([^\\s]+)\\s+(\\d+)\\s+\\d+\\s+(\\d+)\\s+\\d+%\\s+([^\\s]+)";
        } else if (name.startsWith("Windows")) {
            dfFormat = "([^\\s]+)\\s+\\(([^\\s]+)\\)\\s+(\\d+)\\/(\\d+)\\s+";
        } else {
            throw new RuntimeException("unrecognized system:"
                                       + " os.name == " + name);
        }
    }
    private static Pattern dfPattern = Pattern.compile(dfFormat);
    private static int fail = 0;
    private static int pass = 0;
    private static Throwable first;
    static void pass() {
        pass++;
    }
    static void fail(String p) {
        if (first == null)
            setFirst(p);
        System.err.format("FAILED: %s%n", p);
        fail++;
    }
    static void fail(String p, long exp, String cmp, long got) {
        String s = String.format("'%s': %d %s %d", p, exp, cmp, got);
        if (first == null)
            setFirst(s);
        System.err.format("FAILED: %s%n", s);
        fail++;
    }
    private static void fail(String p, Class ex) {
        String s = String.format("'%s': expected %s - FAILED%n", p, ex.getName());
        if (first == null)
            setFirst(s);
        System.err.format("FAILED: %s%n", s);
        fail++;
    }
    private static void setFirst(String s) {
        try {
            throw new RuntimeException(s);
        } catch (RuntimeException x) {
            first = x;
        }
    }
    private static class Space {
        private static final long KSIZE = 1024;
        private String name;
        private long total;
        private long free;
        Space(String total, String free, String name) {
            try {
                this.total = Long.valueOf(total) * KSIZE;
                this.free = Long.valueOf(free) * KSIZE;
            } catch (NumberFormatException x) {
                assert false;
            }
            this.name = name;
        }
        String name() { return name; }
        long total() { return total; }
        long free() { return free; }
        boolean woomFree(long freeSpace) {
            return ((freeSpace >= (free / 10)) && (freeSpace <= (free * 10)));
        }
        public String toString() {
            return String.format("%s (%d/%d)", name, free, total);
        }
    }
    private static ArrayList space(String f) throws IOException {
        ArrayList al = new ArrayList();
        Process p = null;
        String cmd = "df -k" + (f == null ? "" : " " + f);
        p = Runtime.getRuntime().exec(cmd);
        BufferedReader in = new BufferedReader
            (new InputStreamReader(p.getInputStream()));
        String s;
        int i = 0;
        StringBuilder sb = new StringBuilder();
        while ((s = in.readLine()) != null) {
            if (i++ == 0 && !name.startsWith("Windows")) continue;
            sb.append(s).append("\n");
        }
        Matcher m = dfPattern.matcher(sb);
        int j = 0;
        while (j < sb.length()) {
            if (m.find(j)) {
                if (!name.startsWith("Windows")) {
                    if (!m.group(1).equals("swap")) {
                        String name = (f == null ? m.group(4): f);
                        al.add(new Space(m.group(2), m.group(3), name));;
                    }
                } else {
                    String name = (f == null ? m.group(2) : f);
                    al.add(new Space(m.group(4), m.group(3), name ));;
                }
                j = m.end() + 1;
            } else {
                throw new RuntimeException("unrecognized df output format: "
                                           + "charAt(" + j + ") = '"
                                           + sb.charAt(j) + "'");
            }
        }
        if (al.size() == 0) {
            String name = (f == null ? "" : f);
            al.add(new Space("0", "0", name));
        }
        in.close();
        return al;
    }
    private static void tryCatch(Space s) {
        out.format("%s:%n", s.name());
        File f = new File(s.name());
        SecurityManager sm = System.getSecurityManager();
        if (sm instanceof Deny) {
            String fmt = "  %14s: \"%s\" thrown as expected%n";
            try {
                f.getTotalSpace();
                fail(s.name(), SecurityException.class);
            } catch (SecurityException x) {
                out.format(fmt, "getTotalSpace", x);
                pass();
            }
            try {
                f.getFreeSpace();
                fail(s.name(), SecurityException.class);
            } catch (SecurityException x) {
                out.format(fmt, "getFreeSpace", x);
                pass();
            }
            try {
                f.getUsableSpace();
                fail(s.name(), SecurityException.class);
            } catch (SecurityException x) {
                out.format(fmt, "getUsableSpace", x);
                pass();
            }
        }
    }
    private static void compare(Space s) {
        File f = new File(s.name());
        long ts = f.getTotalSpace();
        long fs = f.getFreeSpace();
        long us = f.getUsableSpace();
        out.format("%s:%n", s.name());
        String fmt = "  %-4s total= %12d free = %12d usable = %12d%n";
        out.format(fmt, "df", s.total(), 0, s.free());
        out.format(fmt, "getX", ts, fs, us);
        if (ts != s.total())
            fail(s.name(), s.total(), "!=", ts);
        else
            pass();
        long tsp = (!name.startsWith("Windows") ? us : fs);
        if (!s.woomFree(tsp))
            fail(s.name(), s.free(), "??", tsp);
        else
            pass();
        if (fs > s.total())
            fail(s.name(), s.total(), ">", fs);
        else
            pass();
        if (us > s.total())
            fail(s.name(), s.total(), ">", us);
        else
            pass();
    }
    private static String FILE_PREFIX = "/getSpace.";
    private static void compareZeroNonExist() {
        File f;
        while (true) {
            f = new File(FILE_PREFIX + Math.random());
            if (f.exists())
                continue;
            break;
        }
        long [] s = { f.getTotalSpace(), f.getFreeSpace(), f.getUsableSpace() };
        for (int i = 0; i < s.length; i++) {
            if (s[i] != 0L)
                fail(f.getName(), s[i], "!=", 0L);
            else
                pass();
        }
    }
    private static void compareZeroExist() {
        try {
            File f = File.createTempFile("tmp", null, new File("."));
            long [] s = { f.getTotalSpace(), f.getFreeSpace(), f.getUsableSpace() };
            for (int i = 0; i < s.length; i++) {
                if (s[i] == 0L)
                    fail(f.getName(), s[i], "==", 0L);
                else
                    pass();
            }
        } catch (IOException x) {
            fail("Couldn't create temp file for test");
        }
    }
    private static class Allow extends SecurityManager {
        public void checkRead(String file) {}
        public void checkPermission(Permission p) {}
        public void checkPermission(Permission p, Object context) {}
    }
    private static class Deny extends SecurityManager {
        public void checkPermission(Permission p) {
            if (p.implies(new RuntimePermission("setSecurityManager"))
                || p.implies(new RuntimePermission("getProtectionDomain")))
              return;
            super.checkPermission(p);
        }
        public void checkPermission(Permission p, Object context) {
            if (p.implies(new RuntimePermission("setSecurityManager"))
                || p.implies(new RuntimePermission("getProtectionDomain")))
              return;
            super.checkPermission(p, context);
        }
    }
    private static class DenyFSA extends Deny {
        private String err = "sorry - getFileSystemAttributes";
        public void checkPermission(Permission p) {
            if (p.implies(new RuntimePermission("getFileSystemAttributes")))
                throw new SecurityException(err);
            super.checkPermission(p);
        }
        public void checkPermission(Permission p, Object context) {
            if (p.implies(new RuntimePermission("getFileSystemAttributes")))
                throw new SecurityException(err);
            super.checkPermission(p, context);
        }
    }
    private static class DenyRead extends Deny {
        private String err = "sorry - checkRead()";
        public void checkRead(String file) {
            throw new SecurityException(err);
        }
    }
    private static void testFile(String dirName) {
        out.format("--- Testing %s%n", dirName);
        ArrayList l;
        try {
            l = space(dirName);
        } catch (IOException x) {
            throw new RuntimeException(dirName + " can't get file system information", x);
        }
        compare((GetXSpace.Space) l.get(0));
    }
    private static void testDF() {
        out.format("--- Testing df");
        ArrayList l;
        try {
            l = space(null);
        } catch (IOException x) {
            throw new RuntimeException("can't get file system information", x);
        }
        if (l.size() == 0)
            throw new RuntimeException("no partitions?");
        for (int i = 0; i < sma.length; i++) {
            System.setSecurityManager(sma[i]);
            SecurityManager sm = System.getSecurityManager();
            if (sma[i] != null && sm == null)
                throw new RuntimeException("Test configuration error "
                                           + " - can't set security manager");
            out.format("%nSecurityManager = %s%n" ,
                       (sm == null ? "null" : sm.getClass().getName()));
            for (int j = 0; j < l.size(); j++) {
                Space s = (GetXSpace.Space) l.get(j);
                if (sm instanceof Deny) {
                    tryCatch(s);
                } else {
                    compare(s);
                    compareZeroNonExist();
                    compareZeroExist();
                }
            }
        }
    }
    public static void main(String [] args) {
        if (args.length > 0) {
            testFile(args[0]);
        } else {
            testDF();
        }
        if (fail != 0)
            throw new RuntimeException((fail + pass) + " tests: "
                                       + fail + " failure(s), first", first);
        else
            out.format("all %d tests passed%n", fail + pass);
    }
}
