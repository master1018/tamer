public class MonkeySourceNetwork implements MonkeyEventSource {
    private static final String TAG = "MonkeyStub";
    public static class MonkeyCommandReturn {
        private final boolean success;
        private final String message;
        public MonkeyCommandReturn(boolean success) {
            this.success = success;
            this.message = null;
        }
        public MonkeyCommandReturn(boolean success,
                                   String message) {
            this.success = success;
            this.message = message;
        }
        boolean hasMessage() {
            return message != null;
        }
        String getMessage() {
            return message;
        }
        boolean wasSuccessful() {
            return success;
        }
    }
    public final static MonkeyCommandReturn OK = new MonkeyCommandReturn(true);
    public final static MonkeyCommandReturn ERROR = new MonkeyCommandReturn(false);
    public final static MonkeyCommandReturn EARG = new MonkeyCommandReturn(false,
                                                                            "Invalid Argument");
    public interface MonkeyCommand {
        MonkeyCommandReturn translateCommand(List<String> command, CommandQueue queue);
    }
    private static class FlipCommand implements MonkeyCommand {
        public MonkeyCommandReturn translateCommand(List<String> command,
                                                    CommandQueue queue) {
            if (command.size() > 1) {
                String direction = command.get(1);
                if ("open".equals(direction)) {
                    queue.enqueueEvent(new MonkeyFlipEvent(true));
                    return OK;
                } else if ("close".equals(direction)) {
                    queue.enqueueEvent(new MonkeyFlipEvent(false));
                    return OK;
                }
            }
            return EARG;
        }
    }
    private static class TouchCommand implements MonkeyCommand {
        public MonkeyCommandReturn translateCommand(List<String> command,
                                                    CommandQueue queue) {
            if (command.size() == 4) {
                String actionName = command.get(1);
                int x = 0;
                int y = 0;
                try {
                    x = Integer.parseInt(command.get(2));
                    y = Integer.parseInt(command.get(3));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Got something that wasn't a number", e);
                    return EARG;
                }
                int action = -1;
                if ("down".equals(actionName)) {
                    action = MotionEvent.ACTION_DOWN;
                } else if ("up".equals(actionName)) {
                    action = MotionEvent.ACTION_UP;
                } else if ("move".equals(actionName)) {
                    action = MotionEvent.ACTION_MOVE;
                }
                if (action == -1) {
                    Log.e(TAG, "Got a bad action: " + actionName);
                    return EARG;
                }
                queue.enqueueEvent(new MonkeyMotionEvent(MonkeyEvent.EVENT_TYPE_POINTER,
                                                         -1, action, x, y, 0));
                return OK;
            }
            return EARG;
        }
    }
    private static class TrackballCommand implements MonkeyCommand {
        public MonkeyCommandReturn translateCommand(List<String> command,
                                                    CommandQueue queue) {
            if (command.size() == 3) {
                int dx = 0;
                int dy = 0;
                try {
                    dx = Integer.parseInt(command.get(1));
                    dy = Integer.parseInt(command.get(2));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Got something that wasn't a number", e);
                    return EARG;
                }
                queue.enqueueEvent(new MonkeyMotionEvent(MonkeyEvent.EVENT_TYPE_TRACKBALL, -1,
                                                         MotionEvent.ACTION_MOVE, dx, dy, 0));
                return OK;
            }
            return EARG;
        }
    }
    private static class KeyCommand implements MonkeyCommand {
        public MonkeyCommandReturn translateCommand(List<String> command,
                                                    CommandQueue queue) {
            if (command.size() == 3) {
                int keyCode = getKeyCode(command.get(2));
                if (keyCode < 0) {
                    Log.e(TAG, "Can't find keyname: " + command.get(2));
                    return EARG;
                }
                Log.d(TAG, "keycode: " + keyCode);
                int action = -1;
                if ("down".equals(command.get(1))) {
                    action = KeyEvent.ACTION_DOWN;
                } else if ("up".equals(command.get(1))) {
                    action = KeyEvent.ACTION_UP;
                }
                if (action == -1) {
                    Log.e(TAG, "got unknown action.");
                    return EARG;
                }
                queue.enqueueEvent(new MonkeyKeyEvent(action, keyCode));
                return OK;
            }
            return EARG;
        }
    }
    private static int getKeyCode(String keyName) {
        int keyCode = -1;
        try {
            keyCode = Integer.parseInt(keyName);
        } catch (NumberFormatException e) {
            keyCode = MonkeySourceRandom.getKeyCode(keyName);
            if (keyCode == -1) {
                keyCode = MonkeySourceRandom.getKeyCode("KEYCODE_" + keyName.toUpperCase());
            }
        }
        return keyCode;
    }
    private static class SleepCommand implements MonkeyCommand {
        public MonkeyCommandReturn translateCommand(List<String> command,
                                                    CommandQueue queue) {
            if (command.size() == 2) {
                int sleep = -1;
                String sleepStr = command.get(1);
                try {
                    sleep = Integer.parseInt(sleepStr);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Not a number: " + sleepStr, e);
                    return EARG;
                }
                queue.enqueueEvent(new MonkeyThrottleEvent(sleep));
                return OK;
            }
            return EARG;
        }
    }
    private static class TypeCommand implements MonkeyCommand {
        public MonkeyCommandReturn translateCommand(List<String> command,
                                                    CommandQueue queue) {
            if (command.size() == 2) {
                String str = command.get(1);
                char[] chars = str.toString().toCharArray();
                KeyCharacterMap keyCharacterMap = KeyCharacterMap.
                        load(KeyCharacterMap.BUILT_IN_KEYBOARD);
                KeyEvent[] events = keyCharacterMap.getEvents(chars);
                for (KeyEvent event : events) {
                    queue.enqueueEvent(new MonkeyKeyEvent(event));
                }
                return OK;
            }
            return EARG;
        }
    }
    private static class WakeCommand implements MonkeyCommand {
        public MonkeyCommandReturn translateCommand(List<String> command,
                                                    CommandQueue queue) {
            if (!wake()) {
                return ERROR;
            }
            return OK;
        }
    }
    private static class TapCommand implements MonkeyCommand {
        public MonkeyCommandReturn translateCommand(List<String> command,
                                                    CommandQueue queue) {
            if (command.size() == 3) {
                int x = 0;
                int y = 0;
                try {
                    x = Integer.parseInt(command.get(1));
                    y = Integer.parseInt(command.get(2));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Got something that wasn't a number", e);
                    return EARG;
                }
                queue.enqueueEvent(new MonkeyMotionEvent(MonkeyEvent.EVENT_TYPE_POINTER,
                                                         -1, MotionEvent.ACTION_DOWN,
                                                         x, y, 0));
                queue.enqueueEvent(new MonkeyMotionEvent(MonkeyEvent.EVENT_TYPE_POINTER,
                                                         -1, MotionEvent.ACTION_UP,
                                                         x, y, 0));
                return OK;
            }
            return EARG;
        }
    }
    private static class PressCommand implements MonkeyCommand {
        public MonkeyCommandReturn translateCommand(List<String> command,
                                                    CommandQueue queue) {
            if (command.size() == 2) {
                int keyCode = getKeyCode(command.get(1));
                if (keyCode < 0) {
                    Log.e(TAG, "Can't find keyname: " + command.get(1));
                    return EARG;
                }
                queue.enqueueEvent(new MonkeyKeyEvent(KeyEvent.ACTION_DOWN, keyCode));
                queue.enqueueEvent(new MonkeyKeyEvent(KeyEvent.ACTION_UP, keyCode));
                return OK;
            }
            return EARG;
        }
    }
    private static final boolean wake() {
        IPowerManager pm =
                IPowerManager.Stub.asInterface(ServiceManager.getService(Context.POWER_SERVICE));
        try {
            pm.userActivityWithForce(SystemClock.uptimeMillis(), true, true);
        } catch (RemoteException e) {
            Log.e(TAG, "Got remote exception", e);
            return false;
        }
        return true;
    }
    private static final Map<String, MonkeyCommand> COMMAND_MAP = new HashMap<String, MonkeyCommand>();
    static {
        COMMAND_MAP.put("flip", new FlipCommand());
        COMMAND_MAP.put("touch", new TouchCommand());
        COMMAND_MAP.put("trackball", new TrackballCommand());
        COMMAND_MAP.put("key", new KeyCommand());
        COMMAND_MAP.put("sleep", new SleepCommand());
        COMMAND_MAP.put("wake", new WakeCommand());
        COMMAND_MAP.put("tap", new TapCommand());
        COMMAND_MAP.put("press", new PressCommand());
        COMMAND_MAP.put("type", new TypeCommand());
        COMMAND_MAP.put("listvar", new MonkeySourceNetworkVars.ListVarCommand());
        COMMAND_MAP.put("getvar", new MonkeySourceNetworkVars.GetVarCommand());
    }
    private static final String QUIT = "quit";
    private static final String DONE = "done";
    private static final String OK_STR = "OK";
    private static final String ERROR_STR = "ERROR";
    public static interface CommandQueue {
        public void enqueueEvent(MonkeyEvent e);
    };
    private static class CommandQueueImpl implements CommandQueue{
        private final Queue<MonkeyEvent> queuedEvents = new LinkedList<MonkeyEvent>();
        public void enqueueEvent(MonkeyEvent e) {
            queuedEvents.offer(e);
        }
        public MonkeyEvent getNextQueuedEvent() {
            return queuedEvents.poll();
        }
    };
    private final CommandQueueImpl commandQueue = new CommandQueueImpl();
    private BufferedReader input;
    private PrintWriter output;
    private boolean started = false;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    public MonkeySourceNetwork(int port) throws IOException {
        serverSocket = new ServerSocket(port,
                                        0, 
                                        InetAddress.getLocalHost());
    }
    private void startServer() throws IOException {
        clientSocket = serverSocket.accept();
        wake();
        input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        output = new PrintWriter(clientSocket.getOutputStream(), true);
    }
    private void stopServer() throws IOException {
        clientSocket.close();
        input.close();
        output.close();
        started = false;
    }
    private static String replaceQuotedChars(String input) {
        return input.replace("\\\"", "\"");
    }
    private static List<String> commandLineSplit(String line) {
        ArrayList<String> result = new ArrayList<String>();
        StringTokenizer tok = new StringTokenizer(line);
        boolean insideQuote = false;
        StringBuffer quotedWord = new StringBuffer();
        while (tok.hasMoreTokens()) {
            String cur = tok.nextToken();
            if (!insideQuote && cur.startsWith("\"")) {
                quotedWord.append(replaceQuotedChars(cur));
                insideQuote = true;
            } else if (insideQuote) {
                if (cur.endsWith("\"")) {
                    insideQuote = false;
                    quotedWord.append(" ").append(replaceQuotedChars(cur));
                    String word = quotedWord.toString();
                    result.add(word.substring(1, word.length() - 1));
                } else {
                    quotedWord.append(" ").append(replaceQuotedChars(cur));
                }
            } else {
                result.add(replaceQuotedChars(cur));
            }
        }
        return result;
    }
    private void translateCommand(String commandLine) {
        Log.d(TAG, "translateCommand: " + commandLine);
        List<String> parts = commandLineSplit(commandLine);
        if (parts.size() > 0) {
            MonkeyCommand command = COMMAND_MAP.get(parts.get(0));
            if (command != null) {
                MonkeyCommandReturn ret = command.translateCommand(parts,
                                                                   commandQueue);
                if (ret.wasSuccessful()) {
                    if (ret.hasMessage()) {
                        returnOk(ret.getMessage());
                    } else {
                        returnOk();
                    }
                } else {
                    if (ret.hasMessage()) {
                        returnError(ret.getMessage());
                    } else {
                        returnError();
                    }
                }
            }
        }
    }
    public MonkeyEvent getNextEvent() {
        if (!started) {
            try {
                startServer();
            } catch (IOException e) {
                Log.e(TAG, "Got IOException from server", e);
                return null;
            }
            started = true;
        }
        try {
            while (true) {
                MonkeyEvent queuedEvent = commandQueue.getNextQueuedEvent();
                if (queuedEvent != null) {
                    return queuedEvent;
                }
                String command = input.readLine();
                if (command == null) {
                    Log.d(TAG, "Connection dropped.");
                    command = DONE;
                }
                if (DONE.equals(command)) {
                    try {
                        stopServer();
                    } catch (IOException e) {
                        Log.e(TAG, "Got IOException shutting down!", e);
                        return null;
                    }
                    return new MonkeyNoopEvent();
                }
                if (QUIT.equals(command)) {
                    Log.d(TAG, "Quit requested");
                    returnOk();
                    return null;
                }
                if (command.startsWith("#")) {
                    continue;
                }
                translateCommand(command);
            }
        } catch (IOException e) {
            Log.e(TAG, "Exception: ", e);
            return null;
        }
    }
    private void returnError() {
        output.println(ERROR_STR);
    }
    private void returnError(String msg) {
        output.print(ERROR_STR);
        output.print(":");
        output.println(msg);
    }
    private void returnOk() {
        output.println(OK_STR);
    }
    private void returnOk(String returnValue) {
        output.print(OK_STR);
        output.print(":");
        output.println(returnValue);
    }
    public void setVerbose(int verbose) {
    }
    public boolean validate() {
        return true;
    }
}
