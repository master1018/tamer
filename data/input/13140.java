    void classPrepared(ClassPrepareEvent event);
    void classUnloaded(ClassUnloadEvent event);
    void methodEntered(MethodEntryEvent event);
    void methodExited(MethodExitEvent event);
    void fieldAccessed(AccessWatchpointEvent event);
    void fieldModified(ModificationWatchpointEvent event);
    void threadStarted(ThreadStartEvent event);
    void threadDied(ThreadDeathEvent event);
    void vmStarted(VMStartEvent event);
    void vmDied(VMDeathEvent event);
    void vmDisconnected(VMDisconnectEvent event);
}
