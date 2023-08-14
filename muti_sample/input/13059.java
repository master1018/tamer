public class BufferManagerReadGrow
    implements BufferManagerRead, MarkAndResetHandler
{
    private ORB orb ;
    private ORBUtilSystemException wrapper ;
    BufferManagerReadGrow( ORB orb )
    {
        this.orb = orb ;
        this.wrapper = ORBUtilSystemException.get( orb,
            CORBALogDomains.RPC_ENCODING ) ;
    }
    public void processFragment (ByteBuffer byteBuffer, FragmentMessage header)
    {
    }
    public void init(Message msg) {}
    public ByteBufferWithInfo underflow (ByteBufferWithInfo bbwi)
    {
        throw wrapper.unexpectedEof() ;
    }
    public void cancelProcessing(int requestId) {}
    private Object streamMemento;
    private RestorableInputStream inputStream;
    private boolean markEngaged = false;
    public MarkAndResetHandler getMarkAndResetHandler() {
        return this;
    }
    public void mark(RestorableInputStream is) {
        markEngaged = true;
        inputStream = is;
        streamMemento = inputStream.createStreamMemento();
    }
    public void fragmentationOccured(ByteBufferWithInfo newFragment) {}
    public void reset() {
        if (!markEngaged)
            return;
        markEngaged = false;
        inputStream.restoreInternalState(streamMemento);
        streamMemento = null;
    }
    public void close(ByteBufferWithInfo bbwi) {}
}
