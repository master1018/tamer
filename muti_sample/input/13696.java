public class MyDatagramSocketImplFactory implements DatagramSocketImplFactory {
  public DatagramSocketImpl createDatagramSocketImpl() {
    try {
        return DefaultDatagramSocketImplFactory.createDatagramSocketImpl(false);
    } catch (SocketException se) {
        assert false;
    }
    return null;
  }
}
