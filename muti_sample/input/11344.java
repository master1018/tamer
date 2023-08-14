public class TestTcpNoDelay
{
    public static void main(String[] args) {
        try {
            Socket socket = new Socket();
            boolean on = socket.getTcpNoDelay();
            System.out.println("Get TCP_NODELAY = " + on);
            boolean opposite = on ? false: true;
            System.out.println("Set TCP_NODELAY to " + opposite);
            socket.setTcpNoDelay(opposite);
            boolean noDelay = socket.getTcpNoDelay();
            System.out.println("Get TCP_NODELAY = " + noDelay);
            if (noDelay != opposite)
                throw new RuntimeException("setTcpNoDelay no working as expected");
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
