public final class EmulatorConsole {
    private final static String DEFAULT_ENCODING = "ISO-8859-1"; 
    private final static int WAIT_TIME = 5; 
    private final static int STD_TIMEOUT = 5000; 
    private final static String HOST = "127.0.0.1";  
    private final static String COMMAND_PING = "help\r\n"; 
    private final static String COMMAND_AVD_NAME = "avd name\r\n"; 
    private final static String COMMAND_KILL = "kill\r\n"; 
    private final static String COMMAND_GSM_STATUS = "gsm status\r\n"; 
    private final static String COMMAND_GSM_CALL = "gsm call %1$s\r\n"; 
    private final static String COMMAND_GSM_CANCEL_CALL = "gsm cancel %1$s\r\n"; 
    private final static String COMMAND_GSM_DATA = "gsm data %1$s\r\n"; 
    private final static String COMMAND_GSM_VOICE = "gsm voice %1$s\r\n"; 
    private final static String COMMAND_SMS_SEND = "sms send %1$s %2$s\r\n"; 
    private final static String COMMAND_NETWORK_STATUS = "network status\r\n"; 
    private final static String COMMAND_NETWORK_SPEED = "network speed %1$s\r\n"; 
    private final static String COMMAND_NETWORK_LATENCY = "network delay %1$s\r\n"; 
    private final static String COMMAND_GPS =
        "geo nmea $GPGGA,%1$02d%2$02d%3$02d.%4$03d," + 
        "%5$03d%6$09.6f,%7$c,%8$03d%9$09.6f,%10$c," + 
        "1,10,0.0,0.0,0,0.0,0,0.0,0000\r\n"; 
    private final static Pattern RE_KO = Pattern.compile("KO:\\s+(.*)"); 
    public final static int[] MIN_LATENCIES = new int[] {
        0,      
        150,    
        80,     
        35      
    };
    public final int[] DOWNLOAD_SPEEDS = new int[] {
        0,          
        14400,      
        43200,      
        80000,      
        236800,     
        1920000,    
        14400000    
    };
    public final static String[] NETWORK_SPEEDS = new String[] {
        "full", 
        "gsm", 
        "hscsd", 
        "gprs", 
        "edge", 
        "umts", 
        "hsdpa", 
    };
    public final static String[] NETWORK_LATENCIES = new String[] {
        "none", 
        "gprs", 
        "edge", 
        "umts", 
    };
    public static enum GsmMode {
        UNKNOWN((String)null),
        UNREGISTERED(new String[] { "unregistered", "off" }),
        HOME(new String[] { "home", "on" }),
        ROAMING("roaming"),
        SEARCHING("searching"),
        DENIED("denied");
        private final String[] tags;
        GsmMode(String tag) {
            if (tag != null) {
                this.tags = new String[] { tag };
            } else {
                this.tags = new String[0];
            }
        }
        GsmMode(String[] tags) {
            this.tags = tags;
        }
        public static GsmMode getEnum(String tag) {
            for (GsmMode mode : values()) {
                for (String t : mode.tags) {
                    if (t.equals(tag)) {
                        return mode;
                    }
                }
            }
            return UNKNOWN;
        }
        public String getTag() {
            if (tags.length > 0) {
                return tags[0];
            }
            return null;
        }
    }
    public final static String RESULT_OK = null;
    private final static Pattern sEmulatorRegexp = Pattern.compile(Device.RE_EMULATOR_SN);
    private final static Pattern sVoiceStatusRegexp = Pattern.compile(
            "gsm\\s+voice\\s+state:\\s*([a-z]+)", Pattern.CASE_INSENSITIVE); 
    private final static Pattern sDataStatusRegexp = Pattern.compile(
            "gsm\\s+data\\s+state:\\s*([a-z]+)", Pattern.CASE_INSENSITIVE); 
    private final static Pattern sDownloadSpeedRegexp = Pattern.compile(
            "\\s+download\\s+speed:\\s+(\\d+)\\s+bits.*", Pattern.CASE_INSENSITIVE); 
    private final static Pattern sMinLatencyRegexp = Pattern.compile(
            "\\s+minimum\\s+latency:\\s+(\\d+)\\s+ms", Pattern.CASE_INSENSITIVE); 
    private final static HashMap<Integer, EmulatorConsole> sEmulators =
        new HashMap<Integer, EmulatorConsole>();
    public static class GsmStatus {
        public GsmMode voice = GsmMode.UNKNOWN;
        public GsmMode data = GsmMode.UNKNOWN;
    }
    public static class NetworkStatus {
        public int speed = -1;
        public int latency = -1;
    }
    private int mPort;
    private SocketChannel mSocketChannel;
    private byte[] mBuffer = new byte[1024];
    public static synchronized EmulatorConsole getConsole(IDevice d) {
        Matcher m = sEmulatorRegexp.matcher(d.getSerialNumber());
        if (m.matches()) {
            int port;
            try {
                port = Integer.parseInt(m.group(1));
                if (port <= 0) {
                    return null;
                }
            } catch (NumberFormatException e) {
                return null;
            }
            EmulatorConsole console = sEmulators.get(port);
            if (console != null) {
                if (console.ping() == false) {
                    RemoveConsole(console.mPort);
                    console = null;
                }
            }
            if (console == null) {
                console = new EmulatorConsole(port);
                if (console.start()) {
                    sEmulators.put(port, console);
                } else {
                    console = null;
                }
            }
            return console;
        }
        return null;
    }
    private static synchronized void RemoveConsole(int port) {
        sEmulators.remove(port);
    }
    private EmulatorConsole(int port) {
        super();
        mPort = port;
    }
    private boolean start() {
        InetSocketAddress socketAddr;
        try {
            InetAddress hostAddr = InetAddress.getByName(HOST);
            socketAddr = new InetSocketAddress(hostAddr, mPort);
        } catch (UnknownHostException e) {
            return false;
        }
        try {
            mSocketChannel = SocketChannel.open(socketAddr);
        } catch (IOException e1) {
            return false;
        }
        readLines();
        return true;
    }
    private synchronized boolean ping() {
        if (sendCommand(COMMAND_PING)) {
            return readLines() != null;
        }
        return false;
    }
    public synchronized void kill() {
        if (sendCommand(COMMAND_KILL)) {
            RemoveConsole(mPort);
        }
    }
    public synchronized String getAvdName() {
        if (sendCommand(COMMAND_AVD_NAME)) {
            String[] result = readLines();
            if (result != null && result.length == 2) { 
                return result[0];
            } else {
                Matcher m = RE_KO.matcher(result[result.length-1]);
                if (m.matches()) {
                    return m.group(1);
                }
            }
        }
        return null;
    }
    public synchronized NetworkStatus getNetworkStatus() {
        if (sendCommand(COMMAND_NETWORK_STATUS)) {
            String[] result = readLines();
            if (isValid(result)) {
                NetworkStatus status = new NetworkStatus();
                for (String line : result) {
                    Matcher m = sDownloadSpeedRegexp.matcher(line);
                    if (m.matches()) {
                        String value = m.group(1);
                        status.speed = getSpeedIndex(value);
                        continue;
                    }
                    m = sMinLatencyRegexp.matcher(line);
                    if (m.matches()) {
                        String value = m.group(1);
                        status.latency = getLatencyIndex(value);
                        continue;
                    }
                }
                return status;
            }
        }
        return null;
    }
    public synchronized GsmStatus getGsmStatus() {
        if (sendCommand(COMMAND_GSM_STATUS)) {
            String[] result = readLines();
            if (isValid(result)) {
                GsmStatus status = new GsmStatus();
                for (String line : result) {
                    Matcher m = sVoiceStatusRegexp.matcher(line);
                    if (m.matches()) {
                        String value = m.group(1);
                        status.voice = GsmMode.getEnum(value.toLowerCase());
                        continue;
                    }
                    m = sDataStatusRegexp.matcher(line);
                    if (m.matches()) {
                        String value = m.group(1);
                        status.data = GsmMode.getEnum(value.toLowerCase());
                        continue;
                    }
                }
                return status;
            }
        }
        return null;
    }
    public synchronized String setGsmVoiceMode(GsmMode mode) throws InvalidParameterException {
        if (mode == GsmMode.UNKNOWN) {
            throw new InvalidParameterException();
        }
        String command = String.format(COMMAND_GSM_VOICE, mode.getTag());
        return processCommand(command);
    }
    public synchronized String setGsmDataMode(GsmMode mode) throws InvalidParameterException {
        if (mode == GsmMode.UNKNOWN) {
            throw new InvalidParameterException();
        }
        String command = String.format(COMMAND_GSM_DATA, mode.getTag());
        return processCommand(command);
    }
    public synchronized String call(String number) {
        String command = String.format(COMMAND_GSM_CALL, number);
        return processCommand(command);
    }
    public synchronized String cancelCall(String number) {
        String command = String.format(COMMAND_GSM_CANCEL_CALL, number);
        return processCommand(command);
    }
    public synchronized String sendSms(String number, String message) {
        String command = String.format(COMMAND_SMS_SEND, number, message);
        return processCommand(command);
    }
    public synchronized String setNetworkSpeed(int selectionIndex) {
        String command = String.format(COMMAND_NETWORK_SPEED, NETWORK_SPEEDS[selectionIndex]);
        return processCommand(command);
    }
    public synchronized String setNetworkLatency(int selectionIndex) {
        String command = String.format(COMMAND_NETWORK_LATENCY, NETWORK_LATENCIES[selectionIndex]);
        return processCommand(command);
    }
    public synchronized String sendLocation(double longitude, double latitude, double elevation) {
        Calendar c = Calendar.getInstance();
        double absLong = Math.abs(longitude);
        int longDegree = (int)Math.floor(absLong);
        char longDirection = 'E';
        if (longitude < 0) {
            longDirection = 'W';
        }
        double longMinute = (absLong - Math.floor(absLong)) * 60;
        double absLat = Math.abs(latitude);
        int latDegree = (int)Math.floor(absLat);
        char latDirection = 'N';
        if (latitude < 0) {
            latDirection = 'S';
        }
        double latMinute = (absLat - Math.floor(absLat)) * 60;
        String command = String.format(COMMAND_GPS,
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE),
                c.get(Calendar.SECOND), c.get(Calendar.MILLISECOND),
                latDegree, latMinute, latDirection,
                longDegree, longMinute, longDirection);
        return processCommand(command);
    }
    private boolean sendCommand(String command) {
        boolean result = false;
        try {
            byte[] bCommand;
            try {
                bCommand = command.getBytes(DEFAULT_ENCODING);
            } catch (UnsupportedEncodingException e) {
                return result;
            }
            AdbHelper.write(mSocketChannel, bCommand, bCommand.length, DdmPreferences.getTimeOut());
            result = true;
        } catch (IOException e) {
            return false;
        } finally {
            if (result == false) {
                RemoveConsole(mPort);
            }
        }
        return result;
    }
    private String processCommand(String command) {
        if (sendCommand(command)) {
            String[] result = readLines();
            if (result != null && result.length > 0) {
                Matcher m = RE_KO.matcher(result[result.length-1]);
                if (m.matches()) {
                    return m.group(1);
                }
                return RESULT_OK;
            }
            return "Unable to communicate with the emulator";
        }
        return "Unable to send command to the emulator";
    }
    private String[] readLines() {
        try {
            ByteBuffer buf = ByteBuffer.wrap(mBuffer, 0, mBuffer.length);
            int numWaits = 0;
            boolean stop = false;
            while (buf.position() != buf.limit() && stop == false) {
                int count;
                count = mSocketChannel.read(buf);
                if (count < 0) {
                    return null;
                } else if (count == 0) {
                    if (numWaits * WAIT_TIME > STD_TIMEOUT) {
                        return null;
                    }
                    try {
                        Thread.sleep(WAIT_TIME);
                    } catch (InterruptedException ie) {
                    }
                    numWaits++;
                } else {
                    numWaits = 0;
                }
                if (buf.position() >= 4) {
                    int pos = buf.position();
                    if (endsWithOK(pos) || lastLineIsKO(pos)) {
                        stop = true;
                    }
                }
            }
            String msg = new String(mBuffer, 0, buf.position(), DEFAULT_ENCODING);
            return msg.split("\r\n"); 
        } catch (IOException e) {
            return null;
        }
    }
    private boolean endsWithOK(int currentPosition) {
        if (mBuffer[currentPosition-1] == '\n' &&
                mBuffer[currentPosition-2] == '\r' &&
                mBuffer[currentPosition-3] == 'K' &&
                mBuffer[currentPosition-4] == 'O') {
            return true;
        }
        return false;
    }
    private boolean lastLineIsKO(int currentPosition) {
        if (mBuffer[currentPosition-1] != '\n' ||
                mBuffer[currentPosition-2] != '\r') {
            return false;
        }
        int i = 0;
        for (i = currentPosition-3 ; i >= 0; i--) {
            if (mBuffer[i] == '\n') {
                if (i > 0 && mBuffer[i-1] == '\r') {
                    break;
                }
            }
        }
        if (mBuffer[i+1] == 'K' && mBuffer[i+2] == 'O') {
            return true;
        }
        return false;
    }
    private boolean isValid(String[] result) {
        if (result != null && result.length > 0) {
            return !(RE_KO.matcher(result[result.length-1]).matches());
        }
        return false;
    }
    private int getLatencyIndex(String value) {
        try {
            int latency = Integer.parseInt(value);
            for (int i = 0 ; i < MIN_LATENCIES.length; i++) {
                if (MIN_LATENCIES[i] == latency) {
                    return i;
                }
            }
        } catch (NumberFormatException e) {
        }
        return -1;
    }
    private int getSpeedIndex(String value) {
        try {
            int speed = Integer.parseInt(value);
            for (int i = 0 ; i < DOWNLOAD_SPEEDS.length; i++) {
                if (DOWNLOAD_SPEEDS[i] == speed) {
                    return i;
                }
            }
        } catch (NumberFormatException e) {
        }
        return -1;
    }
}
