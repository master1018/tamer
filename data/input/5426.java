public class SocketOrChannelConnectionImpl
    extends
        EventHandlerBase
    implements
        CorbaConnection,
        Work
{
    public static boolean dprintWriteLocks = false;
    protected long enqueueTime;
    protected SocketChannel socketChannel;
    public SocketChannel getSocketChannel()
    {
        return socketChannel;
    }
    protected CorbaContactInfo contactInfo;
    protected Acceptor acceptor;
    protected ConnectionCache connectionCache;
    protected Socket socket;    
    protected long timeStamp = 0;
    protected boolean isServer = false;
    protected int requestId = 5;
    protected CorbaResponseWaitingRoom responseWaitingRoom;
    protected int state;
    protected java.lang.Object stateEvent = new java.lang.Object();
    protected java.lang.Object writeEvent = new java.lang.Object();
    protected boolean writeLocked;
    protected int serverRequestCount = 0;
    Map serverRequestMap = null;
    protected boolean postInitialContexts = false;
    protected IOR codeBaseServerIOR;
    protected CachedCodeBase cachedCodeBase = new CachedCodeBase(this);
    protected ORBUtilSystemException wrapper ;
    protected ReadTimeouts readTimeouts;
    protected boolean shouldReadGiopHeaderOnly;
    protected CorbaMessageMediator partialMessageMediator = null;
    protected SocketOrChannelConnectionImpl(ORB orb)
    {
        this.orb = orb;
        wrapper = ORBUtilSystemException.get( orb,
            CORBALogDomains.RPC_TRANSPORT ) ;
        setWork(this);
        responseWaitingRoom = new CorbaResponseWaitingRoomImpl(orb, this);
        setReadTimeouts(orb.getORBData().getTransportTCPReadTimeouts());
    }
    protected SocketOrChannelConnectionImpl(ORB orb,
                                            boolean useSelectThreadToWait,
                                            boolean useWorkerThread)
    {
        this(orb) ;
        setUseSelectThreadToWait(useSelectThreadToWait);
        setUseWorkerThreadForEvent(useWorkerThread);
    }
    public SocketOrChannelConnectionImpl(ORB orb,
                                         CorbaContactInfo contactInfo,
                                         boolean useSelectThreadToWait,
                                         boolean useWorkerThread,
                                         String socketType,
                                         String hostname,
                                         int port)
    {
        this(orb, useSelectThreadToWait, useWorkerThread);
        this.contactInfo = contactInfo;
        try {
            socket = orb.getORBData().getSocketFactory()
                .createSocket(socketType,
                              new InetSocketAddress(hostname, port));
            socketChannel = socket.getChannel();
            if (socketChannel != null) {
                boolean isBlocking = !useSelectThreadToWait;
                socketChannel.configureBlocking(isBlocking);
            } else {
                setUseSelectThreadToWait(false);
            }
            if (orb.transportDebugFlag) {
                dprint(".initialize: connection created: " + socket);
            }
        } catch (Throwable t) {
            throw wrapper.connectFailure(t, socketType, hostname,
                                         Integer.toString(port));
        }
        state = OPENING;
    }
    public SocketOrChannelConnectionImpl(ORB orb,
                                         CorbaContactInfo contactInfo,
                                         String socketType,
                                         String hostname,
                                         int port)
    {
        this(orb, contactInfo,
             orb.getORBData().connectionSocketUseSelectThreadToWait(),
             orb.getORBData().connectionSocketUseWorkerThreadForEvent(),
             socketType, hostname, port);
    }
    public SocketOrChannelConnectionImpl(ORB orb,
                                         Acceptor acceptor,
                                         Socket socket,
                                         boolean useSelectThreadToWait,
                                         boolean useWorkerThread)
    {
        this(orb, useSelectThreadToWait, useWorkerThread);
        this.socket = socket;
        socketChannel = socket.getChannel();
        if (socketChannel != null) {
            try {
                boolean isBlocking = !useSelectThreadToWait;
                socketChannel.configureBlocking(isBlocking);
            } catch (IOException e) {
                RuntimeException rte = new RuntimeException();
                rte.initCause(e);
                throw rte;
            }
        }
        this.acceptor = acceptor;
        serverRequestMap = Collections.synchronizedMap(new HashMap());
        isServer = true;
        state = ESTABLISHED;
    }
    public SocketOrChannelConnectionImpl(ORB orb,
                                         Acceptor acceptor,
                                         Socket socket)
    {
        this(orb, acceptor, socket,
             (socket.getChannel() == null
              ? false
              : orb.getORBData().connectionSocketUseSelectThreadToWait()),
             (socket.getChannel() == null
              ? false
              : orb.getORBData().connectionSocketUseWorkerThreadForEvent()));
    }
    public boolean shouldRegisterReadEvent()
    {
        return true;
    }
    public boolean shouldRegisterServerReadEvent()
    {
        return true;
    }
    public boolean read()
    {
        try {
            if (orb.transportDebugFlag) {
                dprint(".read->: " + this);
            }
            CorbaMessageMediator messageMediator = readBits();
            if (messageMediator != null) {
                return dispatch(messageMediator);
            }
            return true;
        } finally {
            if (orb.transportDebugFlag) {
                dprint(".read<-: " + this);
            }
        }
    }
    protected CorbaMessageMediator readBits()
    {
        try {
            if (orb.transportDebugFlag) {
                dprint(".readBits->: " + this);
            }
            MessageMediator messageMediator;
            if (contactInfo != null) {
                messageMediator =
                    contactInfo.createMessageMediator(orb, this);
            } else if (acceptor != null) {
                messageMediator = acceptor.createMessageMediator(orb, this);
            } else {
                throw
                    new RuntimeException("SocketOrChannelConnectionImpl.readBits");
            }
            return (CorbaMessageMediator) messageMediator;
        } catch (ThreadDeath td) {
            if (orb.transportDebugFlag) {
                dprint(".readBits: " + this + ": ThreadDeath: " + td, td);
            }
            try {
                purgeCalls(wrapper.connectionAbort(td), false, false);
            } catch (Throwable t) {
                if (orb.transportDebugFlag) {
                    dprint(".readBits: " + this + ": purgeCalls: Throwable: " + t, t);
                }
            }
            throw td;
        } catch (Throwable ex) {
            if (orb.transportDebugFlag) {
                dprint(".readBits: " + this + ": Throwable: " + ex, ex);
            }
            try {
                if (ex instanceof INTERNAL) {
                    sendMessageError(GIOPVersion.DEFAULT_VERSION);
                }
            } catch (IOException e) {
                if (orb.transportDebugFlag) {
                    dprint(".readBits: " + this +
                           ": sendMessageError: IOException: " + e, e);
                }
            }
            orb.getTransportManager().getSelector(0).unregisterForEvent(this);
            purgeCalls(wrapper.connectionAbort(ex), true, false);
        } finally {
            if (orb.transportDebugFlag) {
                dprint(".readBits<-: " + this);
            }
        }
        return null;
    }
    protected CorbaMessageMediator finishReadingBits(MessageMediator messageMediator)
    {
        try {
            if (orb.transportDebugFlag) {
                dprint(".finishReadingBits->: " + this);
            }
            if (contactInfo != null) {
                messageMediator =
                    contactInfo.finishCreatingMessageMediator(orb, this, messageMediator);
            } else if (acceptor != null) {
                messageMediator =
                    acceptor.finishCreatingMessageMediator(orb, this, messageMediator);
            } else {
                throw
                    new RuntimeException("SocketOrChannelConnectionImpl.finishReadingBits");
            }
            return (CorbaMessageMediator) messageMediator;
        } catch (ThreadDeath td) {
            if (orb.transportDebugFlag) {
                dprint(".finishReadingBits: " + this + ": ThreadDeath: " + td, td);
            }
            try {
                purgeCalls(wrapper.connectionAbort(td), false, false);
            } catch (Throwable t) {
                if (orb.transportDebugFlag) {
                    dprint(".finishReadingBits: " + this + ": purgeCalls: Throwable: " + t, t);
                }
            }
            throw td;
        } catch (Throwable ex) {
            if (orb.transportDebugFlag) {
                dprint(".finishReadingBits: " + this + ": Throwable: " + ex, ex);
            }
            try {
                if (ex instanceof INTERNAL) {
                    sendMessageError(GIOPVersion.DEFAULT_VERSION);
                }
            } catch (IOException e) {
                if (orb.transportDebugFlag) {
                    dprint(".finishReadingBits: " + this +
                           ": sendMessageError: IOException: " + e, e);
                }
            }
            orb.getTransportManager().getSelector(0).unregisterForEvent(this);
            purgeCalls(wrapper.connectionAbort(ex), true, false);
        } finally {
            if (orb.transportDebugFlag) {
                dprint(".finishReadingBits<-: " + this);
            }
        }
        return null;
    }
    protected boolean dispatch(CorbaMessageMediator messageMediator)
    {
        try {
            if (orb.transportDebugFlag) {
                dprint(".dispatch->: " + this);
            }
            boolean result =
                messageMediator.getProtocolHandler()
                .handleRequest(messageMediator);
            return result;
        } catch (ThreadDeath td) {
            if (orb.transportDebugFlag) {
                dprint(".dispatch: ThreadDeath", td );
            }
            try {
                purgeCalls(wrapper.connectionAbort(td), false, false);
            } catch (Throwable t) {
                if (orb.transportDebugFlag) {
                    dprint(".dispatch: purgeCalls: Throwable", t);
                }
            }
            throw td;
        } catch (Throwable ex) {
            if (orb.transportDebugFlag) {
                dprint(".dispatch: Throwable", ex ) ;
            }
            try {
                if (ex instanceof INTERNAL) {
                    sendMessageError(GIOPVersion.DEFAULT_VERSION);
                }
            } catch (IOException e) {
                if (orb.transportDebugFlag) {
                    dprint(".dispatch: sendMessageError: IOException", e);
                }
            }
            purgeCalls(wrapper.connectionAbort(ex), false, false);
        } finally {
            if (orb.transportDebugFlag) {
                dprint(".dispatch<-: " + this);
            }
        }
        return true;
    }
    public boolean shouldUseDirectByteBuffers()
    {
        return getSocketChannel() != null;
    }
    public ByteBuffer read(int size, int offset, int length, long max_wait_time)
        throws IOException
    {
        if (shouldUseDirectByteBuffers()) {
            ByteBuffer byteBuffer =
                orb.getByteBufferPool().getByteBuffer(size);
            if (orb.transportDebugFlag) {
                int bbAddress = System.identityHashCode(byteBuffer);
                StringBuffer sb = new StringBuffer(80);
                sb.append(".read: got ByteBuffer id (");
                sb.append(bbAddress).append(") from ByteBufferPool.");
                String msgStr = sb.toString();
                dprint(msgStr);
            }
            byteBuffer.position(offset);
            byteBuffer.limit(size);
            readFully(byteBuffer, length, max_wait_time);
            return byteBuffer;
        }
        byte[] buf = new byte[size];
        readFully(getSocket().getInputStream(), buf,
                  offset, length, max_wait_time);
        ByteBuffer byteBuffer = ByteBuffer.wrap(buf);
        byteBuffer.limit(size);
        return byteBuffer;
    }
    public ByteBuffer read(ByteBuffer byteBuffer, int offset,
                           int length, long max_wait_time)
        throws IOException
    {
        int size = offset + length;
        if (shouldUseDirectByteBuffers()) {
            if (! byteBuffer.isDirect()) {
                throw wrapper.unexpectedNonDirectByteBufferWithChannelSocket();
            }
            if (size > byteBuffer.capacity()) {
                if (orb.transportDebugFlag) {
                    int bbAddress = System.identityHashCode(byteBuffer);
                    StringBuffer bbsb = new StringBuffer(80);
                    bbsb.append(".read: releasing ByteBuffer id (")
                        .append(bbAddress).append(") to ByteBufferPool.");
                    String bbmsg = bbsb.toString();
                    dprint(bbmsg);
                }
                orb.getByteBufferPool().releaseByteBuffer(byteBuffer);
                byteBuffer = orb.getByteBufferPool().getByteBuffer(size);
            }
            byteBuffer.position(offset);
            byteBuffer.limit(size);
            readFully(byteBuffer, length, max_wait_time);
            byteBuffer.position(0);
            byteBuffer.limit(size);
            return byteBuffer;
        }
        if (byteBuffer.isDirect()) {
            throw wrapper.unexpectedDirectByteBufferWithNonChannelSocket();
        }
        byte[] buf = new byte[size];
        readFully(getSocket().getInputStream(), buf,
                  offset, length, max_wait_time);
        return ByteBuffer.wrap(buf);
    }
    public void readFully(ByteBuffer byteBuffer, int size, long max_wait_time)
        throws IOException
    {
        int n = 0;
        int bytecount = 0;
        long time_to_wait = readTimeouts.get_initial_time_to_wait();
        long total_time_in_wait = 0;
        do {
            bytecount = getSocketChannel().read(byteBuffer);
            if (bytecount < 0) {
                throw new IOException("End-of-stream");
            }
            else if (bytecount == 0) {
                try {
                    Thread.sleep(time_to_wait);
                    total_time_in_wait += time_to_wait;
                    time_to_wait =
                        (long)(time_to_wait*readTimeouts.get_backoff_factor());
                }
                catch (InterruptedException ie) {
                    if (orb.transportDebugFlag) {
                        dprint("readFully(): unexpected exception "
                                + ie.toString());
                    }
                }
            }
            else {
                n += bytecount;
            }
        }
        while (n < size && total_time_in_wait < max_wait_time);
        if (n < size && total_time_in_wait >= max_wait_time)
        {
            throw wrapper.transportReadTimeoutExceeded(new Integer(size),
                                      new Integer(n), new Long(max_wait_time),
                                      new Long(total_time_in_wait));
        }
        getConnectionCache().stampTime(this);
    }
    public void readFully(java.io.InputStream is, byte[] buf,
                          int offset, int size, long max_wait_time)
        throws IOException
    {
        int n = 0;
        int bytecount = 0;
        long time_to_wait = readTimeouts.get_initial_time_to_wait();
        long total_time_in_wait = 0;
        do {
            bytecount = is.read(buf, offset + n, size - n);
            if (bytecount < 0) {
                throw new IOException("End-of-stream");
            }
            else if (bytecount == 0) {
                try {
                    Thread.sleep(time_to_wait);
                    total_time_in_wait += time_to_wait;
                    time_to_wait =
                        (long)(time_to_wait*readTimeouts.get_backoff_factor());
                }
                catch (InterruptedException ie) {
                    if (orb.transportDebugFlag) {
                        dprint("readFully(): unexpected exception "
                                + ie.toString());
                    }
                }
            }
            else {
                n += bytecount;
            }
        }
        while (n < size && total_time_in_wait < max_wait_time);
        if (n < size && total_time_in_wait >= max_wait_time)
        {
            throw wrapper.transportReadTimeoutExceeded(new Integer(size),
                                      new Integer(n), new Long(max_wait_time),
                                      new Long(total_time_in_wait));
        }
        getConnectionCache().stampTime(this);
    }
    public void write(ByteBuffer byteBuffer)
        throws IOException
    {
        if (shouldUseDirectByteBuffers()) {
            do {
                getSocketChannel().write(byteBuffer);
            }
            while (byteBuffer.hasRemaining());
        } else {
            if (! byteBuffer.hasArray()) {
                throw wrapper.unexpectedDirectByteBufferWithNonChannelSocket();
            }
            byte[] tmpBuf = byteBuffer.array();
            getSocket().getOutputStream().write(tmpBuf, 0, byteBuffer.limit());
            getSocket().getOutputStream().flush();
        }
        getConnectionCache().stampTime(this);
    }
    public synchronized void close()
    {
        try {
            if (orb.transportDebugFlag) {
                dprint(".close->: " + this);
            }
            writeLock();
            if (isBusy()) { 
                writeUnlock();
                if (orb.transportDebugFlag) {
                    dprint(".close: isBusy so no close: " + this);
                }
                return;
            }
            try {
                try {
                    sendCloseConnection(GIOPVersion.V1_0);
                } catch (Throwable t) {
                    wrapper.exceptionWhenSendingCloseConnection(t);
                }
                synchronized ( stateEvent ){
                    state = CLOSE_SENT;
                    stateEvent.notifyAll();
                }
                purgeCalls(wrapper.connectionRebind(), false, true);
            } catch (Exception ex) {
                if (orb.transportDebugFlag) {
                    dprint(".close: exception: " + this, ex);
                }
            }
            try {
                Selector selector = orb.getTransportManager().getSelector(0);
                selector.unregisterForEvent(this);
                if (socketChannel != null) {
                    socketChannel.close();
                }
                socket.close();
            } catch (IOException e) {
                if (orb.transportDebugFlag) {
                    dprint(".close: " + this, e);
                }
            }
            closeConnectionResources();
        } finally {
            if (orb.transportDebugFlag) {
                dprint(".close<-: " + this);
            }
        }
    }
    public void closeConnectionResources() {
           if (orb.transportDebugFlag) {
               dprint(".closeConnectionResources->: " + this);
           }
           Selector selector = orb.getTransportManager().getSelector(0);
           selector.unregisterForEvent(this);
           try {
             if (socketChannel != null)
              socketChannel.close() ;
                if (socket != null && !socket.isClosed())
                socket.close() ;
           } catch (IOException e) {
             if (orb.transportDebugFlag) {
                 dprint( ".closeConnectionResources: " + this, e ) ;
             }
           }
           if (orb.transportDebugFlag) {
               dprint(".closeConnectionResources<-: " + this);
           }
     }
    public Acceptor getAcceptor()
    {
        return acceptor;
    }
    public ContactInfo getContactInfo()
    {
        return contactInfo;
    }
    public EventHandler getEventHandler()
    {
        return this;
    }
    public OutputObject createOutputObject(MessageMediator messageMediator)
    {
        throw new RuntimeException("*****SocketOrChannelConnectionImpl.createOutputObject - should not be called.");
    }
    public boolean isServer()
    {
        return isServer;
    }
    public boolean isBusy()
    {
        if (serverRequestCount > 0 ||
            getResponseWaitingRoom().numberRegistered() > 0)
        {
            return true;
        } else {
            return false;
        }
    }
    public long getTimeStamp()
    {
        return timeStamp;
    }
    public void setTimeStamp(long time)
    {
        timeStamp = time;
    }
    public void setState(String stateString)
    {
        synchronized (stateEvent) {
            if (stateString.equals("ESTABLISHED")) {
                state =  ESTABLISHED;
                stateEvent.notifyAll();
            } else {
            }
        }
    }
    public void writeLock()
    {
      try {
        if (dprintWriteLocks && orb.transportDebugFlag) {
            dprint(".writeLock->: " + this);
        }
        while ( true ) {
            int localState = state;
            switch ( localState ) {
            case OPENING:
                synchronized (stateEvent) {
                    if (state != OPENING) {
                        break;
                    }
                    try {
                        stateEvent.wait();
                    } catch (InterruptedException ie) {
                        if (orb.transportDebugFlag) {
                            dprint(".writeLock: OPENING InterruptedException: " + this);
                        }
                    }
                }
                break;
            case ESTABLISHED:
                synchronized (writeEvent) {
                    if (!writeLocked) {
                        writeLocked = true;
                        return;
                    }
                    try {
                        while (state == ESTABLISHED && writeLocked) {
                            writeEvent.wait(100);
                        }
                    } catch (InterruptedException ie) {
                        if (orb.transportDebugFlag) {
                            dprint(".writeLock: ESTABLISHED InterruptedException: " + this);
                        }
                    }
                }
                break;
            case ABORT:
                synchronized ( stateEvent ){
                    if (state != ABORT) {
                        break;
                    }
                    throw wrapper.writeErrorSend() ;
                }
            case CLOSE_RECVD:
                synchronized ( stateEvent ){
                    if (state != CLOSE_RECVD) {
                        break;
                    }
                    throw wrapper.connectionCloseRebind() ;
                }
            default:
                if (orb.transportDebugFlag) {
                    dprint(".writeLock: default: " + this);
                }
                throw new RuntimeException(".writeLock: bad state");
            }
        }
      } finally {
        if (dprintWriteLocks && orb.transportDebugFlag) {
            dprint(".writeLock<-: " + this);
        }
      }
    }
    public void writeUnlock()
    {
        try {
            if (dprintWriteLocks && orb.transportDebugFlag) {
                dprint(".writeUnlock->: " + this);
            }
            synchronized (writeEvent) {
                writeLocked = false;
                writeEvent.notify(); 
            }
        } finally {
            if (dprintWriteLocks && orb.transportDebugFlag) {
                dprint(".writeUnlock<-: " + this);
            }
        }
    }
    public void sendWithoutLock(OutputObject outputObject)
    {
        try {
            CDROutputObject cdrOutputObject = (CDROutputObject) outputObject;
            cdrOutputObject.writeTo(this);
        } catch (IOException e1) {
            SystemException exc = wrapper.writeErrorSend(e1);
            purgeCalls(exc, false, true);
            throw exc;
        }
    }
    public void registerWaiter(MessageMediator messageMediator)
    {
        responseWaitingRoom.registerWaiter(messageMediator);
    }
    public void unregisterWaiter(MessageMediator messageMediator)
    {
        responseWaitingRoom.unregisterWaiter(messageMediator);
    }
    public InputObject waitForResponse(MessageMediator messageMediator)
    {
        return responseWaitingRoom.waitForResponse(messageMediator);
    }
    public void setConnectionCache(ConnectionCache connectionCache)
    {
        this.connectionCache = connectionCache;
    }
    public ConnectionCache getConnectionCache()
    {
        return connectionCache;
    }
    public void setUseSelectThreadToWait(boolean x)
    {
        useSelectThreadToWait = x;
        setReadGiopHeaderOnly(shouldUseSelectThreadToWait());
    }
    public void handleEvent()
    {
        if (orb.transportDebugFlag) {
            dprint(".handleEvent->: " + this);
        }
        getSelectionKey().interestOps(getSelectionKey().interestOps() &
                                      (~ getInterestOps()));
        if (shouldUseWorkerThreadForEvent()) {
            Throwable throwable = null;
            try {
                int poolToUse = 0;
                if (shouldReadGiopHeaderOnly()) {
                    partialMessageMediator = readBits();
                    poolToUse =
                        partialMessageMediator.getThreadPoolToUse();
                }
                if (orb.transportDebugFlag) {
                    dprint(".handleEvent: addWork to pool: " + poolToUse);
                }
                orb.getThreadPoolManager().getThreadPool(poolToUse)
                    .getWorkQueue(0).addWork(getWork());
            } catch (NoSuchThreadPoolException e) {
                throwable = e;
            } catch (NoSuchWorkQueueException e) {
                throwable = e;
            }
            if (throwable != null) {
                if (orb.transportDebugFlag) {
                    dprint(".handleEvent: " + throwable);
                }
                INTERNAL i = new INTERNAL("NoSuchThreadPoolException");
                i.initCause(throwable);
                throw i;
            }
        } else {
            if (orb.transportDebugFlag) {
                dprint(".handleEvent: doWork");
            }
            getWork().doWork();
        }
        if (orb.transportDebugFlag) {
            dprint(".handleEvent<-: " + this);
        }
    }
    public SelectableChannel getChannel()
    {
        return socketChannel;
    }
    public int getInterestOps()
    {
        return SelectionKey.OP_READ;
    }
    public Connection getConnection()
    {
        return this;
    }
    public String getName()
    {
        return this.toString();
    }
    public void doWork()
    {
        try {
            if (orb.transportDebugFlag) {
                dprint(".doWork->: " + this);
            }
            if (!shouldReadGiopHeaderOnly()) {
                read();
            }
            else {
                CorbaMessageMediator messageMediator =
                                         this.getPartialMessageMediator();
                messageMediator = finishReadingBits(messageMediator);
                if (messageMediator != null) {
                    dispatch(messageMediator);
                }
            }
        } catch (Throwable t) {
            if (orb.transportDebugFlag) {
                dprint(".doWork: ignoring Throwable: "
                       + t
                       + " " + this);
            }
        } finally {
            if (orb.transportDebugFlag) {
                dprint(".doWork<-: " + this);
            }
        }
    }
    public void setEnqueueTime(long timeInMillis)
    {
        enqueueTime = timeInMillis;
    }
    public long getEnqueueTime()
    {
        return enqueueTime;
    }
    public boolean shouldReadGiopHeaderOnly() {
        return shouldReadGiopHeaderOnly;
    }
    protected void setReadGiopHeaderOnly(boolean shouldReadHeaderOnly) {
        shouldReadGiopHeaderOnly = shouldReadHeaderOnly;
    }
    public ResponseWaitingRoom getResponseWaitingRoom()
    {
        return responseWaitingRoom;
    }
    public void serverRequestMapPut(int requestId,
                                    CorbaMessageMediator messageMediator)
    {
        serverRequestMap.put(new Integer(requestId), messageMediator);
    }
    public CorbaMessageMediator serverRequestMapGet(int requestId)
    {
        return (CorbaMessageMediator)
            serverRequestMap.get(new Integer(requestId));
    }
    public void serverRequestMapRemove(int requestId)
    {
        serverRequestMap.remove(new Integer(requestId));
    }
    public java.net.Socket getSocket()
    {
        return socket;
    }
    public synchronized void serverRequestProcessingBegins()
    {
        serverRequestCount++;
    }
    public synchronized void serverRequestProcessingEnds()
    {
        serverRequestCount--;
    }
    public synchronized int getNextRequestId()
    {
        return requestId++;
    }
    protected CodeSetComponentInfo.CodeSetContext codeSetContext = null;
    public ORB getBroker()
    {
        return orb;
    }
    public CodeSetComponentInfo.CodeSetContext getCodeSetContext() {
        if (codeSetContext == null) {
            synchronized(this) {
                return codeSetContext;
            }
        }
        return codeSetContext;
    }
    public synchronized void setCodeSetContext(CodeSetComponentInfo.CodeSetContext csc) {
        if (codeSetContext == null) {
            if (OSFCodeSetRegistry.lookupEntry(csc.getCharCodeSet()) == null ||
                OSFCodeSetRegistry.lookupEntry(csc.getWCharCodeSet()) == null) {
                throw wrapper.badCodesetsFromClient() ;
            }
            codeSetContext = csc;
        }
    }
    public MessageMediator clientRequestMapGet(int requestId)
    {
        return responseWaitingRoom.getMessageMediator(requestId);
    }
    protected MessageMediator clientReply_1_1;
    public void clientReply_1_1_Put(MessageMediator x)
    {
        clientReply_1_1 = x;
    }
    public MessageMediator clientReply_1_1_Get()
    {
        return  clientReply_1_1;
    }
    public void clientReply_1_1_Remove()
    {
        clientReply_1_1 = null;
    }
    protected MessageMediator serverRequest_1_1;
    public void serverRequest_1_1_Put(MessageMediator x)
    {
        serverRequest_1_1 = x;
    }
    public MessageMediator serverRequest_1_1_Get()
    {
        return  serverRequest_1_1;
    }
    public void serverRequest_1_1_Remove()
    {
        serverRequest_1_1 = null;
    }
    protected String getStateString( int state )
    {
        synchronized ( stateEvent ){
            switch (state) {
            case OPENING : return "OPENING" ;
            case ESTABLISHED : return "ESTABLISHED" ;
            case CLOSE_SENT : return "CLOSE_SENT" ;
            case CLOSE_RECVD : return "CLOSE_RECVD" ;
            case ABORT : return "ABORT" ;
            default : return "???" ;
            }
        }
    }
    public synchronized boolean isPostInitialContexts() {
        return postInitialContexts;
    }
    public synchronized void setPostInitialContexts(){
        postInitialContexts = true;
    }
    public void purgeCalls(SystemException systemException,
                           boolean die, boolean lockHeld)
    {
        int minor_code = systemException.minor;
        try{
            if (orb.transportDebugFlag) {
                dprint(".purgeCalls->: "
                       + minor_code + "/" + die + "/" + lockHeld
                       + " " + this);
            }
            synchronized ( stateEvent ){
                if ((state == ABORT) || (state == CLOSE_RECVD)) {
                    if (orb.transportDebugFlag) {
                        dprint(".purgeCalls: exiting since state is: "
                               + getStateString(state)
                               + " " + this);
                    }
                    return;
                }
            }
            try {
                if (!lockHeld) {
                    writeLock();
                }
            } catch (SystemException ex) {
                if (orb.transportDebugFlag)
                    dprint(".purgeCalls: SystemException" + ex
                           + "; continuing " + this);
            }
            org.omg.CORBA.CompletionStatus completion_status;
            synchronized ( stateEvent ){
                if (minor_code == ORBUtilSystemException.CONNECTION_REBIND) {
                    state = CLOSE_RECVD;
                    systemException.completed = CompletionStatus.COMPLETED_NO;
                } else {
                    state = ABORT;
                    systemException.completed = CompletionStatus.COMPLETED_MAYBE;
                }
                stateEvent.notifyAll();
            }
            try {
                socket.getInputStream().close();
                socket.getOutputStream().close();
                socket.close();
            } catch (Exception ex) {
                if (orb.transportDebugFlag) {
                    dprint(".purgeCalls: Exception closing socket: " + ex
                           + " " + this);
                }
            }
            responseWaitingRoom.signalExceptionToAllWaiters(systemException);
            if (contactInfo != null) {
                ((OutboundConnectionCache)getConnectionCache()).remove(contactInfo);
            } else if (acceptor != null) {
                ((InboundConnectionCache)getConnectionCache()).remove(this);
            }
            writeUnlock();
        } finally {
            if (orb.transportDebugFlag) {
                dprint(".purgeCalls<-: "
                       + minor_code + "/" + die + "/" + lockHeld
                       + " " + this);
            }
        }
    }
    public void sendCloseConnection(GIOPVersion giopVersion)
        throws IOException
    {
        Message msg = MessageBase.createCloseConnection(giopVersion);
        sendHelper(giopVersion, msg);
    }
    public void sendMessageError(GIOPVersion giopVersion)
        throws IOException
    {
        Message msg = MessageBase.createMessageError(giopVersion);
        sendHelper(giopVersion, msg);
    }
    public void sendCancelRequest(GIOPVersion giopVersion, int requestId)
        throws IOException
    {
        Message msg = MessageBase.createCancelRequest(giopVersion, requestId);
        sendHelper(giopVersion, msg);
    }
    protected void sendHelper(GIOPVersion giopVersion, Message msg)
        throws IOException
    {
        CDROutputObject outputObject =
            new CDROutputObject((ORB)orb, null, giopVersion, this, msg,
                                ORBConstants.STREAM_FORMAT_VERSION_1);
        msg.write(outputObject);
        outputObject.writeTo(this);
    }
    public void sendCancelRequestWithLock(GIOPVersion giopVersion,
                                          int requestId)
        throws IOException
    {
        writeLock();
        try {
            sendCancelRequest(giopVersion, requestId);
        } finally {
            writeUnlock();
        }
    }
    public final void setCodeBaseIOR(IOR ior) {
        codeBaseServerIOR = ior;
    }
    public final IOR getCodeBaseIOR() {
        return codeBaseServerIOR;
    }
    public final CodeBase getCodeBase() {
        return cachedCodeBase;
    }
    protected void setReadTimeouts(ReadTimeouts readTimeouts) {
        this.readTimeouts = readTimeouts;
    }
    protected void setPartialMessageMediator(CorbaMessageMediator messageMediator) {
        partialMessageMediator = messageMediator;
    }
    protected CorbaMessageMediator getPartialMessageMediator() {
        return partialMessageMediator;
    }
    public String toString()
    {
        synchronized ( stateEvent ){
            return
                "SocketOrChannelConnectionImpl[" + " "
                + (socketChannel == null ?
                   socket.toString() : socketChannel.toString()) + " "
                + getStateString( state ) + " "
                + shouldUseSelectThreadToWait() + " "
                + shouldUseWorkerThreadForEvent() + " "
                + shouldReadGiopHeaderOnly()
                + "]" ;
        }
    }
    public void dprint(String msg)
    {
        ORBUtility.dprint("SocketOrChannelConnectionImpl", msg);
    }
    protected void dprint(String msg, Throwable t)
    {
        dprint(msg);
        t.printStackTrace(System.out);
    }
}
