public class ContextManager {
    private ClassManager classManager;
    private ExecutionManager runtime;
    private String mainClassName;
    private String vmArguments;
    private String commandArguments;
    private String remotePort;
    private ThreadReference currentThread;
    private boolean verbose;
    private ArrayList<ContextListener> contextListeners = new ArrayList<ContextListener>();
    public ContextManager(Environment env) {
        classManager = env.getClassManager();
        runtime = env.getExecutionManager();
        mainClassName = "";
        vmArguments = "";
        commandArguments = "";
        currentThread = null;
        ContextManagerListener listener = new ContextManagerListener();
        runtime.addJDIListener(listener);
        runtime.addSessionListener(listener);
    }
    public String getMainClassName() {
        return mainClassName;
    }
    public void setMainClassName(String mainClassName) {
        this.mainClassName = mainClassName;
    }
    public String getVmArguments() {
        return processClasspathDefaults(vmArguments);
    }
    public void setVmArguments(String vmArguments) {
        this.vmArguments = vmArguments;
    }
    public String getProgramArguments() {
        return commandArguments;
    }
    public void setProgramArguments(String commandArguments) {
        this.commandArguments = commandArguments;
    }
    public String getRemotePort() {
        return remotePort;
    }
    public void setRemotePort(String remotePort) {
        this.remotePort = remotePort;
    }
    public boolean getVerboseFlag() {
        return verbose;
    }
    public void setVerboseFlag(boolean verbose) {
        this.verbose = verbose;
    }
    public ThreadReference getCurrentThread() {
        return currentThread;
    }
    public void setCurrentThread(ThreadReference t) {
        if (t != currentThread) {
            currentThread = t;
            notifyCurrentThreadChanged(t);
        }
    }
    public void setCurrentThreadInvalidate(ThreadReference t) {
        currentThread = t;
        notifyCurrentFrameChanged(runtime.threadInfo(t),
                                  0, true);
    }
    public void invalidateCurrentThread() {
        notifyCurrentFrameChanged(null, 0, true);
    }
    public int getCurrentFrameIndex(ThreadReference t) {
        return getCurrentFrameIndex(runtime.threadInfo(t));
    }
    public int getCurrentFrameIndex(ThreadInfo tinfo) {
        if (tinfo == null) {
            return 0;
        }
        Integer currentFrame = (Integer)tinfo.getUserObject();
        if (currentFrame == null) {
            return 0;
        } else {
            return currentFrame.intValue();
        }
    }
    public int moveCurrentFrameIndex(ThreadReference t, int count) throws VMNotInterruptedException {
        return setCurrentFrameIndex(t,count, true);
    }
    public int setCurrentFrameIndex(ThreadReference t, int newIndex) throws VMNotInterruptedException {
        return setCurrentFrameIndex(t, newIndex, false);
    }
    public int setCurrentFrameIndex(int newIndex) throws VMNotInterruptedException {
        if (currentThread == null) {
            return 0;
        } else {
            return setCurrentFrameIndex(currentThread, newIndex, false);
        }
    }
    private int setCurrentFrameIndex(ThreadReference t, int x, boolean relative) throws VMNotInterruptedException {
        boolean sameThread = t.equals(currentThread);
        ThreadInfo tinfo = runtime.threadInfo(t);
        if (tinfo == null) {
            return 0;
        }
        int maxIndex = tinfo.getFrameCount()-1;
        int oldIndex = getCurrentFrameIndex(tinfo);
        int newIndex = relative? oldIndex + x : x;
        if (newIndex > maxIndex) {
            newIndex = maxIndex;
        } else  if (newIndex < 0) {
            newIndex = 0;
        }
        if (!sameThread || newIndex != oldIndex) {  
            setCurrentFrameIndex(tinfo, newIndex);
        }
        return newIndex - oldIndex;
    }
    private void setCurrentFrameIndex(ThreadInfo tinfo, int index) {
        tinfo.setUserObject(new Integer(index));
        notifyCurrentFrameChanged(tinfo.thread(), index);
    }
    public StackFrame getCurrentFrame() throws VMNotInterruptedException {
        return getCurrentFrame(runtime.threadInfo(currentThread));
    }
    public StackFrame getCurrentFrame(ThreadReference t) throws VMNotInterruptedException {
        return getCurrentFrame(runtime.threadInfo(t));
    }
    public StackFrame getCurrentFrame(ThreadInfo tinfo) throws VMNotInterruptedException {
        int index = getCurrentFrameIndex(tinfo);
        try {
            return tinfo.getFrame(index);
        } catch (FrameIndexOutOfBoundsException e) {
            return null;
        }
    }
    public void addContextListener(ContextListener cl) {
        contextListeners.add(cl);
    }
    public void removeContextListener(ContextListener cl) {
        contextListeners.remove(cl);
    }
    private void notifyCurrentThreadChanged(ThreadReference t) {
        ThreadInfo tinfo = null;
        int index = 0;
        if (t != null) {
            tinfo = runtime.threadInfo(t);
            index = getCurrentFrameIndex(tinfo);
        }
        notifyCurrentFrameChanged(tinfo, index, false);
    }
    private void notifyCurrentFrameChanged(ThreadReference t, int index) {
        notifyCurrentFrameChanged(runtime.threadInfo(t),
                                  index, false);
    }
    private void notifyCurrentFrameChanged(ThreadInfo tinfo, int index,
                                           boolean invalidate) {
        ArrayList<ContextListener> l =  new ArrayList<ContextListener>(contextListeners);
        CurrentFrameChangedEvent evt =
            new CurrentFrameChangedEvent(this, tinfo, index, invalidate);
        for (int i = 0; i < l.size(); i++) {
            l.get(i).currentFrameChanged(evt);
        }
    }
    private class ContextManagerListener extends JDIAdapter
                       implements SessionListener, JDIListener {
        @Override
        public void sessionStart(EventObject e) {
            invalidateCurrentThread();
        }
        @Override
        public void sessionInterrupt(EventObject e) {
            setCurrentThreadInvalidate(currentThread);
        }
        @Override
        public void sessionContinue(EventObject e) {
            invalidateCurrentThread();
        }
        @Override
        public void locationTrigger(LocationTriggerEventSet e) {
            setCurrentThreadInvalidate(e.getThread());
        }
        @Override
        public void exception(ExceptionEventSet e) {
            setCurrentThreadInvalidate(e.getThread());
        }
        @Override
        public void vmDisconnect(VMDisconnectEventSet e) {
            invalidateCurrentThread();
        }
    }
    private String processClasspathDefaults(String javaArgs) {
        if (javaArgs.indexOf("-classpath ") == -1) {
            StringBuffer munged = new StringBuffer(javaArgs);
            SearchPath classpath = classManager.getClassPath();
            if (classpath.isEmpty()) {
                String envcp = System.getProperty("env.class.path");
                if ((envcp != null) && (envcp.length() > 0)) {
                    munged.append(" -classpath " + envcp);
                }
            } else {
                munged.append(" -classpath " + classpath.asString());
            }
            return munged.toString();
        } else {
            return javaArgs;
        }
    }
    private String appendPath(String path1, String path2) {
        if (path1 == null || path1.length() == 0) {
            return path2 == null ? "." : path2;
        } else if (path2 == null || path2.length() == 0) {
            return path1;
        } else {
            return path1  + File.pathSeparator + path2;
        }
    }
}
