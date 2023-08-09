public class VersionLoader {
    public static int loadServerVersion(IDevice device) {
        return loadVersion(device, "SERVER");
    }
    public static int loadProtocolVersion(IDevice device) {
        return loadVersion(device, "PROTOCOL");
    }
    private static int loadVersion(IDevice device, String command) {
        Socket socket = null;
        BufferedReader in = null;
        BufferedWriter out = null;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress("127.0.0.1",
                    DeviceBridge.getDeviceLocalPort(device)));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.write(command);
            out.newLine();
            out.flush();
            return Integer.parseInt(in.readLine());
        } catch (Exception e) {
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return 2;
    }
}
