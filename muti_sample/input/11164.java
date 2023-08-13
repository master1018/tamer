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
public class DemoRun {
    private String        demo_name;
    private String        demo_options;
    private MyInputStream output;
    private MyInputStream error;
    public DemoRun(String name, String options)
    {
        demo_name    = name;
        demo_options = options;
    }
    public void runit(String class_name)
    {
        runit(class_name, null);
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
        String libprefix = os_name.contains("Windows")?"":"lib";
        String libsuffix = os_name.contains("Windows")?".dll":".so";
        boolean d64      =    ( os_name.contains("Solaris") ||
                                os_name.contains("SunOS") )
                           && ( os_arch.equals("sparcv9") ||
                                os_arch.equals("amd64"));
        boolean hprof    = demo_name.equals("hprof");
        String isa_dir   = d64?(File.separator+os_arch):"";
        String java      = jre_home
                             + File.separator + "bin" + isa_dir
                             + File.separator + "java";
        int nvm_options = 0;
        if ( vm_options != null ) nvm_options = vm_options.length;
        String cmd[]     = new String[1 + (d64?1:0) + 7 + nvm_options];
        String cmdLine;
        int exitStatus;
        int i,j;
        i = 0;
        cmdLine = "";
        cmdLine += (cmd[i++] = java);
        cmdLine += " ";
        cmdLine += (cmd[i++] = "-cp");
        cmdLine += " ";
        cmdLine += (cmd[i++] = cdir);
        cmdLine += " ";
        cmdLine += (cmd[i++] = "-Dtest.classes=" + cdir);
        if ( d64 ) {
            cmdLine += " ";
            cmdLine += (cmd[i++] = "-d64");
        }
        cmdLine += " ";
        cmdLine += (cmd[i++] = "-Xcheck:jni");
        cmdLine += " ";
        cmdLine += (cmd[i++] = "-Xverify:all");
        if ( hprof ) {
            cmdLine += " ";
            cmdLine += (cmd[i++] = "-agentlib:" + demo_name
                     + (demo_options.equals("")?"":("="+demo_options)));
        } else {
            String libname  = sdk_home
                         + File.separator + "demo"
                         + File.separator + "jvmti"
                         + File.separator + demo_name
                         + File.separator + "lib" + isa_dir
                         + File.separator + libprefix + demo_name + libsuffix;
            cmdLine += " ";
            cmdLine += (cmd[i++] = "-agentpath:" + libname
                     + (demo_options.equals("")?"":("="+demo_options)));
        }
        for ( j = 0; j < nvm_options; j++ ) {
            cmdLine += " ";
            cmdLine += (cmd[i++] = vm_options[j]);
        }
        cmdLine += " ";
        cmdLine += (cmd[i++] = class_name);
        Process p;
        System.out.println("Starting: " + cmdLine);
        try {
            p = Runtime.getRuntime().exec(cmd);
        } catch ( IOException e ) {
            throw new RuntimeException("Test failed - exec got IO exception");
        }
        output = new MyInputStream("Input Stream", p.getInputStream());
        error  = new MyInputStream("Error Stream", p.getErrorStream());
        try {
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
    public boolean output_contains(String pattern)
    {
        return output.contains(pattern) || error.contains(pattern);
    }
}
