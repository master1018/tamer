public class ImplVersionReader extends Thread {
    private BufferedReader in;
    private Process proc;
    public ImplVersionReader(Process p, InputStream is) {
        proc = p;
        in = new BufferedReader(new InputStreamReader(is));
    }
    public void run() {
        boolean terminate = false;
        while (!terminate) {
            try {
                String trace = in.readLine();
                if (trace != null) {
                    System.out.println("ImplVersionCommand: " + trace);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                proc.exitValue();
                terminate = true;
            } catch (IllegalThreadStateException e) {
                terminate = false;
            }
        }
    }
}
