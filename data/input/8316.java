public class ArgWithSpaceAndFinalBackslash {
    private static String getJavaCommand() {
        String javaHome = System.getProperty("java.home");
        if (javaHome != null && javaHome.length() > 0)
            return (javaHome
                    + File.separatorChar + "bin"
                    + File.separatorChar + "java");
        else
            return "java";
    }
    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            System.err.println(args[0]);
            return;
        }
        String[] cmd = new String[5];
        int i = 0;
        cmd[i++] = getJavaCommand();
        cmd[i++] = "-cp";
        String cp = System.getProperty("test.classes");
        if (cp == null)
            cp = ".";
        cmd[i++] = cp;
        cmd[i++] = "ArgWithSpaceAndFinalBackslash";
        cmd[i++] = "foo bar\\baz\\";
        Process process = Runtime.getRuntime().exec(cmd);
        InputStream in = process.getErrorStream();
        byte[] buf = new byte[1024];
        int n = 0, d;
        while ((d = in.read(buf, n, buf.length - n)) >= 0)
            n += d;
        String s = new String(buf, 0, n, "US-ASCII").trim();
        if (!s.equals(cmd[i - 1]))
            throw new Exception("Test failed: Got \"" + s
                                + "\", expected \"" + cmd[i - 1] + "\"");
    }
}
