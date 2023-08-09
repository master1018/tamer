    void classPrepare(ClassPrepareEventSet e);
    void classUnload(ClassUnloadEventSet e);
    void exception(ExceptionEventSet e);
    void locationTrigger(LocationTriggerEventSet e);
    void modificationWatchpoint(ModificationWatchpointEventSet e);
    void threadDeath(ThreadDeathEventSet e);
    void threadStart(ThreadStartEventSet e);
    void vmDeath(VMDeathEventSet e);
    void vmDisconnect(VMDisconnectEventSet e);
    void vmStart(VMStartEventSet e);
}
