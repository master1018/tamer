public class ConcurrentRead {
    static volatile Exception savedException;
    static final String TEE = "/usr/bin/tee";
    public static void main(String[] args) throws Exception {
        if (File.separatorChar == '\\' ||                
                                !new File(TEE).exists()) 
            return;
        Process p = Runtime.getRuntime().exec(TEE);
        OutputStream out = p.getOutputStream();
        InputStream in = p.getInputStream();
        Thread t1 = new WriterThread(out, in);
        t1.start();
        Thread t2 = new WriterThread(out, in);
        t2.start();
        t1.join();
        t2.join();
        if (savedException != null)
            throw savedException;
    }
    static class WriterThread extends Thread {
        OutputStream out;
        InputStream in;
        WriterThread(OutputStream out, InputStream in) {
            this.out = out;
            this.in = in;
        }
        public void run(){
            try {
                out.write('a');
                out.flush();
                if (in.read() == -1) 
                    throw new Exception("End of stream in writer thread");
            } catch (Exception e) {
                savedException = e;
            }
        }
    }
}
