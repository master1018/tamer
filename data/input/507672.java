public class ProfilesLoader {
    public static double[] loadProfiles(IDevice device, Window window, String params) {
        Socket socket = null;
        BufferedReader in = null;
        BufferedWriter out = null;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress("127.0.0.1",
                    DeviceBridge.getDeviceLocalPort(device)));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.write("PROFILE " + window.encode() + " " + params);
            out.newLine();
            out.flush();
            String response = in.readLine();
            String[] data = response.split(" ");
            double[] profiles = new double[data.length];
            for (int i = 0; i < data.length; i++) {
                profiles[i] = (Long.parseLong(data[i]) / 1000.0) / 1000.0; 
            }
            return profiles;
        } catch (IOException e) {
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
        return null;
    }
}
