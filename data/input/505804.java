public class DumpEventLog {
    private static class LogWriter implements ILogListener {
        private FileOutputStream mOutputStream;
        private LogReceiver mReceiver;
        public LogWriter(String filePath) throws IOException {
            mOutputStream = new FileOutputStream(filePath);
        }
        public void newData(byte[] data, int offset, int length) {
            try {
                mOutputStream.write(data, offset, length);
            } catch (IOException e) {
                if (mReceiver != null) {
                    mReceiver.cancel();
                }
                System.out.println(e);
            }
        }
        public void newEntry(LogEntry entry) {
        }
        public void setReceiver(LogReceiver receiver) {
            mReceiver = receiver;
        }
        public void done() throws IOException {
            mOutputStream.close();
        }
    }
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: dumpeventlog <device s/n> <filepath>");
            return;
        }
        Log.setLogOutput(new ILogOutput() {
            public void printAndPromptLog(LogLevel logLevel, String tag, String message) {
            }
            public void printLog(LogLevel logLevel, String tag, String message) {
            }
        });
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
                    System.err.println("Timeout getting device list!");
                    return;
                }
            }
            IDevice[] devices = bridge.getDevices();
            for (IDevice device : devices) {
                if (device.getSerialNumber().equals(args[0])) {
                    try {
                        grabLogFrom(device, args[1]);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
            System.err.println("Could not find " + args[0]);
        } finally {
            AndroidDebugBridge.terminate();
        }
    }
    private static void grabLogFrom(IDevice device, String filePath) throws IOException {
        LogWriter writer = new LogWriter(filePath);
        LogReceiver receiver = new LogReceiver(writer);
        writer.setReceiver(receiver);
        device.runEventLogService(receiver);
        writer.done();
    }
}
