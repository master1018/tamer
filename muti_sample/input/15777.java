public class T6715753 {
    public static void main(String... args) throws Exception {
        new T6715753().run();
    }
    void run() throws Exception {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        String[] args = { "-notAnOption" };
        int rc = com.sun.tools.javap.Main.run(args, pw);
        String log = sw.toString();
        if (rc == 0
            || log.indexOf("-notAnOption") == -1
            || log.indexOf("javap") == -1) { 
            System.err.println("rc: " + rc + ", log=\n" + log);
            throw new Exception("test failed");
        }
    }
}
