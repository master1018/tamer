public class SetChildEnv
{
    public static void main(String argv[])
        throws Exception
    {
        System.out.println("java.compiler=" + System.getProperty("java.compiler"));
        runwith (new String[0], new String[0]);
        runwith (
            new String[] { "-verbosegc" },
            new String[] { "foo.bar=SetChildEnvTest",
                           "sun.rmi.server.doSomething=true" }
            );
        runwith (
            new String[] { },
            new String[] { "parameter.count=zero" }
            );
        runwith (
            new String[] { "-Xmx32m" },
            new String[] { }
            );
    }
    private static void runwith(
        String[] params,        
        String[] props          
    )
        throws Exception
    {
        TestLibrary.suggestSecurityManager(TestParams.defaultSecurityManager);
        DebugExecWatcher watcher = DebugExecWatcher.makeWithPipe();
        RMID.removeLog();
        RMID rmid = RMID.createRMID(watcher.otherEnd(), watcher.otherEnd(),
                                    true); 
        rmid.start();
        Properties p = new Properties();
        p.put("java.security.policy", TestParams.defaultGroupPolicy);
        p.put("java.security.manager", TestParams.defaultSecurityManager);
        int i;
        for (i = 0; i < props.length; i++) {
            p.put(props[i].substring(0, props[i].indexOf('=')),
                  props[i].substring(props[i].indexOf('=')+1));
        }
        ActivationGroupDesc.CommandEnvironment cmdenv =
                new ActivationGroupDesc.CommandEnvironment(
                    null,
                    params);
        ActivationGroupDesc gdesc = new ActivationGroupDesc(
                p, cmdenv);
        ActivationSystem actsys = ActivationGroup.getSystem();
        ActivationGroupID gid = actsys.registerGroup(gdesc);
        ActivationDesc odesc = new ActivationDesc(gid, 
                                                  "Doctor", 
                                                  null, 
                                                  null); 
        Eliza doctor = (Eliza)Activatable.register(odesc);
        System.out.println ("Invoking complain()...");
        String complaint =
                "HELP ME, DOCTOR.  I FEEL VIOLENT TOWARDS PEOPLE " +
                "WHO INQUIRE ABOUT MY PARENTS.";
        System.out.println(complaint);
        String res = doctor.complain(complaint);
        System.out.println (" => " + res);
        String found = watcher.found;
        if (found == null) {
            int fudge = 15;
            while (found == null && --fudge > 0) {
                Thread.sleep(1000);
                found = watcher.found;
            }
            if (found == null) {
                TestLibrary.bomb("rmid subprocess produced no " +
                                 "recognizable debugExec line");
            }
        }
        System.err.println("debugExec found: <<" + found + ">>");
        int q = found.indexOf('"', found.indexOf("rmid: debugExec"));
        int qe = found.lastIndexOf('"');
        if (q <= 1 || qe <= q) {
            TestLibrary.bomb("rmid subprocess produced " +
                             "mangled debugExec line");
        }
        StringTokenizer tk = new StringTokenizer(found.substring(q+1, qe));
        tk.nextToken();         
        Set argset = new HashSet(tk.countTokens());
        while (tk.hasMoreTokens()) {
            argset.add(tk.nextToken());
        }
        int m;
        for (m = 0; m < params.length; m++) {
            if(!argset.contains(params[m]))
                TestLibrary.bomb("Parameter \"" + params[m] + "\" not set");
        }
        for (m = 0; m < props.length; m++) {
            if (!argset.contains("-D" + props[m])) {
                TestLibrary.bomb("Property binding \"" + props[m] +
                                 "\" not set");
            }
        }
        if (doctor instanceof Retireable)
            ((Retireable)doctor).retire();
        actsys.unregisterGroup(gid);
        Thread.sleep(5000);
        rmid.destroy();
    }
    public static class DebugExecWatcher
        extends Thread
    {
        public String found;
        private BufferedReader str;
        private OutputStream otherEnd;
        private DebugExecWatcher(InputStream readStream, OutputStream wrStream)
        {
            super("DebugExecWatcher");
            found = null;
            str = new BufferedReader(new InputStreamReader(readStream));
            otherEnd = wrStream;
        }
        static public DebugExecWatcher makeWithPipe()
            throws IOException
        {
            PipedOutputStream wr = new PipedOutputStream();
            PipedInputStream rd = new PipedInputStream(wr);
            DebugExecWatcher embryo = new DebugExecWatcher(rd, wr);
            embryo.start();
            return embryo;
        }
        public OutputStream otherEnd()
        {
            return otherEnd;
        }
        public synchronized void notifyLine(String s)
        {
            if (s != null && s.indexOf("rmid: debugExec") != -1)
                found = s;
        }
        public void run()
        {
            try {
                String line;
                while ((line = str.readLine()) != null) {
                    this.notifyLine(line);
                    System.err.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
