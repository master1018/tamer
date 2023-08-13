public class AdbUtils {
    private static final String ADB_OK = "OKAY";
    private static final int ADB_PORT = 5037;
    private static final String ADB_HOST = "127.0.0.1";
    private static final int ADB_RESPONSE_SIZE = 4;
    private static final String LOGTAG = "AdbUtils";
    public static String convert(int host) {
        return ((host >> 24) & 0xFF) + "."
        + ((host >> 16) & 0xFF) + "."
        + ((host >> 8) & 0xFF) + "."
        + (host & 0xFF);
    }
    public static int resolve(String host)  throws IOException {
        Socket localSocket = new Socket(ADB_HOST, ADB_PORT);
        DataInputStream dis = new DataInputStream(localSocket.getInputStream());
        OutputStream os = localSocket.getOutputStream();
        int count_read = 0;
        if (localSocket == null || dis == null || os == null)
            return -1;
        String cmd = "dns:" + host;
        if(!sendAdbCmd(dis, os, cmd))
            return -1;
        count_read = dis.readInt();
        localSocket.close();
        return count_read;
    }
    public static boolean sendAdbCmd(InputStream is, OutputStream os,
            String cmd) throws IOException {
        byte[] buf = new byte[ADB_RESPONSE_SIZE];
        cmd = String.format("%04X", cmd.length()) + cmd;
        os.write(cmd.getBytes());
        int read = is.read(buf);
        if(read != ADB_RESPONSE_SIZE || !ADB_OK.equals(new String(buf))) {
            Log.w(LOGTAG, "adb cmd faild.");
            return false;
        }
        return true;
    }
    public static Socket getForwardedSocket(int remoteAddress, int remotePort) {
        try {
            Socket socket = new Socket(ADB_HOST, ADB_PORT);
            String cmd = "tcp:" + remotePort + ":" + convert(remoteAddress);
            if(!sendAdbCmd(socket.getInputStream(), socket.getOutputStream(), cmd)) {
                socket.close();
                return null;
            }
            return socket;
        } catch (IOException ioe) {
            Log.w(LOGTAG, "error creating adb socket", ioe);
            return null;
        }
    }
}
