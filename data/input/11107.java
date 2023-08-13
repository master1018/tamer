public class ExecWithLotsOfArgs {
    public static class EchoingHelper {
        public static void main(String[] args) {
            for (int i = 0; i < args.length; i++) {
                System.out.println(args[i]);
            }
        }
    }
    public static void main(String[] args) throws Exception {
        String[] command = new String[300];
        int n = 0;
        command[n++] = System.getProperty("java.home") + File.separator +
            "bin" + File.separator + "java";
        if (System.getProperty("java.class.path") != null) {
            command[n++] = "-classpath";
            command[n++] = System.getProperty("java.class.path");
        }
        command[n++] = "ExecWithLotsOfArgs$EchoingHelper";
        for (int i = n; i < command.length; i++) {
            command[i] = new String(new Integer(i).toString());
        }
        Process p = null;
        p = Runtime.getRuntime().exec(command);
        BufferedReader in = new BufferedReader
            (new InputStreamReader(p.getInputStream()));
        String s;
        int count = n;
        while ((s = in.readLine()) != null) {
            if (count >= command.length) {
                failed("Was expecting " + (command.length - 2) +
                       " strings to be echo'ed back, but got " +
                       (count - 1) + " instead");
            }
            if (!s.equals(command[count])) {
                failed("Exec'd process returned \"" +
                       s + "\", was expecting \""  +
                       command[count] + "\"");
            }
            count++;
        }
        if (count == n) {
            in = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((s = in.readLine()) != null) {
                System.err.println("Error output: " + s);
            }
            failed("Exec'd process didn't writing anything to its stdout");
        }
    }
    private static void failed(String s) {
        throw new RuntimeException("Failed: " + s);
    }
}
