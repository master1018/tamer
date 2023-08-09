final class WDropTargetContextPeer extends SunDropTargetContextPeer {
    static WDropTargetContextPeer getWDropTargetContextPeer() {
        return new WDropTargetContextPeer();
    }
    private WDropTargetContextPeer() {
        super();
    }
    private static FileInputStream getFileStream(String file, long stgmedium)
        throws IOException
    {
        return new WDropTargetContextPeerFileStream(file, stgmedium);
    }
    private static Object getIStream(long istream) throws IOException {
        return new WDropTargetContextPeerIStream(istream);
    }
    protected Object getNativeData(long format) {
        return getData(getNativeDragContext(), format);
    }
    protected void doDropDone(boolean success, int dropAction,
                              boolean isLocal) {
        dropDone(getNativeDragContext(), success, dropAction);
    }
    protected void eventPosted(final SunDropTargetEvent e) {
        if (e.getID() != SunDropTargetEvent.MOUSE_DROPPED) {
            Runnable runnable = new Runnable() {
                    public void run() {
                        e.getDispatcher().unregisterAllEvents();
                    }
                };
            PeerEvent peerEvent = new PeerEvent(e.getSource(), runnable, 0);
            SunToolkit.executeOnEventHandlerThread(peerEvent);
        }
    }
     private native Object getData(long nativeContext, long format);
     private native void dropDone(long nativeContext, boolean success, int action);
}
class WDropTargetContextPeerFileStream extends FileInputStream {
    WDropTargetContextPeerFileStream(String name, long stgmedium)
        throws FileNotFoundException
    {
        super(name);
        this.stgmedium  = stgmedium;
    }
    public void close() throws IOException {
        if (stgmedium != 0) {
            super.close();
            freeStgMedium(stgmedium);
            stgmedium = 0;
        }
    }
    private native void freeStgMedium(long stgmedium);
    private long    stgmedium;
}
class WDropTargetContextPeerIStream extends InputStream {
    WDropTargetContextPeerIStream(long istream) throws IOException {
        super();
        if (istream == 0) throw new IOException("No IStream");
        this.istream    = istream;
    }
    public int available() throws IOException {
        if (istream == 0) throw new IOException("No IStream");
        return Available(istream);
    }
    private native int Available(long istream);
    public int read() throws IOException {
        if (istream == 0) throw new IOException("No IStream");
        return Read(istream);
    }
    private native int Read(long istream) throws IOException;
    public int read(byte[] b, int off, int len) throws IOException {
        if (istream == 0) throw new IOException("No IStream");
        return ReadBytes(istream, b, off, len);
    }
    private native int ReadBytes(long istream, byte[] b, int off, int len) throws IOException;
    public void close() throws IOException {
        if (istream != 0) {
            super.close();
            Close(istream);
            istream = 0;
        }
    }
    private native void Close(long istream);
    private long istream;
}
