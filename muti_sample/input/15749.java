    List<ClassPrepareRequest> classPrepareRequests();
    List<ClassUnloadRequest> classUnloadRequests();
    List<ThreadStartRequest> threadStartRequests();
    List<ThreadDeathRequest> threadDeathRequests();
    List<ExceptionRequest> exceptionRequests();
    List<BreakpointRequest> breakpointRequests();
    List<AccessWatchpointRequest> accessWatchpointRequests();
    List<ModificationWatchpointRequest> modificationWatchpointRequests();
    List<MethodEntryRequest> methodEntryRequests();
    List<MethodExitRequest> methodExitRequests();
    List<MonitorContendedEnterRequest> monitorContendedEnterRequests();
    List<MonitorContendedEnteredRequest> monitorContendedEnteredRequests();
    List<MonitorWaitRequest> monitorWaitRequests();
    List<MonitorWaitedRequest> monitorWaitedRequests();
    List<VMDeathRequest> vmDeathRequests();
}
