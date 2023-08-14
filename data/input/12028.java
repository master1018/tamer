public class SetBufferSize {
    public static void main(String[] args) throws Exception {
        DatagramSocket soc = new DatagramSocket();
        soc.setReceiveBufferSize(Integer.MAX_VALUE);
    }
}
