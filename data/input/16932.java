public class Status {
    private static int N = 160;
    public static void main(String args[])
        throws Exception
    {
        if (!System.getProperty("os.name").equals("Linux"))
            return;
        for (int i = 0; i < N; i++) {
            Process p = Runtime.getRuntime().exec("false");
            int s = p.waitFor();
            System.out.print(s);
            System.out.print(' ');
            if (s != 1) {
                System.out.println();
                throw new Exception("Wrong status");
            }
        }
        System.out.println();
    }
}
