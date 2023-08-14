public class CommandTool extends JPanel {
    private static final long serialVersionUID = 8613516856378346415L;
    private Environment env;
    private ContextManager context;
    private ExecutionManager runtime;
    private SourceManager sourceManager;
    private TypeScript script;
    private static final String DEFAULT_CMD_PROMPT = "Command:";
    public CommandTool(Environment env) {
        super(new BorderLayout());
        this.env = env;
        this.context = env.getContextManager();
        this.runtime = env.getExecutionManager();
        this.sourceManager = env.getSourceManager();
        script = new TypeScript(DEFAULT_CMD_PROMPT, false); 
        this.add(script);
        final CommandInterpreter interpreter =
            new CommandInterpreter(env);
        script.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                interpreter.executeCommand(script.readln());
            }
        });
        OutputListener diagnosticsListener =
            new TypeScriptOutputListener(script, true);
        runtime.addDiagnosticsListener(diagnosticsListener);
        env.setTypeScript(new PrintWriter(new TypeScriptWriter(script)));
        TTYDebugListener listener = new TTYDebugListener(diagnosticsListener);
        runtime.addJDIListener(listener);
        runtime.addSessionListener(listener);
        runtime.addSpecListener(listener);
        context.addContextListener(listener);
    }
    private class TTYDebugListener implements
            JDIListener, SessionListener, SpecListener, ContextListener {
        private OutputListener diagnostics;
        TTYDebugListener(OutputListener diagnostics) {
            this.diagnostics = diagnostics;
        }
        @Override
        public void accessWatchpoint(AccessWatchpointEventSet e) {
            setThread(e);
            for (EventIterator it = e.eventIterator(); it.hasNext(); ) {
                it.nextEvent();
                diagnostics.putString("Watchpoint hit: " +
                                      locationString(e));
            }
        }
        @Override
        public void classPrepare(ClassPrepareEventSet e) {
            if (context.getVerboseFlag()) {
                String name = e.getReferenceType().name();
                diagnostics.putString("Class " + name + " loaded");
            }
        }
        @Override
        public void classUnload(ClassUnloadEventSet e) {
            if (context.getVerboseFlag()) {
                diagnostics.putString("Class " + e.getClassName() +
                                      " unloaded.");
            }
        }
        @Override
        public void exception(ExceptionEventSet e) {
            setThread(e);
            String name = e.getException().referenceType().name();
            diagnostics.putString("Exception: " + name);
        }
        @Override
        public void locationTrigger(LocationTriggerEventSet e) {
            String locString = locationString(e);
            setThread(e);
            for (EventIterator it = e.eventIterator(); it.hasNext(); ) {
                Event evt = it.nextEvent();
                if (evt instanceof BreakpointEvent) {
                    diagnostics.putString("Breakpoint hit: " + locString);
                } else if (evt instanceof StepEvent) {
                    diagnostics.putString("Step completed: " + locString);
                } else if (evt instanceof MethodEntryEvent) {
                    diagnostics.putString("Method entered: " + locString);
                } else if (evt instanceof MethodExitEvent) {
                    diagnostics.putString("Method exited: " + locString);
                } else {
                    diagnostics.putString("UNKNOWN event: " + e);
                }
            }
        }
        @Override
        public void modificationWatchpoint(ModificationWatchpointEventSet e) {
            setThread(e);
            for (EventIterator it = e.eventIterator(); it.hasNext(); ) {
                it.nextEvent();
                diagnostics.putString("Watchpoint hit: " +
                                      locationString(e));
            }
        }
        @Override
        public void threadDeath(ThreadDeathEventSet e) {
            if (context.getVerboseFlag()) {
                diagnostics.putString("Thread " + e.getThread() +
                                      " ended.");
            }
        }
        @Override
        public void threadStart(ThreadStartEventSet e) {
            if (context.getVerboseFlag()) {
                diagnostics.putString("Thread " + e.getThread() +
                                      " started.");
            }
        }
        @Override
        public void vmDeath(VMDeathEventSet e) {
            script.setPrompt(DEFAULT_CMD_PROMPT);
            diagnostics.putString("VM exited");
        }
        @Override
        public void vmDisconnect(VMDisconnectEventSet e) {
            script.setPrompt(DEFAULT_CMD_PROMPT);
            diagnostics.putString("Disconnected from VM");
        }
        @Override
        public void vmStart(VMStartEventSet e) {
            script.setPrompt(DEFAULT_CMD_PROMPT);
            diagnostics.putString("VM started");
        }
        @Override
        public void sessionStart(EventObject e) {}
        @Override
        public void sessionInterrupt(EventObject e) {
            Thread.yield();  
            diagnostics.putString("VM interrupted by user.");
            script.setPrompt(DEFAULT_CMD_PROMPT);
        }
        @Override
        public void sessionContinue(EventObject e) {
            diagnostics.putString("Execution resumed.");
            script.setPrompt(DEFAULT_CMD_PROMPT);
        }
        @Override
        public void breakpointSet(SpecEvent e) {
            EventRequestSpec spec = e.getEventRequestSpec();
            diagnostics.putString("Breakpoint set at " + spec + ".");
        }
        @Override
        public void breakpointDeferred(SpecEvent e) {
            EventRequestSpec spec = e.getEventRequestSpec();
            diagnostics.putString("Breakpoint will be set at " +
                                  spec + " when its class is loaded.");
        }
        @Override
        public void breakpointDeleted(SpecEvent e) {
            EventRequestSpec spec = e.getEventRequestSpec();
            diagnostics.putString("Breakpoint at " + spec.toString() + " deleted.");
        }
        @Override
        public void breakpointResolved(SpecEvent e) {
            EventRequestSpec spec = e.getEventRequestSpec();
            diagnostics.putString("Breakpoint resolved to " + spec.toString() + ".");
        }
        @Override
        public void breakpointError(SpecErrorEvent e) {
            EventRequestSpec spec = e.getEventRequestSpec();
            diagnostics.putString("Deferred breakpoint at " +
                                  spec + " could not be resolved:" +
                                  e.getReason());
        }
        @Override
        public void watchpointSet(SpecEvent e) {
        }
        @Override
        public void watchpointDeferred(SpecEvent e) {
        }
        @Override
        public void watchpointDeleted(SpecEvent e) {
        }
        @Override
        public void watchpointResolved(SpecEvent e) {
        }
        @Override
        public void watchpointError(SpecErrorEvent e) {
        }
        @Override
        public void exceptionInterceptSet(SpecEvent e) {
        }
        @Override
        public void exceptionInterceptDeferred(SpecEvent e) {
        }
        @Override
        public void exceptionInterceptDeleted(SpecEvent e) {
        }
        @Override
        public void exceptionInterceptResolved(SpecEvent e) {
        }
        @Override
        public void exceptionInterceptError(SpecErrorEvent e) {
        }
        @Override
        public void currentFrameChanged(CurrentFrameChangedEvent e) {
            ThreadReference thread = e.getThread();
            if (thread == context.getCurrentThread()) {
                script.setPrompt(promptString(thread, e.getIndex()));
            }
        }
    }
    private String locationString(LocatableEventSet e) {
        Location loc = e.getLocation();
        return "thread=\"" + e.getThread().name() +
            "\", " + Utils.locationString(loc);
    }
    private void setThread(LocatableEventSet e) {
        if (!e.suspendedNone()) {
            Thread.yield();  
            script.setPrompt(promptString(e.getThread(), 0));
        }
    }
    private String promptString(ThreadReference thread, int frameIndex) {
        if (thread == null) {
            return DEFAULT_CMD_PROMPT;
        } else {
            return (thread.name() + "[" + (frameIndex + 1) + "]:");
        }
    }
}
