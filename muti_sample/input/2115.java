public class ThrowingNameService implements NameService {
    static boolean firstCall = true;
    @Override
    public InetAddress[] lookupAllHostAddr(String host) throws UnknownHostException {
        if (firstCall) {
            firstCall = false;
            throw new IllegalStateException();
        }
        return new InetAddress[] { InetAddress.getLoopbackAddress() };
    }
    @Override
    public String getHostByAddr(byte[] addr) throws UnknownHostException {
        throw new IllegalStateException();
    }
}
