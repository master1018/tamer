public class ExecWithDir {
    private static final String CMD = "/bin/true";
    private static final int N = 500;
    public static void main(String args[]) throws Exception {
        if (! new File(CMD).canExecute())
            return;
        File dir = new File(".");
        for (int i = 1; i <= N; i++) {
            System.out.print(i);
            System.out.print(" e");
            Process p = Runtime.getRuntime().exec(CMD, null, dir);
            System.out.print('w');
            int s = p.waitFor();
            System.out.println("x " + s);
            if (s != 0) throw new Error("Unexpected return code " + s);
            p.getInputStream().close();
            p.getOutputStream().close();
            p.getErrorStream().close();
        }
    }
}
