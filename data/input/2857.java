public class UseDGWithIPv6 {
    static String[] targets = {
        "3ffe:e00:811:b::21:5",
        "15.70.186.80"
    };
    static int BUFFER_LEN = 10;
    static int port = 12345;
    public static void main(String[] args) throws IOException
    {
        ByteBuffer data = ByteBuffer.wrap("TESTING DATA".getBytes());
        DatagramChannel dgChannel = DatagramChannel.open();
        for(int i = 0; i < targets.length; i++){
            data.rewind();
            SocketAddress sa = new InetSocketAddress(targets[i], port);
            System.out.println("-------------\nDG_Sending data:" +
                               "\n    remaining:" + data.remaining() +
                               "\n     position:" + data.position() +
                               "\n        limit:" + data.limit() +
                               "\n     capacity:" + data.capacity() +
                               " bytes on DG channel to " + sa);
            try {
                int n = dgChannel.send(data, sa);
                System.out.println("DG_Sent " + n + " bytes");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dgChannel.close();
    }
}
