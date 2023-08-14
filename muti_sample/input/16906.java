public class ExecutionManager {
    private Session session;
    int traceMode = VirtualMachine.TRACE_NONE;
    ArrayList<SessionListener> sessionListeners = new ArrayList<SessionListener>();
    public void addSessionListener(SessionListener listener) {
        sessionListeners.add(listener);
    }
    public void removeSessionListener(SessionListener listener) {
        sessionListeners.remove(listener);
    }
  ArrayList<SpecListener> specListeners = new ArrayList<SpecListener>();
    public void addSpecListener(SpecListener cl) {
        specListeners.add(cl);
    }
    public void removeSpecListener(SpecListener cl) {
        specListeners.remove(cl);
    }
    ArrayList<JDIListener> jdiListeners = new ArrayList<JDIListener>();
    public void addJDIListener(JDIListener jl) {
        jdiListeners.add(jl);
    }
    public void addJDIListener(int index, JDIListener jl) {
        jdiListeners.add(index, jl);
    }
    public void removeJDIListener(JDIListener jl) {
        jdiListeners.remove(jl);
    }
    private ArrayList<OutputListener> appEchoListeners = new ArrayList<OutputListener>();
    public void addApplicationEchoListener(OutputListener l) {
        appEchoListeners.add(l);
    }
    public void removeApplicationEchoListener(OutputListener l) {
        appEchoListeners.remove(l);
    }
    private ArrayList<OutputListener> appOutputListeners = new ArrayList<OutputListener>();
    public void addApplicationOutputListener(OutputListener l) {
        appOutputListeners.add(l);
    }
    public void removeApplicationOutputListener(OutputListener l) {
        appOutputListeners.remove(l);
    }
    private ArrayList<OutputListener> appErrorListeners = new ArrayList<OutputListener>();
    public void addApplicationErrorListener(OutputListener l) {
        appErrorListeners.add(l);
    }
    public void removeApplicationErrorListener(OutputListener l) {
        appErrorListeners.remove(l);
    }
    private ArrayList<OutputListener> diagnosticsListeners = new ArrayList<OutputListener>();
    public void addDiagnosticsListener(OutputListener l) {
        diagnosticsListeners.add(l);
    }
    public void removeDiagnosticsListener(OutputListener l) {
        diagnosticsListeners.remove(l);
    }
    public VirtualMachine vm() {
        return session == null ? null : session.vm;
    }
    void ensureActiveSession() throws NoSessionException {
        if (session == null) {
         throw new NoSessionException();
      }
    }
    public EventRequestManager eventRequestManager() {
        return vm() == null ? null : vm().eventRequestManager();
    }
    public int getTraceMode(int mode) {
        return traceMode;
    }
    public void setTraceMode(int mode) {
        traceMode = mode;
        if (session != null) {
            session.setTraceMode(mode);
        }
    }
    public boolean isInterrupted()  {
        return session.interrupted;
    }
    public List<ReferenceType> allClasses() throws NoSessionException {
        ensureActiveSession();
        return vm().allClasses();
    }
    public List<ReferenceType> findClassesByName(String name) throws NoSessionException {
        ensureActiveSession();
        return vm().classesByName(name);
    }
    public List<ReferenceType> findClassesMatchingPattern(String pattern)
                                                throws NoSessionException {
        ensureActiveSession();
        List<ReferenceType> result = new ArrayList<ReferenceType>();  
        if (pattern.startsWith("*.")) {
            pattern = pattern.substring(1);
            for (ReferenceType type : vm().allClasses()) {
                if (type.name().endsWith(pattern)) {
                    result.add(type);
                }
            }
            return result;
        } else {
            return vm().classesByName(pattern);
        }
    }
    public List<ThreadReference> allThreads() throws NoSessionException {
        ensureActiveSession();
        return vm().allThreads();
    }
    public List<ThreadGroupReference> topLevelThreadGroups() throws NoSessionException {
        ensureActiveSession();
        return vm().topLevelThreadGroups();
    }
    public ThreadGroupReference systemThreadGroup()
                                                throws NoSessionException {
        ensureActiveSession();
        return vm().topLevelThreadGroups().get(0);
    }
    public Value evaluate(final StackFrame f, String expr)
        throws ParseException,
                                            InvocationException,
                                            InvalidTypeException,
                                            ClassNotLoadedException,
                                            NoSessionException,
                                            IncompatibleThreadStateException {
        ExpressionParser.GetFrame frameGetter = null;
        ensureActiveSession();
        if (f != null) {
            frameGetter = new ExpressionParser.GetFrame() {
                @Override
                public StackFrame get()  {
                    return f;
                }
            };
        }
        return ExpressionParser.evaluate(expr, vm(), frameGetter);
    }
    public void run(boolean suspended,
                    String vmArgs,
                    String className,
                    String args) throws VMLaunchFailureException {
        endSession();
        if (suspended) {
            List<String> argList = new ArrayList<String>(1);
            argList.add("java.lang.String[]");
            createMethodBreakpoint(className, "main", argList);
        }
        String cmdLine = className + " " + args;
        startSession(new ChildSession(this, vmArgs, cmdLine,
                                      appInput, appOutput, appError,
                                      diagnostics));
    }
    public void attach(String portName) throws VMLaunchFailureException {
        endSession();
        VirtualMachineManager mgr = Bootstrap.virtualMachineManager();
        AttachingConnector connector = mgr.attachingConnectors().get(0);
        Map<String, Connector.Argument> arguments = connector.defaultArguments();
        arguments.get("port").setValue(portName);
        Session newSession = internalAttach(connector, arguments);
        if (newSession != null) {
            startSession(newSession);
        }
    }
    private Session internalAttach(AttachingConnector connector,
                                   Map<String, Connector.Argument> arguments) {
        try {
            VirtualMachine vm = connector.attach(arguments);
            return new Session(vm, this, diagnostics);
        } catch (IOException ioe) {
            diagnostics.putString("\n Unable to attach to target VM: " +
                                  ioe.getMessage());
        } catch (IllegalConnectorArgumentsException icae) {
            diagnostics.putString("\n Invalid connector arguments: " +
                                  icae.getMessage());
        }
        return null;
    }
    private Session internalListen(ListeningConnector connector,
                                   Map<String, Connector.Argument> arguments) {
        try {
            VirtualMachine vm = connector.accept(arguments);
            return new Session(vm, this, diagnostics);
        } catch (IOException ioe) {
            diagnostics.putString(
                  "\n Unable to accept connection to target VM: " +
                                  ioe.getMessage());
        } catch (IllegalConnectorArgumentsException icae) {
            diagnostics.putString("\n Invalid connector arguments: " +
                                  icae.getMessage());
        }
        return null;
    }
    public boolean explictStart(Connector connector, Map<String, Connector.Argument> arguments)
                                           throws VMLaunchFailureException {
        Session newSession = null;
        endSession();
        if (connector instanceof LaunchingConnector) {
            newSession = new ChildSession(this, (LaunchingConnector)connector,
                                          arguments,
                                          appInput, appOutput, appError,
                                          diagnostics);
        } else if (connector instanceof AttachingConnector) {
            newSession = internalAttach((AttachingConnector)connector,
                                        arguments);
        } else if (connector instanceof ListeningConnector) {
            newSession = internalListen((ListeningConnector)connector,
                                        arguments);
        } else {
            diagnostics.putString("\n Unknown connector: " + connector);
        }
        if (newSession != null) {
            startSession(newSession);
        }
        return newSession != null;
    }
    public void detach() throws NoSessionException {
        ensureActiveSession();
        endSession();
    }
    private void startSession(Session s) throws VMLaunchFailureException {
        if (!s.attach()) {
            throw new VMLaunchFailureException();
        }
        session = s;
        EventRequestManager em = vm().eventRequestManager();
        ClassPrepareRequest classPrepareRequest = em.createClassPrepareRequest();
        classPrepareRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        classPrepareRequest.enable();
        ClassUnloadRequest classUnloadRequest = em.createClassUnloadRequest();
        classUnloadRequest.setSuspendPolicy(EventRequest.SUSPEND_NONE);
        classUnloadRequest.enable();
        ThreadStartRequest threadStartRequest = em.createThreadStartRequest();
        threadStartRequest.setSuspendPolicy(EventRequest.SUSPEND_NONE);
        threadStartRequest.enable();
        ThreadDeathRequest threadDeathRequest = em.createThreadDeathRequest();
        threadDeathRequest.setSuspendPolicy(EventRequest.SUSPEND_NONE);
        threadDeathRequest.enable();
        ExceptionRequest exceptionRequest =
                                em.createExceptionRequest(null, false, true);
        exceptionRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        exceptionRequest.enable();
        validateThreadInfo();
        session.interrupted = true;
        notifySessionStart();
    }
    void endSession() {
        if (session != null) {
            session.detach();
            session = null;
            invalidateThreadInfo();
            notifySessionDeath();
        }
    }
    public void interrupt() throws NoSessionException {
        ensureActiveSession();
        vm().suspend();
        validateThreadInfo();
        session.interrupted = true;
        notifyInterrupted();
    }
    public void go() throws NoSessionException, VMNotInterruptedException {
        ensureActiveSession();
        invalidateThreadInfo();
        session.interrupted = false;
        notifyContinued();
        vm().resume();
    }
    void clearPreviousStep(ThreadReference thread) {
         EventRequestManager mgr = vm().eventRequestManager();
         for (StepRequest request : mgr.stepRequests()) {
             if (request.thread().equals(thread)) {
                 mgr.deleteEventRequest(request);
                 break;
             }
         }
    }
    private void generalStep(ThreadReference thread, int size, int depth)
                        throws NoSessionException {
        ensureActiveSession();
        invalidateThreadInfo();
        session.interrupted = false;
        notifyContinued();
        clearPreviousStep(thread);
        EventRequestManager reqMgr = vm().eventRequestManager();
        StepRequest request = reqMgr.createStepRequest(thread,
                                                       size, depth);
        request.addCountFilter(1);
        request.enable();
        vm().resume();
    }
    public void stepIntoInstruction(ThreadReference thread)
                        throws NoSessionException {
        generalStep(thread, StepRequest.STEP_MIN, StepRequest.STEP_INTO);
    }
    public void stepOverInstruction(ThreadReference thread)
                        throws NoSessionException {
        generalStep(thread, StepRequest.STEP_MIN, StepRequest.STEP_OVER);
    }
    public void stepIntoLine(ThreadReference thread)
                        throws NoSessionException,
                        AbsentInformationException {
        generalStep(thread, StepRequest.STEP_LINE, StepRequest.STEP_INTO);
    }
    public void stepOverLine(ThreadReference thread)
                        throws NoSessionException,
                        AbsentInformationException {
        generalStep(thread, StepRequest.STEP_LINE, StepRequest.STEP_OVER);
    }
    public void stepOut(ThreadReference thread)
                        throws NoSessionException {
        generalStep(thread, StepRequest.STEP_MIN, StepRequest.STEP_OUT);
    }
    public void suspendThread(ThreadReference thread) throws NoSessionException {
        ensureActiveSession();
        thread.suspend();
    }
    public void resumeThread(ThreadReference thread) throws NoSessionException {
        ensureActiveSession();
        thread.resume();
    }
    public void stopThread(ThreadReference thread) throws NoSessionException {
        ensureActiveSession();
    }
    private List<ThreadInfo> threadInfoList = new LinkedList<ThreadInfo>();
    private HashMap<ThreadReference, ThreadInfo> threadInfoMap = new HashMap<ThreadReference, ThreadInfo>();
    public ThreadInfo threadInfo(ThreadReference thread) {
        if (session == null || thread == null) {
            return null;
        }
        ThreadInfo info = threadInfoMap.get(thread);
        if (info == null) {
            info = new ThreadInfo(thread);
            if (session.interrupted) {
                info.validate();
            }
            threadInfoList.add(info);
            threadInfoMap.put(thread, info);
        }
        return info;
    }
     void validateThreadInfo() {
        session.interrupted = true;
        for (ThreadInfo threadInfo : threadInfoList) {
            threadInfo.validate();
            }
    }
    private void invalidateThreadInfo() {
        if (session != null) {
            session.interrupted = false;
            for (ThreadInfo threadInfo : threadInfoList) {
                threadInfo.invalidate();
            }
        }
    }
    void removeThreadInfo(ThreadReference thread) {
        ThreadInfo info = threadInfoMap.get(thread);
        if (info != null) {
            info.invalidate();
            threadInfoMap.remove(thread);
            threadInfoList.remove(info);
        }
    }
    private void notifyInterrupted() {
      ArrayList<SessionListener> l = new ArrayList<SessionListener>(sessionListeners);
        EventObject evt = new EventObject(this);
        for (int i = 0; i < l.size(); i++) {
            l.get(i).sessionInterrupt(evt);
        }
    }
    private void notifyContinued() {
        ArrayList<SessionListener> l = new ArrayList<SessionListener>(sessionListeners);
        EventObject evt = new EventObject(this);
        for (int i = 0; i < l.size(); i++) {
            l.get(i).sessionContinue(evt);
        }
    }
    private void notifySessionStart() {
        ArrayList<SessionListener> l = new ArrayList<SessionListener>(sessionListeners);
        EventObject evt = new EventObject(this);
        for (int i = 0; i < l.size(); i++) {
            l.get(i).sessionStart(evt);
        }
    }
    private void notifySessionDeath() {
    }
    private Object inputLock = new Object();
    private LinkedList<String> inputBuffer = new LinkedList<String>();
    private void resetInputBuffer() {
        synchronized (inputLock) {
            inputBuffer = new LinkedList<String>();
        }
    }
    public void sendLineToApplication(String line) {
        synchronized (inputLock) {
            inputBuffer.addFirst(line);
            inputLock.notifyAll();
        }
    }
    private InputListener appInput = new InputListener() {
        @Override
        public String getLine() {
            String line = null;
            while (line == null) {
                synchronized (inputLock) {
                    try {
                        while (inputBuffer.size() < 1) {
                            inputLock.wait();
                        }
                        line = inputBuffer.removeLast();
                    } catch (InterruptedException e) {}
                }
            }
            final String input = line;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    echoInputLine(input);
                }
            });
            return line;
        }
    };
    private static String newline = System.getProperty("line.separator");
    private void echoInputLine(String line) {
        ArrayList<OutputListener> l = new ArrayList<OutputListener>(appEchoListeners);
        for (int i = 0; i < l.size(); i++) {
            OutputListener ol = l.get(i);
            ol.putString(line);
            ol.putString(newline);
        }
    }
    private OutputListener appOutput = new OutputListener() {
      @Override
        public void putString(String string) {
            ArrayList<OutputListener> l = new ArrayList<OutputListener>(appEchoListeners);
            for (int i = 0; i < l.size(); i++) {
                l.get(i).putString(string);
            }
        }
    };
    private OutputListener appError = new OutputListener() {
      @Override
        public void putString(String string) {
            ArrayList<OutputListener> l = new ArrayList<OutputListener>(appEchoListeners);
            for (int i = 0; i < l.size(); i++) {
                l.get(i).putString(string);
            }
        }
    };
   private OutputListener diagnostics = new OutputListener() {
      @Override
        public void putString(String string) {
            ArrayList<OutputListener> l = new ArrayList<OutputListener>(diagnosticsListeners);
            for (int i = 0; i < l.size(); i++) {
                l.get(i).putString(string);
            }
        }
   };
    private EventRequestSpecList specList = new EventRequestSpecList(this);
    public BreakpointSpec
    createSourceLineBreakpoint(String sourceName, int line) {
        return specList.createSourceLineBreakpoint(sourceName, line);
    }
    public BreakpointSpec
    createClassLineBreakpoint(String classPattern, int line) {
        return specList.createClassLineBreakpoint(classPattern, line);
    }
    public BreakpointSpec
    createMethodBreakpoint(String classPattern,
                           String methodId, List<String> methodArgs) {
        return specList.createMethodBreakpoint(classPattern,
                                                 methodId, methodArgs);
    }
    public ExceptionSpec
    createExceptionIntercept(String classPattern,
                             boolean notifyCaught,
                             boolean notifyUncaught) {
        return specList.createExceptionIntercept(classPattern,
                                                   notifyCaught,
                                                   notifyUncaught);
    }
    public AccessWatchpointSpec
    createAccessWatchpoint(String classPattern, String fieldId) {
        return specList.createAccessWatchpoint(classPattern, fieldId);
    }
    public ModificationWatchpointSpec
    createModificationWatchpoint(String classPattern, String fieldId) {
        return specList.createModificationWatchpoint(classPattern,
                                                       fieldId);
    }
    public void delete(EventRequestSpec spec) {
        specList.delete(spec);
    }
    void resolve(ReferenceType refType) {
        specList.resolve(refType);
    }
    public void install(EventRequestSpec spec) {
        specList.install(spec, vm());
    }
    public List<EventRequestSpec> eventRequestSpecs() {
        return specList.eventRequestSpecs();
    }
}
