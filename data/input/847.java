public class ExecWithInput {
    private static final String CAT = "/bin/cat";
    private static final int N = 200;
    static int go(int i) throws Exception {
        Process p = Runtime.getRuntime().exec(CAT);
        String input = i + ": line 1\n" + i + ": line 2\n";
        StringBufferInputStream in = new StringBufferInputStream(input);
        IO ioIn = new IO("stdin", in, p.getOutputStream());
        IO ioOut = new IO("stdout", p.getInputStream(), System.out);
        IO ioErr = new IO("stderr", p.getErrorStream(), System.err);
        return p.waitFor();
    }
    public static void main(String[] args) throws Exception {
        if (!System.getProperty("os.name").equals("Linux"))
            return;
        if (File.separatorChar == '\\') {
            return;
        }
        for (int i = 0; i < N; i++)
            go(i);
    }
    static class IO extends Thread {
        private InputStream in;
        private OutputStream out;
        IO(String name, InputStream in, OutputStream out)
        {
            this.in = in;
            this.out = out;
            setName(name);
            start();
        }
        public void run() {
            try {
                int c;
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) != -1) {
                    out.write(buf, 0, n);
                    out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (!System.out.equals(out) && !System.err.equals(out)) {
                    if (out != null) {
                        try { out.close(); } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
