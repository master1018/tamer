public class StreamRemoteCall implements RemoteCall {
    private ConnectionInputStream in = null;
    private ConnectionOutputStream out = null;
    private Connection conn;
    private boolean resultStarted = false;
    private Exception serverException = null;
    public StreamRemoteCall(Connection c) {
        conn = c;
    }
    public StreamRemoteCall(Connection c, ObjID id, int op, long hash)
        throws RemoteException
    {
        try {
            conn = c;
            Transport.transportLog.log(Log.VERBOSE,
                "write remote call header...");
            conn.getOutputStream().write(TransportConstants.Call);
            getOutputStream();           
            id.write(out);               
            out.writeInt(op);            
            out.writeLong(hash);         
        } catch (IOException e) {
            throw new MarshalException("Error marshaling call header", e);
        }
    }
    public Connection getConnection() {
        return conn;
    }
    public ObjectOutput getOutputStream() throws IOException {
        return getOutputStream(false);
    }
    private ObjectOutput getOutputStream(boolean resultStream)
        throws IOException
    {
        if (out == null) {
            Transport.transportLog.log(Log.VERBOSE, "getting output stream");
            out = new ConnectionOutputStream(conn, resultStream);
        }
        return out;
    }
    public void releaseOutputStream() throws IOException {
        try {
            if (out != null) {
                try {
                    out.flush();
                } finally {
                    out.done();         
                }
            }
            conn.releaseOutputStream();
        } finally {
            out = null;
        }
    }
    public ObjectInput getInputStream() throws IOException {
        if (in == null) {
            Transport.transportLog.log(Log.VERBOSE, "getting input stream");
            in = new ConnectionInputStream(conn.getInputStream());
        }
        return in;
    }
    public void releaseInputStream() throws IOException {
        try {
            if (in != null) {
                try {
                    in.done();
                } catch (RuntimeException e) {
                }
                in.registerRefs();
                in.done(conn);
            }
            conn.releaseInputStream();
        } finally {
            in = null;
        }
    }
    public ObjectOutput getResultStream(boolean success) throws IOException {
        if (resultStarted)
            throw new StreamCorruptedException("result already in progress");
        else
            resultStarted = true;
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeByte(TransportConstants.Return);
        getOutputStream(true);  
        if (success)            
            out.writeByte(TransportConstants.NormalReturn);
        else
            out.writeByte(TransportConstants.ExceptionalReturn);
        out.writeID();          
        return out;
    }
    public void executeCall() throws Exception {
        byte returnType;
        DGCAckHandler ackHandler = null;
        try {
            if (out != null) {
                ackHandler = out.getDGCAckHandler();
            }
            releaseOutputStream();
            DataInputStream rd = new DataInputStream(conn.getInputStream());
            byte op = rd.readByte();
            if (op != TransportConstants.Return) {
                if (Transport.transportLog.isLoggable(Log.BRIEF)) {
                    Transport.transportLog.log(Log.BRIEF,
                        "transport return code invalid: " + op);
                }
                throw new UnmarshalException("Transport return code invalid");
            }
            getInputStream();
            returnType = in.readByte();
            in.readID();        
        } catch (UnmarshalException e) {
            throw e;
        } catch (IOException e) {
            throw new UnmarshalException("Error unmarshaling return header",
                                         e);
        } finally {
            if (ackHandler != null) {
                ackHandler.release();
            }
        }
        switch (returnType) {
        case TransportConstants.NormalReturn:
            break;
        case TransportConstants.ExceptionalReturn:
            Object ex;
            try {
                ex = in.readObject();
            } catch (Exception e) {
                throw new UnmarshalException("Error unmarshaling return", e);
            }
            if (ex instanceof Exception) {
                exceptionReceivedFromServer((Exception) ex);
            } else {
                throw new UnmarshalException("Return type not Exception");
            }
        default:
            if (Transport.transportLog.isLoggable(Log.BRIEF)) {
                Transport.transportLog.log(Log.BRIEF,
                    "return code invalid: " + returnType);
            }
            throw new UnmarshalException("Return code invalid");
        }
    }
    protected void exceptionReceivedFromServer(Exception ex) throws Exception {
        serverException = ex;
        StackTraceElement[] serverTrace = ex.getStackTrace();
        StackTraceElement[] clientTrace = (new Throwable()).getStackTrace();
        StackTraceElement[] combinedTrace =
            new StackTraceElement[serverTrace.length + clientTrace.length];
        System.arraycopy(serverTrace, 0, combinedTrace, 0,
                         serverTrace.length);
        System.arraycopy(clientTrace, 0, combinedTrace, serverTrace.length,
                         clientTrace.length);
        ex.setStackTrace(combinedTrace);
        if (UnicastRef.clientCallLog.isLoggable(Log.BRIEF)) {
            TCPEndpoint ep = (TCPEndpoint) conn.getChannel().getEndpoint();
            UnicastRef.clientCallLog.log(Log.BRIEF, "outbound call " +
                "received exception: [" + ep.getHost() + ":" +
                ep.getPort() + "] exception: ", ex);
        }
        throw ex;
    }
    public Exception getServerException() {
        return serverException;
    }
    public void done() throws IOException {
        releaseInputStream();
    }
}
