public class ServerRuntimeException extends RemoteException {
    private static final long serialVersionUID = 7054464920481467219L;
    @Deprecated
    public ServerRuntimeException(String s, Exception ex) {
        super(s, ex);
    }
}
