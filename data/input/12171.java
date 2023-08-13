public class CorbaResponseWaitingRoomImpl
    implements
        CorbaResponseWaitingRoom
{
    final static class OutCallDesc
    {
        java.lang.Object done = new java.lang.Object();
        Thread thread;
        MessageMediator messageMediator;
        SystemException exception;
        InputObject inputObject;
    }
    private ORB orb;
    private ORBUtilSystemException wrapper ;
    private CorbaConnection connection;
    private Hashtable out_calls = null; 
    public CorbaResponseWaitingRoomImpl(ORB orb, CorbaConnection connection)
    {
        this.orb = orb;
        wrapper = ORBUtilSystemException.get( orb,
            CORBALogDomains.RPC_TRANSPORT ) ;
        this.connection = connection;
        out_calls = new Hashtable();
    }
    public void registerWaiter(MessageMediator mediator)
    {
        CorbaMessageMediator messageMediator = (CorbaMessageMediator) mediator;
        if (orb.transportDebugFlag) {
            dprint(".registerWaiter: " + opAndId(messageMediator));
        }
        Integer requestId = messageMediator.getRequestIdInteger();
        OutCallDesc call = new OutCallDesc();
        call.thread = Thread.currentThread();
        call.messageMediator = messageMediator;
        out_calls.put(requestId, call);
    }
    public void unregisterWaiter(MessageMediator mediator)
    {
        CorbaMessageMediator messageMediator = (CorbaMessageMediator) mediator;
        if (orb.transportDebugFlag) {
            dprint(".unregisterWaiter: " + opAndId(messageMediator));
        }
        Integer requestId = messageMediator.getRequestIdInteger();
        out_calls.remove(requestId);
    }
    public InputObject waitForResponse(MessageMediator mediator)
    {
      CorbaMessageMediator messageMediator = (CorbaMessageMediator) mediator;
      try {
        InputObject returnStream = null;
        if (orb.transportDebugFlag) {
            dprint(".waitForResponse->: " + opAndId(messageMediator));
        }
        Integer requestId = messageMediator.getRequestIdInteger();
        if (messageMediator.isOneWay()) {
            if (orb.transportDebugFlag) {
                dprint(".waitForResponse: one way - not waiting: "
                       + opAndId(messageMediator));
            }
            return null;
        }
        OutCallDesc call = (OutCallDesc)out_calls.get(requestId);
        if (call == null) {
            throw wrapper.nullOutCall(CompletionStatus.COMPLETED_MAYBE);
        }
        synchronized(call.done) {
            while (call.inputObject == null && call.exception == null) {
                try {
                    if (orb.transportDebugFlag) {
                        dprint(".waitForResponse: waiting: "
                               + opAndId(messageMediator));
                    }
                    call.done.wait();
                } catch (InterruptedException ie) {};
            }
            if (call.exception != null) {
                if (orb.transportDebugFlag) {
                    dprint(".waitForResponse: exception: "
                           + opAndId(messageMediator));
                }
                throw call.exception;
            }
            returnStream = call.inputObject;
        }
        if (returnStream != null) {
            ((CDRInputObject)returnStream).unmarshalHeader();
        }
        return returnStream;
      } finally {
        if (orb.transportDebugFlag) {
            dprint(".waitForResponse<-: " + opAndId(messageMediator));
        }
      }
    }
    public void responseReceived(InputObject is)
    {
        CDRInputObject inputObject = (CDRInputObject) is;
        LocateReplyOrReplyMessage header = (LocateReplyOrReplyMessage)
            inputObject.getMessageHeader();
        Integer requestId = new Integer(header.getRequestId());
        OutCallDesc call = (OutCallDesc) out_calls.get(requestId);
        if (orb.transportDebugFlag) {
            dprint(".responseReceived: id/"
                   + requestId  + ": "
                   + header);
        }
        if (call == null) {
            if (orb.transportDebugFlag) {
                dprint(".responseReceived: id/"
                       + requestId
                       + ": no waiter: "
                       + header);
            }
            return;
        }
        synchronized (call.done) {
            CorbaMessageMediator messageMediator = (CorbaMessageMediator)
                call.messageMediator;
            if (orb.transportDebugFlag) {
                dprint(".responseReceived: "
                       + opAndId(messageMediator)
                       + ": notifying waiters");
            }
            messageMediator.setReplyHeader(header);
            messageMediator.setInputObject(is);
            inputObject.setMessageMediator(messageMediator);
            call.inputObject = is;
            call.done.notify();
        }
    }
    public int numberRegistered()
    {
        return out_calls.size();
    }
    public void signalExceptionToAllWaiters(SystemException systemException)
    {
        if (orb.transportDebugFlag) {
            dprint(".signalExceptionToAllWaiters: " + systemException);
        }
        OutCallDesc call;
        java.util.Enumeration e = out_calls.elements();
        while(e.hasMoreElements()) {
            call = (OutCallDesc) e.nextElement();
            synchronized(call.done){
                CorbaMessageMediator corbaMsgMediator =
                             (CorbaMessageMediator)call.messageMediator;
                CDRInputObject inputObject =
                           (CDRInputObject)corbaMsgMediator.getInputObject();
                if (inputObject != null) {
                    BufferManagerReadStream bufferManager =
                        (BufferManagerReadStream)inputObject.getBufferManager();
                    int requestId = corbaMsgMediator.getRequestId();
                    bufferManager.cancelProcessing(requestId);
                }
                call.inputObject = null;
                call.exception = systemException;
                call.done.notify();
            }
        }
    }
    public MessageMediator getMessageMediator(int requestId)
    {
        Integer id = new Integer(requestId);
        OutCallDesc call = (OutCallDesc) out_calls.get(id);
        if (call == null) {
            return null;
        }
        return call.messageMediator;
    }
    protected void dprint(String msg)
    {
        ORBUtility.dprint("CorbaResponseWaitingRoomImpl", msg);
    }
    protected String opAndId(CorbaMessageMediator mediator)
    {
        return ORBUtility.operationNameAndRequestId(mediator);
    }
}
