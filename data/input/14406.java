class MyInputStream implements Runnable {
    private String              name;
    private BufferedInputStream in;
    private StringBuffer        buffer;
    MyInputStream(String name, InputStream in) {
        this.name = name;
        this.in = new BufferedInputStream(in);
        buffer = new StringBuffer(4096);
        Thread thr = new Thread(this);
        thr.setDaemon(true);
        thr.start();
    }
    void dump(PrintStream x) {
        String str = buffer.toString();
        x.println("<beginning of " + name + " buffer>");
        x.println(str);
        x.println("<end of buffer>");
    }
    boolean contains(String pattern) {
        String str = buffer.toString();
        return str.contains(pattern);
    }
    public void run() {
        try {
            byte b[] = new byte[100];
            for (;;) {
                int n = in.read(b);
                String str;
                if (n < 0) {
                    break;
                }
                str = new String(b, 0, n);
                buffer.append(str);
                System.out.print(str);
            }
        } catch (IOException ioe) {  }
    }
}
public class HatRun {
    private String        all_hprof_options;
    private String        all_hat_options;
    private String        dumpfile;
    private MyInputStream output;
    private MyInputStream error;
    public HatRun(String hprof_options, String hat_options)
    {
        all_hprof_options = hprof_options;
        all_hat_options   = hat_options;
    }
    public void runit(String class_name)
    {
        runit(class_name, null);
    }
    private void execute(String cmd[])
    {
        Process p;
        String cmdLine = "";
        int i;
        for ( i = 0 ; i < cmd.length; i++ ) {
          cmdLine += cmd[i];
          cmdLine += " ";
        }
        System.out.println("Starting: " + cmdLine);
        try {
            p = Runtime.getRuntime().exec(cmd);
        } catch ( IOException e ) {
            throw new RuntimeException("Test failed - exec got IO exception");
        }
        output = new MyInputStream("Input Stream", p.getInputStream());
        error  = new MyInputStream("Error Stream", p.getErrorStream());
        try {
            int exitStatus;
            exitStatus = p.waitFor();
            if ( exitStatus != 0) {
                System.out.println("Exit code is " + exitStatus);
                error.dump(System.out);
                output.dump(System.out);
                throw new RuntimeException("Test failed - " +
                                    "exit return code non-zero " +
                                    "(exitStatus==" + exitStatus + ")");
            }
        } catch ( InterruptedException e ) {
            throw new RuntimeException("Test failed - process interrupted");
        }
        System.out.println("Completed: " + cmdLine);
    }
    public void runit(String class_name, String vm_options[])
    {
        String jre_home  = System.getProperty("java.home");
        String sdk_home  = (jre_home.endsWith("jre") ?
                            (jre_home + File.separator + "..") :
                            jre_home );
        String cdir      = System.getProperty("test.classes", ".");
        String os_arch   = System.getProperty("os.arch");
        String os_name   = System.getProperty("os.name");
        boolean d64      = os_name.equals("SunOS") && (
                             os_arch.equals("sparcv9") ||
                             os_arch.equals("amd64"));
        String isa_dir   = d64?(File.separator+os_arch):"";
        String java      = jre_home
                             + File.separator + "bin" + isa_dir
                             + File.separator + "java";
        String jhat      = sdk_home + File.separator + "bin"
                           + File.separator + "jhat";
        int nvm_options = 0;
        if ( vm_options != null ) nvm_options = vm_options.length;
        String cmd[]     = new String[1 + (d64?1:0) + 7 + nvm_options];
        int i,j;
        i = 0;
        cmd[i++] = java;
        cmd[i++] = "-cp";
        cmd[i++] = cdir;
        cmd[i++] = "-Dtest.classes=" + cdir;
        if ( d64 ) {
            cmd[i++] = "-d64";
        }
        cmd[i++] = "-Xcheck:jni";
        cmd[i++] = "-Xverify:all";
        dumpfile= cdir + File.separator + class_name + ".hdump";
        cmd[i++] = "-agentlib:hprof=" + all_hprof_options
                    + ",format=b,file=" + dumpfile;
        for ( j = 0; j < nvm_options; j++ ) {
            cmd[i++] = vm_options[j];
        }
        cmd[i++] = class_name;
        execute(cmd);
        String jhat_cmd[] = new String[4];
        jhat_cmd[0] = jhat;
        jhat_cmd[1] = "-debug";
        jhat_cmd[2] = "2";
        jhat_cmd[3] = dumpfile;
        execute(jhat_cmd);
    }
    public boolean output_contains(String pattern)
    {
        return output.contains(pattern) || error.contains(pattern);
    }
}
