public class InternalEventHandler implements Runnable
{
    EventQueueImpl queue;
    VirtualMachineImpl vm;
    InternalEventHandler(VirtualMachineImpl vm, EventQueueImpl queue)
    {
        this.vm = vm;
        this.queue = queue;
        Thread thread = new Thread(vm.threadGroupForJDI(), this,
                                   "JDI Internal Event Handler");
        thread.setDaemon(true);
        thread.start();
    }
    public void run() {
        if ((vm.traceFlags & VirtualMachine.TRACE_EVENTS) != 0) {
            vm.printTrace("Internal event handler running");
        }
        try {
            while (true) {
                try {
                    EventSet eventSet = queue.removeInternal();
                    EventIterator it = eventSet.eventIterator();
                    while (it.hasNext()) {
                        Event event = it.nextEvent();
                        if (event instanceof ClassUnloadEvent) {
                            ClassUnloadEvent cuEvent = (ClassUnloadEvent)event;
                            vm.removeReferenceType(cuEvent.classSignature());
                            if ((vm.traceFlags & vm.TRACE_EVENTS) != 0) {
                                vm.printTrace("Handled Unload Event for " +
                                              cuEvent.classSignature());
                            }
                        } else if (event instanceof ClassPrepareEvent) {
                            ClassPrepareEvent cpEvent = (ClassPrepareEvent)event;
                            ((ReferenceTypeImpl)cpEvent.referenceType())
                                                            .markPrepared();
                            if ((vm.traceFlags & vm.TRACE_EVENTS) != 0) {
                                vm.printTrace("Handled Prepare Event for " +
                                              cpEvent.referenceType().name());
                            }
                        }
                    }
                } catch (VMOutOfMemoryException vmme) {
                    vmme.printStackTrace();
                } catch (InconsistentDebugInfoException idie) {
                    idie.printStackTrace();
                } catch (ObjectCollectedException oce) {
                    oce.printStackTrace();
                } catch (ClassNotPreparedException cnpe) {
                    cnpe.printStackTrace();
                }
            }
        } catch (InterruptedException e) {  
        } catch (VMDisconnectedException e) {  
        }
        if ((vm.traceFlags & VirtualMachine.TRACE_EVENTS) != 0) {
            vm.printTrace("Internal event handler exiting");
        }
    }
}
