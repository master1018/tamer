public class SetCwd {
    public static void testExec(String cmd, String[] cmdarray, boolean flag)
        throws Exception {
        File dir = new File(".");
        File[] files = dir.listFiles();
        String curDir = dir.getCanonicalPath();
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if (f.isDirectory() && (new File(f, "SetCwd.class")).exists()) {
                String newDir = f.getCanonicalPath();
                Process p = null;
                if (flag) {
                    p = Runtime.getRuntime().exec(cmd, null, f);
                } else {
                    p = Runtime.getRuntime().exec(cmdarray, null, f);
                }
                BufferedReader in = new BufferedReader
                    (new InputStreamReader(p.getInputStream()));
                String s = in.readLine();
                if (!s.startsWith(newDir)) {
                    throw new Exception("inconsistent directory after exec");
                }
                p.waitFor();
            }
        }
        System.out.println(curDir);
    }
    public static void main (String args[]) throws Exception {
        String cmdarray[] = new String[2];
        cmdarray[0] = System.getProperty("java.home") + File.separator +
            "bin" + File.separator + "java";
        cmdarray[1] = "SetCwd";
        String cmd = cmdarray[0] + " " + cmdarray[1];
        testExec(cmd, null, true);
        testExec(null, cmdarray, false);
    }
}
