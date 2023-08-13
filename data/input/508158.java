class ViewServer implements Runnable {
    public static final int VIEW_SERVER_DEFAULT_PORT = 4939;
    private static final String LOG_TAG = "ViewServer";
    private static final String VALUE_PROTOCOL_VERSION = "2";
    private static final String VALUE_SERVER_VERSION = "3";
    private static final String COMMAND_PROTOCOL_VERSION = "PROTOCOL";
    private static final String COMMAND_SERVER_VERSION = "SERVER";
    private static final String COMMAND_WINDOW_MANAGER_LIST = "LIST";
    private ServerSocket mServer;
    private Thread mThread;
    private final WindowManagerService mWindowManager;
    private final int mPort;
    ViewServer(WindowManagerService windowManager) {
        this(windowManager, VIEW_SERVER_DEFAULT_PORT);
    }
    ViewServer(WindowManagerService windowManager, int port) {
        mWindowManager = windowManager;
        mPort = port;
    }
    boolean start() throws IOException {
        if (mThread != null) {
            return false;
        }
        mServer = new ServerSocket(mPort, 1, InetAddress.getLocalHost());
        mThread = new Thread(this, "Remote View Server [port=" + mPort + "]");
        mThread.start();
        return true;
    }
    boolean stop() {
        if (mThread != null) {
            mThread.interrupt();
            mThread = null;
            try {
                mServer.close();
                mServer = null;
                return true;
            } catch (IOException e) {
                Slog.w(LOG_TAG, "Could not close the view server");
            }
        }
        return false;
    }
    boolean isRunning() {
        return mThread != null && mThread.isAlive();
    }
    public void run() {
        final ServerSocket server = mServer;
        while (Thread.currentThread() == mThread) {
            Socket client = null;
            try {
                client = server.accept();
                BufferedReader in = null;
                try {
                    in = new BufferedReader(new InputStreamReader(client.getInputStream()), 1024);
                    final String request = in.readLine();
                    String command;
                    String parameters;
                    int index = request.indexOf(' ');
                    if (index == -1) {
                        command = request;
                        parameters = "";
                    } else {
                        command = request.substring(0, index);
                        parameters = request.substring(index + 1);
                    }
                    boolean result;
                    if (COMMAND_PROTOCOL_VERSION.equalsIgnoreCase(command)) {
                        result = writeValue(client, VALUE_PROTOCOL_VERSION);
                    } else if (COMMAND_SERVER_VERSION.equalsIgnoreCase(command)) {
                        result = writeValue(client, VALUE_SERVER_VERSION);
                    } else if (COMMAND_WINDOW_MANAGER_LIST.equalsIgnoreCase(command)) {
                        result = mWindowManager.viewServerListWindows(client);
                    } else {
                        result = mWindowManager.viewServerWindowCommand(client,
                                command, parameters);
                    }
                    if (!result) {
                        Slog.w(LOG_TAG, "An error occured with the command: " + command);
                    }
                } finally {
                    if (in != null) {
                        in.close();
                    }
                }
            } catch (Exception e) {
                Slog.w(LOG_TAG, "Connection error: ", e);
            } finally {
                if (client != null) {
                    try {
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    private static boolean writeValue(Socket client, String value) {
        boolean result;
        BufferedWriter out = null;
        try {
            OutputStream clientStream = client.getOutputStream();
            out = new BufferedWriter(new OutputStreamWriter(clientStream), 8 * 1024);
            out.write(value);
            out.write("\n");
            out.flush();
            result = true;
        } catch (Exception e) {
            result = false;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    result = false;
                }
            }
        }
        return result;
    }
}
