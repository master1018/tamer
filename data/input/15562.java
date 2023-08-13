    void classPrepareEvent(ClassPrepareEvent e);
    void classUnloadEvent(ClassUnloadEvent e);
    void breakpointEvent(BreakpointEvent e);
    void fieldWatchEvent(WatchpointEvent e);
    void stepEvent(StepEvent e);
    void exceptionEvent(ExceptionEvent e);
    void methodEntryEvent(MethodEntryEvent e);
    boolean methodExitEvent(MethodExitEvent e);
    void vmInterrupted();
    void receivedEvent(Event event);
}
