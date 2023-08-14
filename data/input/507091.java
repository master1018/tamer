public class EventAnalyzer implements ILogListener {
    private final static int TAG_ACTIVITY_LAUNCH_TIME = 30009;
    private final static char DATA_SEPARATOR = ',';
    private final static String CVS_EXT = ".csv";
    private final static String TAG_FILE_EXT = ".tag"; 
    private EventLogParser mParser;
    private TreeMap<String, ArrayList<Long>> mLaunchMap = new TreeMap<String, ArrayList<Long>>();
    String mInputTextFile = null;
    String mInputBinaryFile = null;
    String mInputDevice = null;
    String mInputFolder = null;
    String mAlternateTagFile = null;
    String mOutputFile = null;
    public static void main(String[] args) {
        new EventAnalyzer().run(args);
    }
    private void run(String[] args) {
        if (args.length == 0) {
            printUsageAndQuit();
        }
        int index = 0;
        do {
            String argument = args[index++];
            if ("-s".equals(argument)) {
                checkInputValidity("-s");
                if (index == args.length) {
                    printUsageAndQuit();
                }
                mInputDevice = args[index++];
            } else if ("-fb".equals(argument)) {
                checkInputValidity("-fb");
                if (index == args.length) {
                    printUsageAndQuit();
                }
                mInputBinaryFile = args[index++];
            } else if ("-ft".equals(argument)) {
                checkInputValidity("-ft");
                if (index == args.length) {
                    printUsageAndQuit();
                }
                mInputTextFile = args[index++];
            } else if ("-F".equals(argument)) {
                checkInputValidity("-F");
                if (index == args.length) {
                    printUsageAndQuit();
                }
                mInputFolder = args[index++];
            } else if ("-t".equals(argument)) {
                if (index == args.length) {
                    printUsageAndQuit();
                }
                mAlternateTagFile = args[index++];
            } else {
                mOutputFile = argument;
                if (index < args.length) {
                    printAndExit("Too many arguments!", false );
                }
            }
        } while (index < args.length);
        if ((mInputTextFile == null && mInputBinaryFile == null && mInputFolder == null &&
                mInputDevice == null)) {
            printUsageAndQuit();
        }
        File outputParent = new File(mOutputFile).getParentFile();
        if (outputParent == null || outputParent.isDirectory() == false) {
            printAndExit(String.format("%1$s is not a valid ouput file", mOutputFile),
                    false );
        }
        Log.setLogOutput(new ILogOutput() {
            public void printAndPromptLog(LogLevel logLevel, String tag, String message) {
            }
            public void printLog(LogLevel logLevel, String tag, String message) {
            }
        });
        try {
            if (mInputBinaryFile != null) {
                parseBinaryLogFile();
            } else if (mInputTextFile != null) {
                parseTextLogFile(mInputTextFile);
            } else if (mInputFolder != null) {
                parseFolder(mInputFolder);
            } else if (mInputDevice != null) {
                parseLogFromDevice();
            }
            analyzeData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void parseBinaryLogFile() throws IOException {
        mParser = new EventLogParser();
        String tagFile = mInputBinaryFile + TAG_FILE_EXT;
        if (mParser.init(tagFile) == false) {
            if (mAlternateTagFile != null) {
                if (mParser.init(mAlternateTagFile) == false) {
                    printAndExit("Failed to get event tags from " + mAlternateTagFile,
                            false );
                }
            } else {
                printAndExit("Failed to get event tags from " + tagFile, false );
            }
        }
        LogReceiver receiver = new LogReceiver(this);
        byte[] buffer = new byte[256];
        FileInputStream fis = new FileInputStream(mInputBinaryFile);
        int count;
        while ((count = fis.read(buffer)) != -1) {
            receiver.parseNewData(buffer, 0, count);
        }
    }
    private void parseTextLogFile(String filePath) throws IOException {
        mParser = new EventLogParser();
        String tagFile = filePath + TAG_FILE_EXT;
        if (mParser.init(tagFile) == false) {
            if (mAlternateTagFile != null) {
                if (mParser.init(mAlternateTagFile) == false) {
                    printAndExit("Failed to get event tags from " + mAlternateTagFile,
                            false );
                }
            } else {
                printAndExit("Failed to get event tags from " + tagFile, false );
            }
        }
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath)));
        String line;
        while ((line = reader.readLine()) != null) {
            processEvent(mParser.parse(line));
        }
    }
    private void parseLogFromDevice() throws IOException {
        AndroidDebugBridge.init(false );
        try {
            AndroidDebugBridge bridge = AndroidDebugBridge.createBridge();
            int count = 0;
            while (bridge.hasInitialDeviceList() == false) {
                try {
                    Thread.sleep(100);
                    count++;
                } catch (InterruptedException e) {
                }
                if (count > 100) {
                    printAndExit("Timeout getting device list!", true );
                }
            }
            IDevice[] devices = bridge.getDevices();
            for (IDevice device : devices) {
                if (device.getSerialNumber().equals(mInputDevice)) {
                    grabLogFrom(device);
                    return;
                }
            }
            System.err.println("Could not find " + mInputDevice);
        } finally {
            AndroidDebugBridge.terminate();
        }
    }
    private void parseFolder(String folderPath) {
        File f = new File(folderPath);
        if (f.isDirectory() == false) {
            printAndExit(String.format("%1$s is not a valid folder", folderPath),
                    false );
        }
        String[] files = f.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return name.endsWith(".tag") == false;
            }
        });
        for (String file : files) {
            try {
                f = new File(folderPath + File.separator + file);
                if (f.isDirectory()) {
                    parseFolder(f.getAbsolutePath());
                } else {
                    parseTextLogFile(f.getAbsolutePath());
                }
            } catch (IOException e) {
            }
        }
    }
    private void grabLogFrom(IDevice device) throws IOException {
        mParser = new EventLogParser();
        if (mParser.init(device) == false) {
            printAndExit("Failed to get event-log-tags from " + device.getSerialNumber(),
                    true );
        }
        LogReceiver receiver = new LogReceiver(this);
        device.runEventLogService(receiver);
    }
    private void analyzeData() throws IOException {
        BufferedWriter writer = null;
        try {
            if (mOutputFile.toLowerCase().endsWith(CVS_EXT) == false) {
                mOutputFile = mOutputFile + CVS_EXT;
            }
            writer = new BufferedWriter(new FileWriter(mOutputFile));
            StringBuilder builder = new StringBuilder();
            Set<String> activities = mLaunchMap.keySet();
            for (String activity : activities) {
                builder.append(activity).append(DATA_SEPARATOR);
            }
            writer.write(builder.append('\n').toString());
            boolean moreValues = true;
            int index = 0;
            while (moreValues) {
                moreValues = false;
                builder.setLength(0);
                for (String activity : activities) {
                    ArrayList<Long> list = mLaunchMap.get(activity);
                    if (index < list.size()) {
                        moreValues = true;
                        builder.append(list.get(index).longValue()).append(DATA_SEPARATOR);
                    } else {
                        builder.append(DATA_SEPARATOR);
                    }
                }
                if (moreValues) {
                    writer.write(builder.append('\n').toString());
                }
                index++;
            }
            for (String activity : activities) {
                builder.setLength(0);
                builder.append(activity).append(DATA_SEPARATOR);
                ArrayList<Long> list = mLaunchMap.get(activity);
                Collections.sort(list);
                builder.append(list.get(0).longValue()).append(DATA_SEPARATOR);
                builder.append(list.get(list.size()-1).longValue()).append(DATA_SEPARATOR);
                builder.append(list.get(list.size()/2).longValue()).append(DATA_SEPARATOR);
                long total = 0; 
                for (Long value : list) {
                    total += value.longValue();
                }
                builder.append(total / list.size()).append(DATA_SEPARATOR);
                writer.write(builder.append('\n').toString());
            }
        } finally {
            writer.close();
        }
    }
    public void newData(byte[] data, int offset, int length) {
    }
    public void newEntry(LogEntry entry) {
        processEvent(mParser.parse(entry));
    }
    private void processEvent(EventContainer event) {
        if (event != null && event.mTag == TAG_ACTIVITY_LAUNCH_TIME) {
            try {
                String name = event.getValueAsString(0);
                Object value = event.getValue(1);
                if (value instanceof Long) {
                    addLaunchTime(name, (Long)value);
                }
            } catch (InvalidTypeException e) {
            }
        }
    }
    private void addLaunchTime(String name, Long value) {
        ArrayList<Long> list = mLaunchMap.get(name);
        if (list == null) {
            list = new ArrayList<Long>();
            mLaunchMap.put(name, list);
        }
        list.add(value);
    }
    private void checkInputValidity(String option) {
        if (mInputTextFile != null || mInputBinaryFile != null) {
            printAndExit(String.format("ERROR: %1$s cannot be used with an input file.", option),
                    false );
        } else if (mInputFolder != null) {
            printAndExit(String.format("ERROR: %1$s cannot be used with an input file.", option),
                    false );
        } else if (mInputDevice != null) {
            printAndExit(String.format("ERROR: %1$s cannot be used with an input device serial number.",
                    option), false );
        }
    }
    private static void printUsageAndQuit() {
        System.out.println("Usage:");
        System.out.println("   eventanalyzer [-t <TAG_FILE>] <SOURCE> <OUTPUT>");
        System.out.println("");
        System.out.println("Possible sources:");
        System.out.println("   -fb <file>    The path to a binary event log, gathered by dumpeventlog");
        System.out.println("   -ft <file>    The path to a text event log, gathered by adb logcat -b events");
        System.out.println("   -F <folder>   The path to a folder containing multiple text log files.");
        System.out.println("   -s <serial>   The serial number of the Device to grab the event log from.");
        System.out.println("Options:");
        System.out.println("   -t <file>     The path to tag file to use in case the one associated with");
        System.out.println("                 the source is missing");
        System.exit(1);
    }
    private static void printAndExit(String message, boolean terminate) {
        System.out.println(message);
        if (terminate) {
            AndroidDebugBridge.terminate();
        }
        System.exit(1);
    }
}
