public class WindowsLoader {
    public static Window[] loadWindows(IDevice device, int protocol, int server) {
        Socket socket = null;
        BufferedReader in = null;
        BufferedWriter out = null;
        System.out.println("protocol = " + protocol);
        System.out.println("version = " + server);
        try {
            ArrayList<Window> windows = new ArrayList<Window>();
            socket = new Socket();
            socket.connect(new InetSocketAddress("127.0.0.1",
                    DeviceBridge.getDeviceLocalPort(device)));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.write("LIST");
            out.newLine();
            out.flush();
            String line;
            while ((line = in.readLine()) != null) {
                if ("DONE.".equalsIgnoreCase(line)) {
                    break;
                }
                int index = line.indexOf(' ');
                if (index != -1) {
                    String windowId = line.substring(0, index);
                    int id;
                    if (server > 2) {
                        id = (int) Long.parseLong(windowId, 16);
                    } else {
                        id = Integer.parseInt(windowId, 16);
                    }
                    Window w = new Window(line.substring(index + 1), id);
                    windows.add(w);
                }
            }
            return windows.toArray(new Window[windows.size()]);
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
        return new Window[0];
    }
}
