public class TTY implements EventNotifier {
    EventHandler handler = null;
    private List<String> monitorCommands = new ArrayList<String>();
    private int monitorCount = 0;
    private static final String progname = "jdb";
    @Override
    public void vmStartEvent(VMStartEvent se)  {
        Thread.yield();  
        MessageOutput.lnprint("VM Started:");
    }
    @Override
    public void vmDeathEvent(VMDeathEvent e)  {
    }
    @Override
    public void vmDisconnectEvent(VMDisconnectEvent e)  {
    }
    @Override
    public void threadStartEvent(ThreadStartEvent e)  {
    }
    @Override
    public void threadDeathEvent(ThreadDeathEvent e)  {
    }
    @Override
    public void classPrepareEvent(ClassPrepareEvent e)  {
    }
    @Override
    public void classUnloadEvent(ClassUnloadEvent e)  {
    }
    @Override
    public void breakpointEvent(BreakpointEvent be)  {
        Thread.yield();  
        MessageOutput.lnprint("Breakpoint hit:");
    }
    @Override
    public void fieldWatchEvent(WatchpointEvent fwe)  {
        Field field = fwe.field();
        ObjectReference obj = fwe.object();
        Thread.yield();  
        if (fwe instanceof ModificationWatchpointEvent) {
            MessageOutput.lnprint("Field access encountered before after",
                                  new Object [] {field,
                                                 fwe.valueCurrent(),
                                                 ((ModificationWatchpointEvent)fwe).valueToBe()});
        } else {
            MessageOutput.lnprint("Field access encountered", field.toString());
        }
    }
    @Override
    public void stepEvent(StepEvent se)  {
        Thread.yield();  
        MessageOutput.lnprint("Step completed:");
    }
    @Override
    public void exceptionEvent(ExceptionEvent ee) {
        Thread.yield();  
        Location catchLocation = ee.catchLocation();
        if (catchLocation == null) {
            MessageOutput.lnprint("Exception occurred uncaught",
                                  ee.exception().referenceType().name());
        } else {
            MessageOutput.lnprint("Exception occurred caught",
                                  new Object [] {ee.exception().referenceType().name(),
                                                 Commands.locationString(catchLocation)});
        }
    }
    @Override
    public void methodEntryEvent(MethodEntryEvent me) {
        Thread.yield();  
        if (me.request().suspendPolicy() != EventRequest.SUSPEND_NONE) {
            MessageOutput.lnprint("Method entered:");
        } else {
            MessageOutput.print("Method entered:");
            printLocationOfEvent(me);
        }
    }
    @Override
    public boolean methodExitEvent(MethodExitEvent me) {
        Thread.yield();  
        Method mmm = Env.atExitMethod();
        Method meMethod = me.method();
        if (mmm == null || mmm.equals(meMethod)) {
            if (me.request().suspendPolicy() != EventRequest.SUSPEND_NONE) {
                MessageOutput.println();
            }
            if (Env.vm().canGetMethodReturnValues()) {
                MessageOutput.print("Method exitedValue:", me.returnValue() + "");
            } else {
                MessageOutput.print("Method exited:");
            }
            if (me.request().suspendPolicy() == EventRequest.SUSPEND_NONE) {
                printLocationOfEvent(me);
            }
            if (false) {
                Env.setAtExitMethod(null);
                EventRequestManager erm = Env.vm().eventRequestManager();
                for (EventRequest eReq : erm.methodExitRequests()) {
                    if (eReq.equals(me.request())) {
                        eReq.disable();
                    }
                }
            }
            return true;
        }
        return false;
    }
    @Override
    public void vmInterrupted() {
        Thread.yield();  
        printCurrentLocation();
        for (String cmd : monitorCommands) {
            StringTokenizer t = new StringTokenizer(cmd);
            t.nextToken();  
            executeCommand(t);
        }
        MessageOutput.printPrompt();
    }
    @Override
    public void receivedEvent(Event event) {
    }
    private void printBaseLocation(String threadName, Location loc) {
        MessageOutput.println("location",
                              new Object [] {threadName,
                                             Commands.locationString(loc)});
    }
    private void printCurrentLocation() {
        ThreadInfo threadInfo = ThreadInfo.getCurrentThreadInfo();
        StackFrame frame;
        try {
            frame = threadInfo.getCurrentFrame();
        } catch (IncompatibleThreadStateException exc) {
            MessageOutput.println("<location unavailable>");
            return;
        }
        if (frame == null) {
            MessageOutput.println("No frames on the current call stack");
        } else {
            Location loc = frame.location();
            printBaseLocation(threadInfo.getThread().name(), loc);
            if (loc.lineNumber() != -1) {
                String line;
                try {
                    line = Env.sourceLine(loc, loc.lineNumber());
                } catch (java.io.IOException e) {
                    line = null;
                }
                if (line != null) {
                    MessageOutput.println("source line number and line",
                                          new Object [] {new Integer(loc.lineNumber()),
                                                         line});
                }
            }
        }
        MessageOutput.println();
    }
    private void printLocationOfEvent(LocatableEvent theEvent) {
        printBaseLocation(theEvent.thread().name(), theEvent.location());
    }
    void help() {
        MessageOutput.println("zz help text");
    }
    private static final String[][] commandList = {
        {"!!",           "n",         "y"},
        {"?",            "y",         "y"},
        {"bytecodes",    "n",         "y"},
        {"catch",        "y",         "n"},
        {"class",        "n",         "y"},
        {"classes",      "n",         "y"},
        {"classpath",    "n",         "y"},
        {"clear",        "y",         "n"},
        {"connectors",   "y",         "y"},
        {"cont",         "n",         "n"},
        {"disablegc",    "n",         "n"},
        {"down",         "n",         "y"},
        {"dump",         "n",         "y"},
        {"enablegc",     "n",         "n"},
        {"eval",         "n",         "y"},
        {"exclude",      "y",         "n"},
        {"exit",         "y",         "y"},
        {"extension",    "n",         "y"},
        {"fields",       "n",         "y"},
        {"gc",           "n",         "n"},
        {"help",         "y",         "y"},
        {"ignore",       "y",         "n"},
        {"interrupt",    "n",         "n"},
        {"kill",         "n",         "n"},
        {"lines",        "n",         "y"},
        {"list",         "n",         "y"},
        {"load",         "n",         "y"},
        {"locals",       "n",         "y"},
        {"lock",         "n",         "n"},
        {"memory",       "n",         "y"},
        {"methods",      "n",         "y"},
        {"monitor",      "n",         "n"},
        {"next",         "n",         "n"},
        {"pop",          "n",         "n"},
        {"print",        "n",         "y"},
        {"quit",         "y",         "y"},
        {"read",         "y",         "y"},
        {"redefine",     "n",         "n"},
        {"reenter",      "n",         "n"},
        {"resume",       "n",         "n"},
        {"run",          "y",         "n"},
        {"save",         "n",         "n"},
        {"set",          "n",         "n"},
        {"sourcepath",   "y",         "y"},
        {"step",         "n",         "n"},
        {"stepi",        "n",         "n"},
        {"stop",         "y",         "n"},
        {"suspend",      "n",         "n"},
        {"thread",       "n",         "y"},
        {"threadgroup",  "n",         "y"},
        {"threadgroups", "n",         "y"},
        {"threadlocks",  "n",         "y"},
        {"threads",      "n",         "y"},
        {"trace",        "n",         "n"},
        {"unmonitor",    "n",         "n"},
        {"untrace",      "n",         "n"},
        {"unwatch",      "y",         "n"},
        {"up",           "n",         "y"},
        {"use",          "y",         "y"},
        {"version",      "y",         "y"},
        {"watch",        "y",         "n"},
        {"where",        "n",         "y"},
        {"wherei",       "n",         "y"},
    };
    private int isCommand(String key) {
        int low = 0;
        int high = commandList.length - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            String midVal = commandList[mid][0];
            int compare = midVal.compareTo(key);
            if (compare < 0) {
                low = mid + 1;
            } else if (compare > 0) {
                high = mid - 1;
            }
            else {
                return mid; 
        }
        }
        return -(low + 1);  
    };
    private boolean isDisconnectCmd(int ii) {
        if (ii < 0 || ii >= commandList.length) {
            return false;
        }
        return (commandList[ii][1].equals("y"));
    }
    private boolean isReadOnlyCmd(int ii) {
        if (ii < 0 || ii >= commandList.length) {
            return false;
        }
        return (commandList[ii][2].equals("y"));
    };
    void executeCommand(StringTokenizer t) {
        String cmd = t.nextToken().toLowerCase();
        boolean showPrompt = true;
        if (!cmd.startsWith("#")) {
            if (Character.isDigit(cmd.charAt(0)) && t.hasMoreTokens()) {
                try {
                    int repeat = Integer.parseInt(cmd);
                    String subcom = t.nextToken("");
                    while (repeat-- > 0) {
                        executeCommand(new StringTokenizer(subcom));
                        showPrompt = false; 
                    }
                } catch (NumberFormatException exc) {
                    MessageOutput.println("Unrecognized command.  Try help...", cmd);
                }
            } else {
                int commandNumber = isCommand(cmd);
                if (commandNumber < 0) {
                    MessageOutput.println("Unrecognized command.  Try help...", cmd);
                } else if (!Env.connection().isOpen() && !isDisconnectCmd(commandNumber)) {
                    MessageOutput.println("Command not valid until the VM is started with the run command",
                                          cmd);
                } else if (Env.connection().isOpen() && !Env.vm().canBeModified() &&
                           !isReadOnlyCmd(commandNumber)) {
                    MessageOutput.println("Command is not supported on a read-only VM connection",
                                          cmd);
                } else {
                    Commands evaluator = new Commands();
                    try {
                        if (cmd.equals("print")) {
                            evaluator.commandPrint(t, false);
                            showPrompt = false;        
                        } else if (cmd.equals("eval")) {
                            evaluator.commandPrint(t, false);
                            showPrompt = false;        
                        } else if (cmd.equals("set")) {
                            evaluator.commandSet(t);
                            showPrompt = false;        
                        } else if (cmd.equals("dump")) {
                            evaluator.commandPrint(t, true);
                            showPrompt = false;        
                        } else if (cmd.equals("locals")) {
                            evaluator.commandLocals();
                        } else if (cmd.equals("classes")) {
                            evaluator.commandClasses();
                        } else if (cmd.equals("class")) {
                            evaluator.commandClass(t);
                        } else if (cmd.equals("connectors")) {
                            evaluator.commandConnectors(Bootstrap.virtualMachineManager());
                        } else if (cmd.equals("methods")) {
                            evaluator.commandMethods(t);
                        } else if (cmd.equals("fields")) {
                            evaluator.commandFields(t);
                        } else if (cmd.equals("threads")) {
                            evaluator.commandThreads(t);
                        } else if (cmd.equals("thread")) {
                            evaluator.commandThread(t);
                        } else if (cmd.equals("suspend")) {
                            evaluator.commandSuspend(t);
                        } else if (cmd.equals("resume")) {
                            evaluator.commandResume(t);
                        } else if (cmd.equals("cont")) {
                            evaluator.commandCont();
                        } else if (cmd.equals("threadgroups")) {
                            evaluator.commandThreadGroups();
                        } else if (cmd.equals("threadgroup")) {
                            evaluator.commandThreadGroup(t);
                        } else if (cmd.equals("catch")) {
                            evaluator.commandCatchException(t);
                        } else if (cmd.equals("ignore")) {
                            evaluator.commandIgnoreException(t);
                        } else if (cmd.equals("step")) {
                            evaluator.commandStep(t);
                        } else if (cmd.equals("stepi")) {
                            evaluator.commandStepi();
                        } else if (cmd.equals("next")) {
                            evaluator.commandNext();
                        } else if (cmd.equals("kill")) {
                            evaluator.commandKill(t);
                        } else if (cmd.equals("interrupt")) {
                            evaluator.commandInterrupt(t);
                        } else if (cmd.equals("trace")) {
                            evaluator.commandTrace(t);
                        } else if (cmd.equals("untrace")) {
                            evaluator.commandUntrace(t);
                        } else if (cmd.equals("where")) {
                            evaluator.commandWhere(t, false);
                        } else if (cmd.equals("wherei")) {
                            evaluator.commandWhere(t, true);
                        } else if (cmd.equals("up")) {
                            evaluator.commandUp(t);
                        } else if (cmd.equals("down")) {
                            evaluator.commandDown(t);
                        } else if (cmd.equals("load")) {
                            evaluator.commandLoad(t);
                        } else if (cmd.equals("run")) {
                            evaluator.commandRun(t);
                            if ((handler == null) && Env.connection().isOpen()) {
                                handler = new EventHandler(this, false);
                            }
                        } else if (cmd.equals("memory")) {
                            evaluator.commandMemory();
                        } else if (cmd.equals("gc")) {
                            evaluator.commandGC();
                        } else if (cmd.equals("stop")) {
                            evaluator.commandStop(t);
                        } else if (cmd.equals("clear")) {
                            evaluator.commandClear(t);
                        } else if (cmd.equals("watch")) {
                            evaluator.commandWatch(t);
                        } else if (cmd.equals("unwatch")) {
                            evaluator.commandUnwatch(t);
                        } else if (cmd.equals("list")) {
                            evaluator.commandList(t);
                        } else if (cmd.equals("lines")) { 
                            evaluator.commandLines(t);
                        } else if (cmd.equals("classpath")) {
                            evaluator.commandClasspath(t);
                        } else if (cmd.equals("use") || cmd.equals("sourcepath")) {
                            evaluator.commandUse(t);
                        } else if (cmd.equals("monitor")) {
                            monitorCommand(t);
                        } else if (cmd.equals("unmonitor")) {
                            unmonitorCommand(t);
                        } else if (cmd.equals("lock")) {
                            evaluator.commandLock(t);
                            showPrompt = false;        
                        } else if (cmd.equals("threadlocks")) {
                            evaluator.commandThreadlocks(t);
                        } else if (cmd.equals("disablegc")) {
                            evaluator.commandDisableGC(t);
                            showPrompt = false;        
                        } else if (cmd.equals("enablegc")) {
                            evaluator.commandEnableGC(t);
                            showPrompt = false;        
                        } else if (cmd.equals("save")) { 
                            evaluator.commandSave(t);
                            showPrompt = false;        
                        } else if (cmd.equals("bytecodes")) { 
                            evaluator.commandBytecodes(t);
                        } else if (cmd.equals("redefine")) {
                            evaluator.commandRedefine(t);
                        } else if (cmd.equals("pop")) {
                            evaluator.commandPopFrames(t, false);
                        } else if (cmd.equals("reenter")) {
                            evaluator.commandPopFrames(t, true);
                        } else if (cmd.equals("extension")) {
                            evaluator.commandExtension(t);
                        } else if (cmd.equals("exclude")) {
                            evaluator.commandExclude(t);
                        } else if (cmd.equals("read")) {
                            readCommand(t);
                        } else if (cmd.equals("help") || cmd.equals("?")) {
                            help();
                        } else if (cmd.equals("version")) {
                            evaluator.commandVersion(progname,
                                                     Bootstrap.virtualMachineManager());
                        } else if (cmd.equals("quit") || cmd.equals("exit")) {
                            if (handler != null) {
                                handler.shutdown();
                            }
                            Env.shutdown();
                        } else {
                            MessageOutput.println("Unrecognized command.  Try help...", cmd);
                        }
                    } catch (VMCannotBeModifiedException rovm) {
                        MessageOutput.println("Command is not supported on a read-only VM connection", cmd);
                    } catch (UnsupportedOperationException uoe) {
                        MessageOutput.println("Command is not supported on the target VM", cmd);
                    } catch (VMNotConnectedException vmnse) {
                        MessageOutput.println("Command not valid until the VM is started with the run command",
                                              cmd);
                    } catch (Exception e) {
                        MessageOutput.printException("Internal exception:", e);
                    }
                }
            }
        }
        if (showPrompt) {
            MessageOutput.printPrompt();
        }
    }
    void monitorCommand(StringTokenizer t) {
        if (t.hasMoreTokens()) {
            ++monitorCount;
            monitorCommands.add(monitorCount + ": " + t.nextToken(""));
        } else {
            for (String cmd : monitorCommands) {
                MessageOutput.printDirectln(cmd);
            }
        }
    }
    void unmonitorCommand(StringTokenizer t) {
        if (t.hasMoreTokens()) {
            String monTok = t.nextToken();
            int monNum;
            try {
                monNum = Integer.parseInt(monTok);
            } catch (NumberFormatException exc) {
                MessageOutput.println("Not a monitor number:", monTok);
                return;
            }
            String monStr = monTok + ":";
            for (String cmd : monitorCommands) {
                StringTokenizer ct = new StringTokenizer(cmd);
                if (ct.nextToken().equals(monStr)) {
                    monitorCommands.remove(cmd);
                    MessageOutput.println("Unmonitoring", cmd);
                    return;
                }
            }
            MessageOutput.println("No monitor numbered:", monTok);
        } else {
            MessageOutput.println("Usage: unmonitor <monitor#>");
        }
    }
    void readCommand(StringTokenizer t) {
        if (t.hasMoreTokens()) {
            String cmdfname = t.nextToken();
            if (!readCommandFile(new File(cmdfname))) {
                MessageOutput.println("Could not open:", cmdfname);
            }
        } else {
            MessageOutput.println("Usage: read <command-filename>");
        }
    }
    boolean readCommandFile(File f) {
        BufferedReader inFile = null;
        try {
            if (f.canRead()) {
                MessageOutput.println("*** Reading commands from", f.getPath());
                inFile = new BufferedReader(new FileReader(f));
                String ln;
                while ((ln = inFile.readLine()) != null) {
                    StringTokenizer t = new StringTokenizer(ln);
                    if (t.hasMoreTokens()) {
                        executeCommand(t);
                    }
                }
            }
        } catch (IOException e) {
        } finally {
            if (inFile != null) {
                try {
                    inFile.close();
                } catch (Exception exc) {
                }
            }
        }
        return inFile != null;
    }
    String readStartupCommandFile(String dir, String fname, String canonPath) {
        File dotInitFile = new File(dir, fname);
        if (!dotInitFile.exists()) {
            return null;
        }
        String myCanonFile;
        try {
            myCanonFile = dotInitFile.getCanonicalPath();
        } catch (IOException ee) {
            MessageOutput.println("Could not open:", dotInitFile.getPath());
            return null;
        }
        if (canonPath == null || !canonPath.equals(myCanonFile)) {
            if (!readCommandFile(dotInitFile)) {
                MessageOutput.println("Could not open:", dotInitFile.getPath());
            }
        }
        return myCanonFile;
    }
    public TTY() throws Exception {
        MessageOutput.println("Initializing progname", progname);
        if (Env.connection().isOpen() && Env.vm().canBeModified()) {
            this.handler = new EventHandler(this, true);
        }
        try {
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(System.in));
            String lastLine = null;
            Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
            {
                String userHome = System.getProperty("user.home");
                String canonPath;
                if ((canonPath = readStartupCommandFile(userHome, "jdb.ini", null)) == null) {
                    canonPath = readStartupCommandFile(userHome, ".jdbrc", null);
                }
                String userDir = System.getProperty("user.dir");
                if (readStartupCommandFile(userDir, "jdb.ini", canonPath) == null) {
                    readStartupCommandFile(userDir, ".jdbrc", canonPath);
                }
            }
            MessageOutput.printPrompt();
            while (true) {
                String ln = in.readLine();
                if (ln == null) {
                    MessageOutput.println("Input stream closed.");
                    ln = "quit";
                }
                if (ln.startsWith("!!") && lastLine != null) {
                    ln = lastLine + ln.substring(2);
                    MessageOutput.printDirectln(ln);
                }
                StringTokenizer t = new StringTokenizer(ln);
                if (t.hasMoreTokens()) {
                    lastLine = ln;
                    executeCommand(t);
                } else {
                    MessageOutput.printPrompt();
                }
            }
        } catch (VMDisconnectedException e) {
            handler.handleDisconnectedException();
        }
    }
    private static void usage() {
        MessageOutput.println("zz usage text", new Object [] {progname,
                                                     File.pathSeparator});
        System.exit(1);
    }
    static void usageError(String messageKey) {
        MessageOutput.println(messageKey);
        MessageOutput.println();
        usage();
    }
    static void usageError(String messageKey, String argument) {
        MessageOutput.println(messageKey, argument);
        MessageOutput.println();
        usage();
    }
    private static boolean supportsSharedMemory() {
        for (Connector connector :
                 Bootstrap.virtualMachineManager().allConnectors()) {
            if (connector.transport() == null) {
                continue;
            }
            if ("dt_shmem".equals(connector.transport().name())) {
                return true;
            }
        }
        return false;
    }
    private static String addressToSocketArgs(String address) {
        int index = address.indexOf(':');
        if (index != -1) {
            String hostString = address.substring(0, index);
            String portString = address.substring(index + 1);
            return "hostname=" + hostString + ",port=" + portString;
        } else {
            return "port=" + address;
        }
    }
    private static boolean hasWhitespace(String string) {
        int length = string.length();
        for (int i = 0; i < length; i++) {
            if (Character.isWhitespace(string.charAt(i))) {
                return true;
            }
        }
        return false;
    }
    private static String addArgument(String string, String argument) {
        if (hasWhitespace(argument) || argument.indexOf(',') != -1) {
            StringBuffer buffer = new StringBuffer(string);
            buffer.append('"');
            for (int i = 0; i < argument.length(); i++) {
                char c = argument.charAt(i);
                if (c == '"') {
                    buffer.append('\\');
                }
                buffer.append(c);
            }
            buffer.append("\" ");
            return buffer.toString();
        } else {
            return string + argument + ' ';
        }
    }
    public static void main(String argv[]) throws MissingResourceException {
        String cmdLine = "";
        String javaArgs = "";
        int traceFlags = VirtualMachine.TRACE_NONE;
        boolean launchImmediately = false;
        String connectSpec = null;
        MessageOutput.textResources = ResourceBundle.getBundle
            ("com.sun.tools.example.debug.tty.TTYResources",
             Locale.getDefault());
        for (int i = 0; i < argv.length; i++) {
            String token = argv[i];
            if (token.equals("-dbgtrace")) {
                if ((i == argv.length - 1) ||
                    ! Character.isDigit(argv[i+1].charAt(0))) {
                    traceFlags = VirtualMachine.TRACE_ALL;
                } else {
                    String flagStr = "";
                    try {
                        flagStr = argv[++i];
                        traceFlags = Integer.decode(flagStr).intValue();
                    } catch (NumberFormatException nfe) {
                        usageError("dbgtrace flag value must be an integer:",
                                   flagStr);
                        return;
                    }
                }
            } else if (token.equals("-X")) {
                usageError("Use java minus X to see");
                return;
            } else if (
                   token.equals("-v") || token.startsWith("-v:") ||  
                   token.startsWith("-verbose") ||                  
                   token.startsWith("-D") ||
                   token.startsWith("-X") ||
                   token.equals("-noasyncgc") || token.equals("-prof") ||
                   token.equals("-verify") || token.equals("-noverify") ||
                   token.equals("-verifyremote") ||
                   token.equals("-verbosegc") ||
                   token.startsWith("-ms") || token.startsWith("-mx") ||
                   token.startsWith("-ss") || token.startsWith("-oss") ) {
                javaArgs = addArgument(javaArgs, token);
            } else if (token.equals("-tclassic")) {
                usageError("Classic VM no longer supported.");
                return;
            } else if (token.equals("-tclient")) {
                javaArgs = "-client " + javaArgs;
            } else if (token.equals("-tserver")) {
                javaArgs = "-server " + javaArgs;
            } else if (token.equals("-sourcepath")) {
                if (i == (argv.length - 1)) {
                    usageError("No sourcepath specified.");
                    return;
                }
                Env.setSourcePath(argv[++i]);
            } else if (token.equals("-classpath")) {
                if (i == (argv.length - 1)) {
                    usageError("No classpath specified.");
                    return;
                }
                javaArgs = addArgument(javaArgs, token);
                javaArgs = addArgument(javaArgs, argv[++i]);
            } else if (token.equals("-attach")) {
                if (connectSpec != null) {
                    usageError("cannot redefine existing connection", token);
                    return;
                }
                if (i == (argv.length - 1)) {
                    usageError("No attach address specified.");
                    return;
                }
                String address = argv[++i];
                if (supportsSharedMemory()) {
                    connectSpec = "com.sun.jdi.SharedMemoryAttach:name=" +
                                   address;
                } else {
                    String suboptions = addressToSocketArgs(address);
                    connectSpec = "com.sun.jdi.SocketAttach:" + suboptions;
                }
            } else if (token.equals("-listen") || token.equals("-listenany")) {
                if (connectSpec != null) {
                    usageError("cannot redefine existing connection", token);
                    return;
                }
                String address = null;
                if (token.equals("-listen")) {
                    if (i == (argv.length - 1)) {
                        usageError("No attach address specified.");
                        return;
                    }
                    address = argv[++i];
                }
                if (supportsSharedMemory()) {
                    connectSpec = "com.sun.jdi.SharedMemoryListen:";
                    if (address != null) {
                        connectSpec += ("name=" + address);
                    }
                } else {
                    connectSpec = "com.sun.jdi.SocketListen:";
                    if (address != null) {
                        connectSpec += addressToSocketArgs(address);
                    }
                }
            } else if (token.equals("-launch")) {
                launchImmediately = true;
            } else if (token.equals("-listconnectors")) {
                Commands evaluator = new Commands();
                evaluator.commandConnectors(Bootstrap.virtualMachineManager());
                return;
            } else if (token.equals("-connect")) {
                if (connectSpec != null) {
                    usageError("cannot redefine existing connection", token);
                    return;
                }
                if (i == (argv.length - 1)) {
                    usageError("No connect specification.");
                    return;
                }
                connectSpec = argv[++i];
            } else if (token.equals("-help")) {
                usage();
            } else if (token.equals("-version")) {
                Commands evaluator = new Commands();
                evaluator.commandVersion(progname,
                                         Bootstrap.virtualMachineManager());
                System.exit(0);
            } else if (token.startsWith("-")) {
                usageError("invalid option", token);
                return;
            } else {
                cmdLine = addArgument("", token);
                for (i++; i < argv.length; i++) {
                    cmdLine = addArgument(cmdLine, argv[i]);
                }
                break;
            }
        }
        if (connectSpec == null) {
            connectSpec = "com.sun.jdi.CommandLineLaunch:";
        } else if (!connectSpec.endsWith(",") && !connectSpec.endsWith(":")) {
            connectSpec += ","; 
        }
        cmdLine = cmdLine.trim();
        javaArgs = javaArgs.trim();
        if (cmdLine.length() > 0) {
            if (!connectSpec.startsWith("com.sun.jdi.CommandLineLaunch:")) {
                usageError("Cannot specify command line with connector:",
                           connectSpec);
                return;
            }
            connectSpec += "main=" + cmdLine + ",";
        }
        if (javaArgs.length() > 0) {
            if (!connectSpec.startsWith("com.sun.jdi.CommandLineLaunch:")) {
                usageError("Cannot specify target vm arguments with connector:",
                           connectSpec);
                return;
            }
            connectSpec += "options=" + javaArgs + ",";
        }
        try {
            if (! connectSpec.endsWith(",")) {
                connectSpec += ","; 
            }
            Env.init(connectSpec, launchImmediately, traceFlags);
            new TTY();
        } catch(Exception e) {
            MessageOutput.printException("Internal exception:", e);
        }
    }
}
