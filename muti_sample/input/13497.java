public class ConfigureBlocking {
    public static void main (String [] str) throws Exception {
        SelectableChannel [] channels = null;
        channels = new SelectableChannel [] {
            DatagramChannel.open(),
            SocketChannel.open(),
            ServerSocketChannel.open()};
        for (int i = 0; i < channels.length; i++) {
            SelectableChannel channel = channels[i];
            channel.close();
            try {
                channel.configureBlocking(true);
                throw new RuntimeException("expected exception not thrown");
            } catch (ClosedChannelException e) {
            }
        }
    }
}
